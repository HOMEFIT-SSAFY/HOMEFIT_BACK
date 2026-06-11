# Backend DB Optimization Roadmap

## 목적

현재 HappyHome 백엔드는 MySQL, MyBatis, Spring Boot 기반으로 다음 기능을 제공한다.

- 아파트 실거래가 검색
- 시/도, 구/군, 동 지역 선택
- LH 공공임대 공고 조회 및 캐시
- 생활권 분석
- 양도 게시판
- 관심 매물
- 회원/공지 관리

이 문서는 실제 서비스 품질을 높이기 위해 적용 가능한 DB 튜닝, 공간 검색, 배치, 알림, 통계 개선 항목을 정리한다.

## 현재 구조 기준 주요 테이블

현재 백엔드 코드가 직접 사용하는 핵심 테이블은 다음과 같다.

- `dongcodes`: 법정동 코드 및 시/군/동 이름
- `houseinfos`: 아파트 기본 정보
- `housedeals`: 아파트 거래 정보
- `favorite_deals`: 회원별 관심 실거래 매물
- `rental_notice_cache`: LH 공고 캐시
- `analysis_snapshot`: 생활권 분석 결과 저장
- `transfers`: 양도 게시글
- `transfer_images`: 양도 게시글 이미지
- `members`: 회원
- `notices`: 공지사항

## 1. 인덱스 튜닝

### 지역 선택 API

대상 API:

- `GET /api/regions/sidos`
- `GET /api/regions/guguns`
- `GET /api/regions/dongs`

추천 인덱스:

```sql
CREATE INDEX idx_dongcodes_sido ON dongcodes (sido_name);
CREATE INDEX idx_dongcodes_sido_gugun ON dongcodes (sido_name, gugun_name);
CREATE INDEX idx_dongcodes_sido_gugun_dong ON dongcodes (sido_name, gugun_name, dong_name);
```

효과:

- 시/도, 구/군, 동 드롭다운 조회 속도 개선
- 지역 조건 검색의 기본 비용 감소

주의:

- `dongcodes`는 데이터가 수천 건 수준이라 지금 당장 병목은 아닐 수 있다.
- 하지만 지역 선택은 자주 호출되므로 인덱스를 걸어도 부담이 거의 없다.

### 아파트 검색

대상 테이블:

- `houseinfos`
- `housedeals`

추천 인덱스:

```sql
CREATE INDEX idx_houseinfos_dong_code ON houseinfos (dong_code);
CREATE INDEX idx_houseinfos_sgg_umd ON houseinfos (sgg_cd, umd_cd);
CREATE INDEX idx_houseinfos_apt_nm ON houseinfos (apt_nm);

CREATE INDEX idx_housedeals_apt_seq ON housedeals (apt_seq);
CREATE INDEX idx_housedeals_date ON housedeals (deal_year, deal_month, deal_day);
CREATE INDEX idx_housedeals_apt_date ON housedeals (apt_seq, deal_year, deal_month);
```

효과:

- `housedeals -> houseinfos -> dongcodes` 조인 최적화
- 최근 거래 조회 개선
- 아파트명 검색 개선
- 특정 아파트 거래 내역 조회 개선

주의:

- `apt_nm LIKE '%keyword%'` 형태는 일반 B-Tree 인덱스를 잘 못 탄다.
- 검색 품질을 높이려면 추후 FULLTEXT 인덱스나 검색 엔진 도입을 고려할 수 있다.

### 양도 게시판

추천 인덱스:

```sql
CREATE INDEX idx_transfers_created_at ON transfers (created_at);
CREATE INDEX idx_transfers_status ON transfers (status);
CREATE INDEX idx_transfers_writer_id ON transfers (writer_id);
CREATE INDEX idx_transfers_status_created_at ON transfers (status, created_at);
```

효과:

- 최신순 목록 조회 개선
- 상태별 필터링 개선
- 회원별 작성글 조회 확장 시 유리

### LH 공고 캐시

추천 인덱스:

```sql
CREATE INDEX idx_rental_notice_cached_at ON rental_notice_cache (cached_at);
CREATE INDEX idx_rental_notice_region_name ON rental_notice_cache (region_name);
CREATE INDEX idx_rental_notice_status ON rental_notice_cache (status);
CREATE INDEX idx_rental_notice_status_cached_at ON rental_notice_cache (status, cached_at);
```

효과:

- 최신 LH 공고 조회 개선
- 지역/상태별 필터링 확장 가능

## 2. View 사용

현재 `HouseDealMapper.xml`은 `housedeals`, `houseinfos`, `dongcodes`를 매번 조인한다.

이 조인을 View로 분리할 수 있다.

### `v_house_deals`

```sql
CREATE VIEW v_house_deals AS
SELECT
    d.no,
    h.apt_seq,
    h.apt_nm,
    dc.sido_name,
    dc.gugun_name,
    dc.dong_name,
    h.umd_nm,
    h.jibun,
    h.road_nm,
    h.build_year,
    h.latitude,
    h.longitude,
    d.apt_dong,
    d.floor,
    d.deal_year,
    d.deal_month,
    d.deal_day,
    d.exclu_use_ar,
    d.deal_amount
FROM housedeals d
JOIN houseinfos h ON d.apt_seq = h.apt_seq
LEFT JOIN dongcodes dc ON CONCAT(h.sgg_cd, h.umd_cd) = dc.dong_code;
```

효과:

- MyBatis SQL 단순화
- 공통 조인 로직 중복 제거
- 통계 쿼리 작성이 쉬워짐

주의:

- MySQL View는 물리적으로 결과를 저장하지 않는다.
- 성능 자체가 무조건 빨라지는 것은 아니다.
- View 위에 조건을 잘 걸어야 내부 테이블 인덱스를 활용할 수 있다.

### 양도글 대표 이미지 View

```sql
CREATE VIEW v_transfer_summary AS
SELECT
    t.transfer_id,
    t.writer_id,
    t.title,
    t.status,
    t.address,
    t.deposit_amount,
    t.monthly_rent_amount,
    t.transfer_fee,
    t.view_count,
    t.created_at,
    MIN(i.image_url) AS thumbnail_url,
    COUNT(i.image_id) AS image_count
FROM transfers t
LEFT JOIN transfer_images i ON t.transfer_id = i.transfer_id
GROUP BY
    t.transfer_id,
    t.writer_id,
    t.title,
    t.status,
    t.address,
    t.deposit_amount,
    t.monthly_rent_amount,
    t.transfer_fee,
    t.view_count,
    t.created_at;
```

효과:

- 양도 게시판 목록에서 이미지 개수와 썸네일을 쉽게 노출
- 상세 API와 목록 API의 데이터 구성을 분리 가능

## 3. 공간 검색과 R-Tree

주소 문자열만으로는 R-Tree나 공간 인덱스를 사용할 수 없다.

공간 검색을 하려면 좌표가 필요하다.

현재 `houseinfos`에는 `latitude`, `longitude`가 문자열로 있다. 공간 검색을 제대로 하려면 `POINT` 컬럼을 추가하는 방향이 좋다.

### 예시 구조

```sql
ALTER TABLE houseinfos
ADD COLUMN location POINT SRID 4326;

UPDATE houseinfos
SET location = ST_SRID(POINT(CAST(longitude AS DECIMAL(10, 7)), CAST(latitude AS DECIMAL(10, 7))), 4326)
WHERE latitude IS NOT NULL
  AND longitude IS NOT NULL
  AND latitude <> ''
  AND longitude <> '';

CREATE SPATIAL INDEX idx_houseinfos_location ON houseinfos (location);
```

활용 가능 기능:

- 지도 화면 영역 안의 매물 조회
- 현재 위치 기준 반경 N미터 내 매물 조회
- 특정 아파트 주변 LH 공고 또는 양도글 조회
- 생활권 분석에서 주변 상권/매물/교통 이벤트 결합

### 반경 검색 예시

```sql
SELECT *
FROM houseinfos
WHERE ST_Distance_Sphere(
    location,
    ST_SRID(POINT(127.0276, 37.4979), 4326)
) <= 1000;
```

주의:

- `ST_Distance_Sphere()`만 쓰면 전체 스캔이 될 수 있다.
- 실무에서는 먼저 bounding box 조건으로 후보를 줄이고, 그다음 정확한 거리 계산을 하는 방식이 좋다.
- Kakao 지도 화면의 bounds를 받아 `latitude`, `longitude` 범위 조건으로 1차 필터링하는 것도 충분히 효과적이다.

## 4. 주소 Geocoding

좌표가 없는 데이터는 Kakao Local API 같은 Geocoding API로 주소를 좌표로 변환할 수 있다.

대상:

- 양도 게시글 주소
- LH 공고 주소
- 아파트 정보 중 좌표 누락 데이터

추천 컬럼:

```sql
ALTER TABLE transfers
ADD COLUMN latitude DECIMAL(10, 7),
ADD COLUMN longitude DECIMAL(10, 7),
ADD COLUMN location POINT SRID 4326;
```

가능한 처리:

- 게시글 생성 시 주소를 좌표로 변환
- 실패하면 배치에서 재시도
- 좌표가 있으면 지도 마커, 주변 검색, 거리 정렬 가능

주의:

- API 호출량 제한을 고려해야 한다.
- 같은 주소는 캐싱해야 한다.
- 주소 정규화가 필요하다.

## 5. Batch

간단한 주기 작업은 `@Scheduled`로 시작하고, 작업 이력/재시도/대량 처리가 필요해지면 Spring Batch로 확장하는 것이 좋다.

### LH 공고 수집 Batch

목표:

- 하루 1회 또는 몇 시간마다 LH Open API 호출
- 신규 공고를 `rental_notice_cache`에 upsert
- 기존 공고 상태 갱신

가능한 기능:

- 신규 공고 감지
- 마감 임박 공고 감지
- 관심 지역 사용자에게 알림 발송

필요한 추가 테이블:

```sql
CREATE TABLE batch_job_logs (
    job_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    processed_count INT,
    error_message VARCHAR(4000)
);
```

### 아파트 실거래 수집 Batch

목표:

- 공공데이터 API에서 월별/지역별 실거래 데이터 수집
- `houseinfos`, `housedeals` upsert

고려사항:

- 지역코드별 반복 호출 필요
- API 호출량 제한
- 이미 저장된 거래 중복 제거 기준 필요
- 원천 API의 거래번호가 안정적이지 않다면 복합키 전략 필요

중복 기준 예시:

- `apt_seq`
- `deal_year`
- `deal_month`
- `deal_day`
- `floor`
- `exclusive_area`
- `deal_amount`

### 법정동 코드 갱신 Batch

목표:

- 월 1회 법정동 코드 파일 또는 API 갱신
- `dongcodes` 반영

주의:

- 기존 `houseinfos`가 참조 중인 `dong_code`를 삭제하면 FK 문제가 생길 수 있다.
- 삭제보다 `active` 컬럼을 두는 방식이 안전하다.

추천 구조:

```sql
ALTER TABLE dongcodes
ADD COLUMN active BOOLEAN DEFAULT TRUE,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

## 6. 메일 알림

Spring Mail을 붙이면 다음 알림이 가능하다.

### 가능한 메일

- 관심 지역 신규 LH 공고
- 관심 공고 마감 임박
- 관심 매물 신규 거래 발생
- 양도글 댓글 알림
- 양도글 상태 변경 알림
- 비밀번호 찾기 인증 메일
- 주간 지역 거래 요약

### 추천 테이블

```sql
CREATE TABLE notification_preferences (
    preference_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    sido_name VARCHAR(30),
    gugun_name VARCHAR(30),
    dong_name VARCHAR(30),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES members (user_id)
);

CREATE TABLE mail_send_logs (
    mail_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50),
    email VARCHAR(255) NOT NULL,
    subject VARCHAR(300) NOT NULL,
    status VARCHAR(30) NOT NULL,
    error_message VARCHAR(4000),
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES members (user_id)
);
```

주의:

- 메일은 반드시 실패 로그를 남겨야 한다.
- 같은 알림이 중복 발송되지 않도록 dedup key가 필요하다.
- Gmail SMTP는 개발용으로 좋지만 운영에서는 SES, SendGrid 같은 서비스가 안정적이다.

## 7. 통계

통계는 실시간 집계와 배치 집계로 나눌 수 있다.

### 실시간 SQL 통계

가능한 통계:

- 지역별 거래 건수
- 지역별 평균 거래가
- 월별 거래량
- 전용면적별 평균 거래가
- 최근 거래 TOP 지역
- LH 공고 지역별 분포
- 양도글 상태별 개수

예시:

```sql
SELECT
    dc.sido_name,
    dc.gugun_name,
    COUNT(*) AS deal_count,
    AVG(CAST(REPLACE(d.deal_amount, ',', '') AS UNSIGNED)) AS avg_deal_amount
FROM housedeals d
JOIN houseinfos h ON d.apt_seq = h.apt_seq
LEFT JOIN dongcodes dc ON CONCAT(h.sgg_cd, h.umd_cd) = dc.dong_code
GROUP BY dc.sido_name, dc.gugun_name;
```

### 배치 집계 테이블

트래픽이 늘어나면 매번 집계하지 말고 통계 테이블에 저장하는 것이 좋다.

```sql
CREATE TABLE region_deal_stats_daily (
    stat_date DATE NOT NULL,
    dong_code VARCHAR(10) NOT NULL,
    deal_count INT NOT NULL,
    avg_deal_amount BIGINT,
    min_deal_amount BIGINT,
    max_deal_amount BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (stat_date, dong_code),
    FOREIGN KEY (dong_code) REFERENCES dongcodes (dong_code)
);
```

활용:

- 지역별 대시보드
- 가격 추세 그래프
- 거래량 변화율
- 관심 지역 리포트

## 8. 캐싱

자주 바뀌지 않는 데이터는 캐시가 좋다.

대상:

- 시/도 목록
- 구/군 목록
- 동 목록
- LH 공고 최신 목록
- 생활권 분석 결과

방식:

- Spring Cache + Caffeine
- Redis
- DB 캐시 테이블

추천 시작점:

- 로컬 단일 서버면 Caffeine
- 여러 서버 또는 세션/알림 큐까지 고려하면 Redis

## 9. 우선순위

### 1단계

- 핵심 인덱스 추가
- `v_house_deals` View 추가
- 지역 API 캐싱
- LH 공고 수집 스케줄러

### 2단계

- 좌표 없는 데이터 Geocoding
- `POINT` 컬럼과 공간 인덱스 추가
- 지도 bounds 기반 매물 조회 API
- 관심 지역 신규 공고 알림

### 3단계

- 통계 집계 테이블
- 주간 요약 메일
- 배치 실행 이력 관리
- 실패 재시도 정책

## 10. 결론

가장 먼저 할 일은 인덱스와 View다.

그다음 좌표 기반 검색을 위해 `POINT` 컬럼과 공간 인덱스를 추가하면 지도 기능과 생활권 분석 품질이 좋아진다.

Batch는 LH 공고 수집부터 시작하는 것이 좋다. 이미 `rental_notice_cache`가 있으므로 도입 비용이 낮고, 신규 공고 알림이나 마감 임박 알림으로 확장하기 쉽다.

통계는 처음에는 SQL로 바로 집계하고, 데이터가 많아지면 일/월 단위 집계 테이블로 옮기는 방식이 가장 안정적이다.
