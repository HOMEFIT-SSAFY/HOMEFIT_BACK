# Schema, View, and Index Performance Design

## Goal

현재 애플리케이션이 실제 사용하는 MySQL 스키마를 `src/main/resources/schema.sql`에 맞추고, 실거래 지도와 LH 공공임대 공고 상세 조회의 지연을 최소한의 View·Index·캐시 우선 조회로 줄인다.

## Current Evidence

- `housedeals`: 6,988,921행
- `houseinfos`: 41,055행
- 날짜순 실거래 500건 조회: 약 12.5초
- 최근 실거래 20건 조회: 약 4.4초
- 실행계획에서 `housedeals` 전체 스캔 후 약 699만 행 정렬 발생
- 현재 운영 DB의 `housedeals`에는 기본키와 `apt_seq` 인덱스만 존재
- LH 공고 목록은 약 0.2초로 병목이 크지 않음
- LH 상세 캐시 테이블은 존재하지만 서비스가 매번 외부 상세·공급 API를 호출함

## Scope

### Included

- `src/main/resources/schema.sql`을 현재 MyBatis 매퍼가 사용하는 테이블 기준으로 정리
- 실거래 조회용 `v_house_deals` View
- LH 공고 상세 조회용 `v_rental_notice_details` View
- 실거래 정렬·지역 검색과 LH 상세/공급 조회에 필요한 인덱스
- 실거래 매퍼가 View를 사용하도록 변경
- LH 상세 조회가 DB 캐시를 먼저 사용하고, 캐시가 없을 때만 외부 API를 호출한 뒤 저장하도록 변경
- 변경 전후 `EXPLAIN ANALYZE`와 HTTP 응답시간 비교

### Excluded

- Materialized View 또는 별도 집계 테이블
- Redis 등 외부 캐시
- LH 목록 전체의 TTL·만료 정책
- 모든 테이블에 대한 추측성 인덱스
- `app-runtime-schema.sql`, `erdcloud-import.sql` 변경

## Schema Design

### `v_house_deals`

`housedeals`, `houseinfos`, `dongcodes`의 반복 조인을 한 곳에 모은다. View는 기존 `HouseDeal` DTO가 요구하는 열만 노출한다. MySQL 일반 View는 데이터를 복제하지 않으며, 실제 성능은 기반 테이블 인덱스가 담당한다.

핵심 인덱스는 다음으로 제한한다.

- `housedeals(deal_year DESC, deal_month DESC, deal_day DESC, no DESC)`: 최신순 LIMIT 조회
- `housedeals(apt_seq)`: 아파트 정보 조인
- `houseinfos(sgg_cd, umd_cd, apt_seq)`: 지역 조건 후 거래 조인
- `dongcodes(sido_name, gugun_name, dong_name, dong_code)`: 단계별 지역 선택과 지역 필터
- `lh_notice_supplies(notice_id, supply_id)`: 공고별 공급 목록 정렬 조회

`apt_nm LIKE '%검색어%'`는 선행 와일드카드 때문에 일반 B-Tree 인덱스 효과가 없으므로 이번 범위에서 검색용 인덱스를 추가하지 않는다.

### `v_rental_notice_details`

`rental_notice_cache`와 `lh_notice_details`를 `notice_id`로 LEFT JOIN한다. 공고 기본정보와 신청 일정·계약 장소·문의 정보를 한 번에 읽는다. 공급 목록은 1:N 관계이므로 View에서 문자열 집계하지 않고 `lh_notice_supplies`를 별도 조회한다.

## Application Data Flow

### Real-estate deals

1. 검색/최근/단건 매퍼는 `v_house_deals`에서 조회한다.
2. 최신순 조회는 날짜 복합 인덱스에서 필요한 건수만 읽는다.
3. 지역 조건은 기반 테이블 인덱스를 통해 필터링한다.

### LH notice detail

1. `v_rental_notice_details`에서 공고와 상세 캐시를 조회한다.
2. 상세 캐시와 공급 캐시가 모두 있으면 즉시 반환한다.
3. 하나라도 없으면 필요한 외부 API만 호출한다.
4. 호출 결과를 기존 `lh_notice_details`, `lh_notice_supplies`에 저장하고 응답한다.
5. 외부 API가 실패하면 사용 가능한 기존 캐시는 유지해 반환한다.

LH 목록 조회는 실측 0.2초 수준이고 최신성 요구가 있으므로 현재 외부 API 흐름을 유지한다.

## Existing Database Application

`CREATE TABLE IF NOT EXISTS`는 이미 존재하는 테이블에 누락 인덱스를 추가하지 않는다. 따라서:

- `schema.sql`은 새 DB 생성 시 완전한 정의를 제공한다.
- 현재 개발 DB에는 누락 여부를 확인한 후 필요한 View와 Index DDL을 한 번 적용한다.
- 중복 인덱스를 만들지 않도록 `information_schema.statistics`와 `information_schema.views`를 확인한다.

## Verification

- 스키마 SQL 문법 및 View 열 구조 검증
- MyBatis 매퍼 통합 테스트로 DTO 매핑 검증
- LH 서비스 테스트로 캐시 hit 시 외부 API 미호출, cache miss 시 API 호출·저장 검증
- `EXPLAIN ANALYZE`에서 699만 행 전체 스캔·정렬 제거 확인
- `/api/houses?limit=500`, `/api/houses/recent?limit=20`, LH 상세 API를 각각 반복 호출해 변경 전후 시간 비교
- 기존 검색 조건과 응답 JSON 필드가 유지되는지 확인

## Success Criteria

- 실거래 500건과 최근 20건 조회가 전체 테이블 정렬 없이 인덱스를 사용한다.
- 실거래 API 응답시간이 기존 12.5초/4.4초보다 명확히 감소한다.
- 캐시된 LH 상세 재조회에서는 외부 상세·공급 API를 호출하지 않는다.
- View와 Index는 위 조회 경로에 필요한 것만 존재한다.
