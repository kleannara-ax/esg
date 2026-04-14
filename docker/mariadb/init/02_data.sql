-- ============================================================
-- ESG 관리시스템 초기 데이터 (개발/테스트용)
-- ============================================================

USE esg_db;

-- ── 사용자 (BCrypt: 'password123') ───────────────────────────────────────────
INSERT INTO MOD_USER_ACCOUNT (login_id, password, user_name, email, role, active_yn, created_by, updated_by)
VALUES
    ('admin',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKnS7Z.jzYbLroiH/V/OJ7TfS9.q', '시스템관리자', 'admin@company.com',   'ROLE_ADMIN',   1, 'SYSTEM', 'SYSTEM'),
    ('manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKnS7Z.jzYbLroiH/V/OJ7TfS9.q', '업무담당자',   'manager@company.com', 'ROLE_MANAGER', 1, 'SYSTEM', 'SYSTEM'),
    ('user01',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKnS7Z.jzYbLroiH/V/OJ7TfS9.q', '일반사용자1',  'user01@company.com',  'ROLE_USER',    1, 'SYSTEM', 'SYSTEM');

-- ── ESG 지표 마스터 ──────────────────────────────────────────────────────────
INSERT INTO MOD_ESG_INDICATOR (category, indicator_code, indicator_name, description, unit, active_yn, created_by, updated_by)
VALUES
    -- 환경 (E)
    ('E', 'E_GHG_SCOPE1',   '온실가스 배출량 Scope 1', '직접 온실가스 배출량 (연료 연소 등)',           'tCO2eq', 1, 'SYSTEM', 'SYSTEM'),
    ('E', 'E_GHG_SCOPE2',   '온실가스 배출량 Scope 2', '간접 온실가스 배출량 (전력 구매 등)',           'tCO2eq', 1, 'SYSTEM', 'SYSTEM'),
    ('E', 'E_ENERGY_TOTAL',  '총 에너지 사용량',        '전력 및 열 에너지 총 사용량',                  'GJ',     1, 'SYSTEM', 'SYSTEM'),
    ('E', 'E_WATER_TOTAL',   '총 용수 사용량',          '산업용수, 상수도 등 총 용수 사용량',           'm³',     1, 'SYSTEM', 'SYSTEM'),
    ('E', 'E_WASTE_TOTAL',   '총 폐기물 발생량',        '사업장에서 발생하는 총 폐기물',                't',      1, 'SYSTEM', 'SYSTEM'),
    -- 사회 (S)
    ('S', 'S_EMPLOYEE_TOTAL','임직원 수',               '정규직 및 계약직 임직원 총 수',                '명',     1, 'SYSTEM', 'SYSTEM'),
    ('S', 'S_FEMALE_RATIO',  '여성 임직원 비율',        '전체 임직원 중 여성 임직원 비율',              '%',      1, 'SYSTEM', 'SYSTEM'),
    ('S', 'S_SAFETY_ACCIDENT','산업재해 건수',          '업무상 재해로 인한 사고 건수',                 '건',     1, 'SYSTEM', 'SYSTEM'),
    ('S', 'S_TRAINING_HOURS','1인당 교육 시간',         '임직원 1인당 평균 교육 시간',                  '시간',   1, 'SYSTEM', 'SYSTEM'),
    -- 지배구조 (G)
    ('G', 'G_BOARD_SIZE',    '이사회 구성원 수',        '사내외 이사 총 인원',                         '명',     1, 'SYSTEM', 'SYSTEM'),
    ('G', 'G_INDEPENDENT_RATIO','사외이사 비율',        '이사회 중 사외이사 비율',                     '%',      1, 'SYSTEM', 'SYSTEM'),
    ('G', 'G_FEMALE_BOARD',  '여성 이사 비율',          '이사회 중 여성 이사 비율',                    '%',      1, 'SYSTEM', 'SYSTEM');

-- ── ESG 데이터 입력 (2024년 1~3월 샘플) ─────────────────────────────────────
INSERT INTO MOD_ESG_DATA_ENTRY (indicator_id, target_year, target_month, actual_value, target_value, entry_status, created_by, updated_by)
SELECT i.indicator_id, 2024, 1,
       CASE i.indicator_code
           WHEN 'E_GHG_SCOPE1'  THEN 1250.5
           WHEN 'E_GHG_SCOPE2'  THEN 3800.2
           WHEN 'E_ENERGY_TOTAL' THEN 45000.0
           WHEN 'E_WATER_TOTAL'  THEN 12000.0
           WHEN 'E_WASTE_TOTAL'  THEN 85.3
           WHEN 'S_EMPLOYEE_TOTAL' THEN 2350
           WHEN 'S_FEMALE_RATIO'   THEN 38.5
           WHEN 'S_SAFETY_ACCIDENT' THEN 0
           WHEN 'S_TRAINING_HOURS'  THEN 12.5
           WHEN 'G_BOARD_SIZE'      THEN 9
           WHEN 'G_INDEPENDENT_RATIO' THEN 55.6
           WHEN 'G_FEMALE_BOARD'    THEN 22.2
       END,
       CASE i.indicator_code
           WHEN 'E_GHG_SCOPE1'  THEN 1200.0
           WHEN 'E_GHG_SCOPE2'  THEN 3600.0
           WHEN 'E_ENERGY_TOTAL' THEN 44000.0
           WHEN 'E_WATER_TOTAL'  THEN 11000.0
           WHEN 'E_WASTE_TOTAL'  THEN 80.0
           WHEN 'S_EMPLOYEE_TOTAL' THEN 2400
           WHEN 'S_FEMALE_RATIO'   THEN 40.0
           WHEN 'S_SAFETY_ACCIDENT' THEN 0
           WHEN 'S_TRAINING_HOURS'  THEN 15.0
           WHEN 'G_BOARD_SIZE'      THEN 9
           WHEN 'G_INDEPENDENT_RATIO' THEN 55.6
           WHEN 'G_FEMALE_BOARD'    THEN 25.0
       END,
       'APPROVED', 'SYSTEM', 'SYSTEM'
FROM MOD_ESG_INDICATOR i
WHERE i.active_yn = 1;
