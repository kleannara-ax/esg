-- ============================================================
-- ESG 관리시스템 DDL
-- DB     : esg_db
-- Charset: utf8mb4
-- ============================================================

USE esg_db;

-- ── module-user ──────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS MOD_USER_ACCOUNT
(
    user_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '사용자 ID',
    login_id   VARCHAR(50)  NOT NULL COMMENT '로그인 ID',
    password   VARCHAR(255) NOT NULL COMMENT '비밀번호(BCrypt)',
    user_name  VARCHAR(100) NOT NULL COMMENT '사용자명',
    email      VARCHAR(200) NULL     COMMENT '이메일',
    role       VARCHAR(20)  NOT NULL COMMENT '역할 (ROLE_ADMIN/ROLE_MANAGER/ROLE_USER)',
    active_yn  TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '활성화 여부',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '등록일시',
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    created_by VARCHAR(50)  NULL     COMMENT '등록자',
    updated_by VARCHAR(50)  NULL     COMMENT '수정자',
    PRIMARY KEY (user_id),
    UNIQUE KEY UK_USER_LOGIN_ID (login_id),
    UNIQUE KEY UK_USER_EMAIL    (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '사용자 계정';

-- ── module-esg ───────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS MOD_ESG_INDICATOR
(
    indicator_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ESG 지표 ID',
    category       VARCHAR(1)   NOT NULL COMMENT 'ESG 카테고리 (E/S/G)',
    indicator_code VARCHAR(50)  NOT NULL COMMENT '지표 코드',
    indicator_name VARCHAR(200) NOT NULL COMMENT '지표명',
    description    TEXT         NULL     COMMENT '지표 설명',
    unit           VARCHAR(50)  NULL     COMMENT '측정 단위',
    active_yn      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '활성화 여부',
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '등록일시',
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    created_by     VARCHAR(50)  NULL     COMMENT '등록자',
    updated_by     VARCHAR(50)  NULL     COMMENT '수정자',
    PRIMARY KEY (indicator_id),
    UNIQUE KEY UK_ESG_INDICATOR_CODE (indicator_code),
    INDEX IDX_ESG_INDICATOR_CATEGORY (category)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'ESG 지표 마스터';

CREATE TABLE IF NOT EXISTS MOD_ESG_DATA_ENTRY
(
    entry_id       BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'ESG 데이터 입력 ID',
    indicator_id   BIGINT         NOT NULL COMMENT 'ESG 지표 ID (FK)',
    target_year    INT            NOT NULL COMMENT '대상 연도',
    target_month   INT            NOT NULL COMMENT '대상 월',
    actual_value   DECIMAL(20, 4) NULL     COMMENT '실적값',
    target_value   DECIMAL(20, 4) NULL     COMMENT '목표값',
    remark         TEXT           NULL     COMMENT '비고',
    entry_status   VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT '데이터 상태 (DRAFT/SUBMITTED/APPROVED/REJECTED)',
    created_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '등록일시',
    updated_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    created_by     VARCHAR(50)    NULL     COMMENT '등록자',
    updated_by     VARCHAR(50)    NULL     COMMENT '수정자',
    PRIMARY KEY (entry_id),
    UNIQUE KEY UK_ESG_DATA_ENTRY (indicator_id, target_year, target_month),
    INDEX IDX_ESG_DATA_YEAR_MONTH (target_year, target_month),
    INDEX IDX_ESG_DATA_STATUS (entry_status),
    CONSTRAINT FK_ESG_DATA_INDICATOR
        FOREIGN KEY (indicator_id) REFERENCES MOD_ESG_INDICATOR (indicator_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'ESG 데이터 입력';
