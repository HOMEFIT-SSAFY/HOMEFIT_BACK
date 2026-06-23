CREATE TABLE IF NOT EXISTS members (
    user_id VARCHAR(50) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS dongcodes (
    dong_code VARCHAR(10) PRIMARY KEY,
    sido_name VARCHAR(30),
    gugun_name VARCHAR(30),
    dong_name VARCHAR(30),
    INDEX idx_dongcodes_sido_gugun_dong (sido_name, gugun_name, dong_name),
    INDEX idx_dongcodes_region_code (sido_name, gugun_name, dong_name, dong_code)
);

CREATE TABLE IF NOT EXISTS houseinfos (
    apt_seq VARCHAR(40) PRIMARY KEY,
    sgg_cd VARCHAR(5),
    umd_cd VARCHAR(5),
    umd_nm VARCHAR(80),
    jibun VARCHAR(30),
    road_nm_sgg_cd VARCHAR(5),
    road_nm VARCHAR(120),
    road_nm_bonbun VARCHAR(20),
    road_nm_bubun VARCHAR(20),
    apt_nm VARCHAR(120),
    build_year INT,
    latitude VARCHAR(30),
    longitude VARCHAR(30),
    INDEX idx_houseinfos_region_seq (sgg_cd, umd_cd, apt_seq),
    INDEX idx_houseinfos_apt_nm (apt_nm)
);

CREATE TABLE IF NOT EXISTS housedeals (
    no INT PRIMARY KEY,
    apt_seq VARCHAR(40),
    apt_dong VARCHAR(40),
    floor VARCHAR(20),
    deal_year INT,
    deal_month INT,
    deal_day INT,
    exclu_use_ar DECIMAL(10, 2),
    deal_amount VARCHAR(40),
    INDEX idx_housedeals_apt_date_no (apt_seq, deal_year DESC, deal_month DESC, deal_day DESC, no DESC),
    INDEX idx_housedeals_date_no (deal_year DESC, deal_month DESC, deal_day DESC, no DESC),
    FOREIGN KEY (apt_seq) REFERENCES houseinfos (apt_seq)
);

CREATE TABLE IF NOT EXISTS favorite_deals (
    user_id VARCHAR(50) NOT NULL,
    deal_no INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, deal_no),
    FOREIGN KEY (user_id) REFERENCES members (user_id),
    FOREIGN KEY (deal_no) REFERENCES housedeals (no)
);

CREATE TABLE IF NOT EXISTS notices (
    notice_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    writer VARCHAR(50) NOT NULL,
    popup_enabled BOOLEAN DEFAULT FALSE,
    pinned BOOLEAN DEFAULT FALSE,
    visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (writer) REFERENCES members (user_id)
);

CREATE TABLE IF NOT EXISTS transfers (
    transfer_id INT AUTO_INCREMENT PRIMARY KEY,
    writer_id VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255),
    floor VARCHAR(30),
    exclusive_area DECIMAL(10, 2),
    deposit_amount INT NOT NULL DEFAULT 0,
    monthly_rent_amount INT NOT NULL DEFAULT 0,
    maintenance_fee INT NOT NULL DEFAULT 0,
    transfer_fee INT NOT NULL DEFAULT 0,
    contract_end_date DATE,
    move_in_date DATE,
    contact_phone VARCHAR(50),
    view_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (writer_id) REFERENCES members (user_id)
);

CREATE TABLE IF NOT EXISTS transfer_images (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    transfer_id INT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transfer_id) REFERENCES transfers (transfer_id)
);

CREATE TABLE IF NOT EXISTS favorite_transfers (
    user_id VARCHAR(50) NOT NULL,
    transfer_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, transfer_id),
    INDEX idx_favorite_transfers_transfer (transfer_id),
    FOREIGN KEY (user_id) REFERENCES members (user_id),
    FOREIGN KEY (transfer_id) REFERENCES transfers (transfer_id)
);

CREATE TABLE IF NOT EXISTS transfer_comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    transfer_id INT NOT NULL,
    writer_id VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transfer_comments_transfer (transfer_id, created_at, comment_id),
    INDEX idx_transfer_comments_writer (writer_id),
    FOREIGN KEY (transfer_id) REFERENCES transfers (transfer_id) ON DELETE CASCADE,
    FOREIGN KEY (writer_id) REFERENCES members (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS rental_notice_cache (
    notice_id VARCHAR(40) PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    region_name VARCHAR(80),
    notice_type VARCHAR(80),
    detail_type VARCHAR(80),
    status VARCHAR(40),
    notice_date VARCHAR(20),
    close_date VARCHAR(20),
    detail_url VARCHAR(1000),
    ccr_cnnt_sys_ds_cd VARCHAR(10),
    upp_ais_tp_cd VARCHAR(10),
    ais_tp_cd VARCHAR(10),
    spl_inf_tp_cd VARCHAR(10),
    source VARCHAR(20),
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lh_notice_details (
    notice_id VARCHAR(40) PRIMARY KEY,
    contract_address VARCHAR(300),
    contract_detail_address VARCHAR(300),
    apply_start_date VARCHAR(20),
    apply_end_date VARCHAR(20),
    contact VARCHAR(100),
    raw_json TEXT,
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notice_id) REFERENCES rental_notice_cache (notice_id)
);

CREATE TABLE IF NOT EXISTS lh_notice_supplies (
    supply_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_id VARCHAR(40) NOT NULL,
    `usage` VARCHAR(120),
    address VARCHAR(300),
    area VARCHAR(80),
    expected_amount VARCHAR(300),
    house_type VARCHAR(120),
    household_count VARCHAR(80),
    raw_json TEXT,
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (notice_id) REFERENCES rental_notice_cache (notice_id)
);

CREATE TABLE IF NOT EXISTS openapi_sync_logs (
    sync_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    api_name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    fetched_count INT DEFAULT 0,
    saved_count INT DEFAULT 0,
    error_message TEXT
);

CREATE TABLE IF NOT EXISTS loan_products (
    product_code VARCHAR(80) PRIMARY KEY,
    loan_type VARCHAR(30) NOT NULL,
    company_code VARCHAR(30),
    company_name VARCHAR(120) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    join_way VARCHAR(300),
    loan_incidental_expense TEXT,
    early_repayment_fee TEXT,
    delinquency_rate TEXT,
    loan_limit TEXT,
    disclosure_start_day VARCHAR(20),
    disclosure_end_day VARCHAR(20),
    submitted_at VARCHAR(30),
    source VARCHAR(20) DEFAULT 'api',
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS loan_rate_options (
    rate_option_id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_code) REFERENCES loan_products (product_code)
);

CREATE TABLE IF NOT EXISTS analysis_snapshot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    label VARCHAR(120) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    radius_m INT NOT NULL,
    commercial_count INT NOT NULL,
    traffic_event_count INT NOT NULL,
    score INT NOT NULL,
    risk_level VARCHAR(30) NOT NULL,
    source VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
