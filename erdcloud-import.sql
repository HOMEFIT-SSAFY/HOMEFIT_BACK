CREATE TABLE members (
    user_id VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (user_id)
);

CREATE TABLE dongcodes (
    dong_code VARCHAR(10) NOT NULL,
    sido_name VARCHAR(30),
    gugun_name VARCHAR(30),
    dong_name VARCHAR(30),
    PRIMARY KEY (dong_code)
);

CREATE TABLE commercial_places (
    place_id BIGINT NOT NULL,
    place_name VARCHAR(200) NOT NULL,
    category_large VARCHAR(100),
    category_middle VARCHAR(100),
    category_small VARCHAR(100),
    address VARCHAR(300),
    road_address VARCHAR(300),
    latitude DOUBLE,
    longitude DOUBLE,
    source_api VARCHAR(80),
    raw_json VARCHAR(4000),
    cached_at TIMESTAMP,
    PRIMARY KEY (place_id)
);

CREATE TABLE property_deals (
    deal_id BIGINT NOT NULL,
    dong_code VARCHAR(10),
    property_type VARCHAR(30) NOT NULL,
    trade_type VARCHAR(20) NOT NULL,
    rent_type VARCHAR(20),
    building_name VARCHAR(120),
    building_dong VARCHAR(40),
    address VARCHAR(300),
    road_address VARCHAR(300),
    jibun VARCHAR(30),
    floor VARCHAR(20),
    deal_year INT NOT NULL,
    deal_month INT NOT NULL,
    deal_day INT,
    exclusive_area DECIMAL(10, 2),
    deal_amount BIGINT,
    deposit_amount BIGINT,
    monthly_rent_amount BIGINT,
    build_year INT,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    source_api VARCHAR(80),
    source_deal_no VARCHAR(80),
    created_at TIMESTAMP,
    PRIMARY KEY (deal_id),
    FOREIGN KEY (dong_code) REFERENCES dongcodes (dong_code)
);

CREATE TABLE rental_notices (
    rental_notice_id VARCHAR(40) NOT NULL,
    dong_code VARCHAR(10),
    title VARCHAR(300) NOT NULL,
    region_name VARCHAR(80),
    notice_type VARCHAR(80),
    detail_type VARCHAR(80),
    status VARCHAR(40),
    notice_date DATE,
    close_date DATE,
    apply_start_date DATE,
    apply_end_date DATE,
    detail_url VARCHAR(1000),
    contact VARCHAR(100),
    contract_address VARCHAR(300),
    contract_detail_address VARCHAR(300),
    supply_info VARCHAR(4000),
    qualification_info VARCHAR(4000),
    ccr_cnnt_sys_ds_cd VARCHAR(10),
    upp_ais_tp_cd VARCHAR(10),
    ais_tp_cd VARCHAR(10),
    spl_inf_tp_cd VARCHAR(10),
    raw_json VARCHAR(4000),
    source VARCHAR(20),
    cached_at TIMESTAMP,
    PRIMARY KEY (rental_notice_id),
    FOREIGN KEY (dong_code) REFERENCES dongcodes (dong_code)
);

CREATE TABLE transfer_posts (
    transfer_id BIGINT NOT NULL,
    writer_id VARCHAR(50) NOT NULL,
    dong_code VARCHAR(10),
    title VARCHAR(200) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    address VARCHAR(300),
    detail_address VARCHAR(300),
    floor VARCHAR(20),
    exclusive_area DECIMAL(10, 2),
    deposit_amount BIGINT,
    monthly_rent_amount BIGINT,
    maintenance_fee BIGINT,
    transfer_fee BIGINT,
    contract_end_date DATE,
    move_in_date DATE,
    contact_phone VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    view_count INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (transfer_id),
    FOREIGN KEY (writer_id) REFERENCES members (user_id),
    FOREIGN KEY (dong_code) REFERENCES dongcodes (dong_code)
);

CREATE TABLE transfer_images (
    image_id BIGINT NOT NULL,
    transfer_id BIGINT NOT NULL,
    image_url VARCHAR(1000),
    sort_order INT,
    created_at TIMESTAMP,
    PRIMARY KEY (image_id),
    FOREIGN KEY (transfer_id) REFERENCES transfer_posts (transfer_id)
);

CREATE TABLE transfer_comments (
    comment_id BIGINT NOT NULL,
    transfer_id BIGINT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    content VARCHAR(1000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (comment_id),
    FOREIGN KEY (transfer_id) REFERENCES transfer_posts (transfer_id),
    FOREIGN KEY (user_id) REFERENCES members (user_id)
);

CREATE TABLE notices (
    notice_id INT NOT NULL,
    writer_id VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    popup_enabled TINYINT,
    pinned TINYINT,
    visible TINYINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (notice_id),
    FOREIGN KEY (writer_id) REFERENCES members (user_id)
);

CREATE TABLE favorite_deals (
    user_id VARCHAR(50) NOT NULL,
    deal_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    PRIMARY KEY (user_id, deal_id),
    FOREIGN KEY (user_id) REFERENCES members (user_id),
    FOREIGN KEY (deal_id) REFERENCES property_deals (deal_id)
);

CREATE TABLE favorite_transfer_posts (
    user_id VARCHAR(50) NOT NULL,
    transfer_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    PRIMARY KEY (user_id, transfer_id),
    FOREIGN KEY (user_id) REFERENCES members (user_id),
    FOREIGN KEY (transfer_id) REFERENCES transfer_posts (transfer_id)
);

CREATE TABLE favorite_rental_notices (
    user_id VARCHAR(50) NOT NULL,
    rental_notice_id VARCHAR(40) NOT NULL,
    created_at TIMESTAMP,
    PRIMARY KEY (user_id, rental_notice_id),
    FOREIGN KEY (user_id) REFERENCES members (user_id),
    FOREIGN KEY (rental_notice_id) REFERENCES rental_notices (rental_notice_id)
);

CREATE TABLE rental_notification_logs (
    notification_id BIGINT NOT NULL,
    rental_notice_id VARCHAR(40) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    notification_type VARCHAR(20),
    title VARCHAR(200),
    content VARCHAR(4000),
    sent_at TIMESTAMP,
    PRIMARY KEY (notification_id),
    FOREIGN KEY (rental_notice_id) REFERENCES rental_notices (rental_notice_id),
    FOREIGN KEY (user_id) REFERENCES members (user_id)
);

CREATE TABLE loan_products (
    product_code VARCHAR(80) NOT NULL,
    loan_type VARCHAR(30) NOT NULL,
    company_code VARCHAR(30),
    company_name VARCHAR(120) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    join_way VARCHAR(300),
    loan_incidental_expense VARCHAR(4000),
    early_repayment_fee VARCHAR(4000),
    delinquency_rate VARCHAR(4000),
    loan_limit VARCHAR(4000),
    disclosure_start_day VARCHAR(20),
    disclosure_end_day VARCHAR(20),
    submitted_at VARCHAR(30),
    source VARCHAR(20),
    cached_at TIMESTAMP,
    PRIMARY KEY (product_code)
);

CREATE TABLE loan_rate_options (
    rate_option_id BIGINT NOT NULL,
    product_code VARCHAR(80) NOT NULL,
    repayment_type_code VARCHAR(20),
    repayment_type_name VARCHAR(80),
    rate_type_code VARCHAR(20),
    rate_type_name VARCHAR(80),
    mortgage_type_code VARCHAR(20),
    mortgage_type_name VARCHAR(80),
    rate_min DECIMAL(5, 2),
    rate_max DECIMAL(5, 2),
    rate_avg DECIMAL(5, 2),
    cached_at TIMESTAMP,
    PRIMARY KEY (rate_option_id),
    FOREIGN KEY (product_code) REFERENCES loan_products (product_code)
);

CREATE TABLE analysis_snapshot (
    analysis_id BIGINT NOT NULL,
    user_id VARCHAR(50),
    dong_code VARCHAR(10),
    place_id BIGINT NOT NULL,
    label VARCHAR(120) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    radius_m INT NOT NULL,
    commercial_count INT NOT NULL,
    traffic_event_count INT NOT NULL,
    score INT NOT NULL,
    risk_level VARCHAR(30) NOT NULL,
    commercial_raw_json VARCHAR(4000),
    traffic_raw_json VARCHAR(4000),
    source VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    PRIMARY KEY (analysis_id),
    FOREIGN KEY (user_id) REFERENCES members (user_id),
    FOREIGN KEY (dong_code) REFERENCES dongcodes (dong_code),
    FOREIGN KEY (place_id) REFERENCES commercial_places (place_id)
);
