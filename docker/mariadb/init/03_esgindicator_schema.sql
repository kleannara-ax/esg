-- ============================================================
-- module-esgindicator DDL
-- DB Table Prefix: MOD_ESGINDICATOR_
-- charset: utf8mb4 / collate: utf8mb4_unicode_ci
-- ============================================================

USE esg_db;

-- ────────────────────────────────────────────────────────────
-- 1. 부서 마스터
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS MOD_ESGINDICATOR_DEPT (
    dept_id     BIGINT          NOT NULL AUTO_INCREMENT COMMENT '부서ID',
    dept_code   VARCHAR(30)     NOT NULL                COMMENT '부서코드',
    dept_name   VARCHAR(100)    NOT NULL                COMMENT '부서명',
    use_yn      TINYINT(1)      NOT NULL DEFAULT 1      COMMENT '사용여부(1:Y,0:N)',
    sort_order  INT             NOT NULL DEFAULT 0      COMMENT '정렬순서',
    created_at  DATETIME(6)     NOT NULL                COMMENT '등록일시',
    updated_at  DATETIME(6)     NOT NULL                COMMENT '수정일시',
    created_by  VARCHAR(50)                             COMMENT '등록자',
    updated_by  VARCHAR(50)                             COMMENT '수정자',
    CONSTRAINT PK_ESI_DEPT      PRIMARY KEY (dept_id),
    CONSTRAINT UK_ESI_DEPT_CODE UNIQUE (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='ESG 지표 담당부서 마스터';

-- ────────────────────────────────────────────────────────────
-- 2. 지표 마스터
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS MOD_ESGINDICATOR_MASTER (
    indicator_id    BIGINT          NOT NULL AUTO_INCREMENT COMMENT '지표ID',
    esg_category    CHAR(1)         NOT NULL                COMMENT 'ESG구분(E/S/G)',
    dept_id         BIGINT                                  COMMENT '담당부서ID (FK)',
    title           VARCHAR(200)                            COMMENT '제목',
    major_category  VARCHAR(200)                            COMMENT '표 대제목',
    mid_category    VARCHAR(200)                            COMMENT '표 중제목',
    minor_category  VARCHAR(300)    NOT NULL                COMMENT '지표명(표 소제목)',
    unit            VARCHAR(50)                             COMMENT '단위',
    excel_row_num   INT                                     COMMENT '엑셀 행번호',
    sort_order      INT             NOT NULL DEFAULT 0      COMMENT '표시순서',
    use_yn          TINYINT(1)      NOT NULL DEFAULT 1      COMMENT '사용여부',
    remark          TEXT                                    COMMENT '비고',
    created_at      DATETIME(6)     NOT NULL                COMMENT '등록일시',
    updated_at      DATETIME(6)     NOT NULL                COMMENT '수정일시',
    created_by      VARCHAR(50)                             COMMENT '등록자',
    updated_by      VARCHAR(50)                             COMMENT '수정자',
    CONSTRAINT PK_ESI_MASTER PRIMARY KEY (indicator_id),
    CONSTRAINT FK_ESI_MASTER_DEPT FOREIGN KEY (dept_id)
        REFERENCES MOD_ESGINDICATOR_DEPT (dept_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX IDX_ESI_MASTER_CATEGORY (esg_category),
    INDEX IDX_ESI_MASTER_DEPT     (dept_id),
    INDEX IDX_ESI_MASTER_SORT     (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='ESG 지표 마스터';

-- ────────────────────────────────────────────────────────────
-- 3. 연도별 지표값
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS MOD_ESGINDICATOR_VALUE (
    value_id        BIGINT          NOT NULL AUTO_INCREMENT COMMENT '지표값ID',
    indicator_id    BIGINT          NOT NULL                COMMENT '지표ID (FK)',
    dept_id         BIGINT          NOT NULL                COMMENT '부서ID (FK)',
    base_year       INT             NOT NULL                COMMENT '기준연도',
    indicator_value DECIMAL(20,6)                           COMMENT '지표값',
    formula_remark  TEXT                                    COMMENT '산식/비고',
    input_status    VARCHAR(20)     NOT NULL DEFAULT 'NOT_STARTED'
                                                            COMMENT '입력상태(NOT_STARTED/IN_PROGRESS/SUBMITTED/CONFIRMED)',
    confirmed_yn    TINYINT(1)      NOT NULL DEFAULT 0      COMMENT '최종확정여부',
    confirm_remark  VARCHAR(500)                            COMMENT '확정비고',
    created_at      DATETIME(6)     NOT NULL                COMMENT '등록일시',
    updated_at      DATETIME(6)     NOT NULL                COMMENT '수정일시',
    created_by      VARCHAR(50)                             COMMENT '등록자',
    updated_by      VARCHAR(50)                             COMMENT '수정자',
    CONSTRAINT PK_ESI_VALUE     PRIMARY KEY (value_id),
    CONSTRAINT UK_ESI_VALUE     UNIQUE (indicator_id, dept_id, base_year),
    CONSTRAINT FK_ESI_VALUE_IND FOREIGN KEY (indicator_id)
        REFERENCES MOD_ESGINDICATOR_MASTER (indicator_id),
    CONSTRAINT FK_ESI_VALUE_DEP FOREIGN KEY (dept_id)
        REFERENCES MOD_ESGINDICATOR_DEPT (dept_id),
    INDEX IDX_ESI_VALUE_YEAR  (base_year),
    INDEX IDX_ESI_VALUE_DEPT  (dept_id),
    INDEX IDX_ESI_VALUE_INDIC (indicator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='ESG 연도별 지표값';

-- ────────────────────────────────────────────────────────────
-- 4. 입력/수정 이력
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS MOD_ESGINDICATOR_HISTORY (
    history_id      BIGINT          NOT NULL AUTO_INCREMENT COMMENT '이력ID',
    value_id        BIGINT          NOT NULL                COMMENT '지표값ID (FK)',
    change_type     VARCHAR(20)     NOT NULL                COMMENT '변경유형(CREATE/UPDATE/SUBMIT/CONFIRM/CANCEL)',
    before_value    DECIMAL(20,6)                           COMMENT '변경 전 값',
    after_value     DECIMAL(20,6)                           COMMENT '변경 후 값',
    before_status   VARCHAR(20)                             COMMENT '변경 전 상태',
    after_status    VARCHAR(20)                             COMMENT '변경 후 상태',
    change_remark   VARCHAR(500)                            COMMENT '변경 비고',
    changed_by      VARCHAR(50)     NOT NULL                COMMENT '변경자',
    changed_at      DATETIME(6)     NOT NULL                COMMENT '변경일시',
    CONSTRAINT PK_ESI_HIST    PRIMARY KEY (history_id),
    CONSTRAINT FK_ESI_HIST_V  FOREIGN KEY (value_id)
        REFERENCES MOD_ESGINDICATOR_VALUE (value_id),
    INDEX IDX_ESI_HIST_VALUE (value_id),
    INDEX IDX_ESI_HIST_DATE  (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='ESG 지표값 변경 이력';
