-- ============================================
-- STEM Platform - PostgreSQL Schema
-- Version: 1.0.0
-- Generated for production use
-- ============================================

-- ============================================
-- ENUM TYPES
-- ============================================

CREATE TYPE user_role AS ENUM ('ADMIN', 'INSTRUCTOR', 'STUDENT');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION');
CREATE TYPE course_status AS ENUM ('DRAFT', 'UNDER_REVIEW', 'PUBLISHED', 'ARCHIVED');
CREATE TYPE resource_type AS ENUM ('VIDEO', 'PDF', 'DOCUMENT', 'LINK', 'IMAGE');
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');
CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');
CREATE TYPE payout_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');
CREATE TYPE payout_tx_status AS ENUM ('INITIATED', 'SUCCESS', 'FAILED');
CREATE TYPE workshop_mode AS ENUM ('ONLINE', 'OFFLINE', 'HYBRID');
CREATE TYPE registration_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED');

-- ============================================
-- USER MANAGEMENT TABLES
-- ============================================

-- NOTE: users.id is Firebase UID (string), not auto-generated
CREATE TABLE users (
    id                VARCHAR(128) PRIMARY KEY,  -- Firebase UID
    email             VARCHAR(255) NOT NULL,
    role              user_role NOT NULL DEFAULT 'STUDENT',
    status            user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified_at TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

CREATE TABLE user_profiles (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(128) NOT NULL,  -- Firebase UID
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    phone       VARCHAR(20),
    bio         TEXT,
    avatar_url  VARCHAR(500),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_profiles_user_id UNIQUE (user_id),
    CONSTRAINT uk_user_profiles_phone UNIQUE (phone)
);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);

CREATE TABLE admin_details (
    id          BIGSERIAL PRIMARY KEY,
    profile_id  BIGINT NOT NULL,
    permissions JSONB NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_details_profile FOREIGN KEY (profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    CONSTRAINT uk_admin_details_profile_id UNIQUE (profile_id)
);

CREATE TABLE instructor_details (
    id                  BIGSERIAL PRIMARY KEY,
    profile_id          BIGINT NOT NULL,
    specialization      VARCHAR(255),
    qualification       VARCHAR(255),
    years_of_experience INTEGER DEFAULT 0,
    profile_data        JSONB DEFAULT '{}',
    is_verified         BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_instructor_details_profile FOREIGN KEY (profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    CONSTRAINT uk_instructor_details_profile_id UNIQUE (profile_id)
);

CREATE INDEX idx_instructor_details_is_verified ON instructor_details(is_verified);

CREATE TABLE student_details (
    id              BIGSERIAL PRIMARY KEY,
    profile_id      BIGINT NOT NULL,
    education_level VARCHAR(100),
    institution     VARCHAR(255),
    profile_data    JSONB DEFAULT '{}',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_details_profile FOREIGN KEY (profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    CONSTRAINT uk_student_details_profile_id UNIQUE (profile_id)
);

-- ============================================
-- INSTRUCTOR PAYMENT TABLES
-- ============================================

CREATE TABLE instructor_payment_details (
    id                    BIGSERIAL PRIMARY KEY,
    instructor_id         BIGINT NOT NULL,
    account_holder_name   VARCHAR(255) NOT NULL,
    bank_name             VARCHAR(255),
    account_number        VARCHAR(50),
    ifsc_code             VARCHAR(20),
    upi_id                VARCHAR(100),
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified           BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at           TIMESTAMPTZ,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_instructor_payment_instructor FOREIGN KEY (instructor_id) REFERENCES instructor_details(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_instructor_payment_active ON instructor_payment_details(instructor_id) WHERE is_active = TRUE;

CREATE TABLE instructor_payouts (
    id              BIGSERIAL PRIMARY KEY,
    instructor_id   BIGINT NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,
    status          payout_status NOT NULL DEFAULT 'PENDING',
    reference_note  TEXT,
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_instructor_payouts_instructor FOREIGN KEY (instructor_id) REFERENCES instructor_details(id),
    CONSTRAINT chk_payout_period CHECK (period_end >= period_start),
    CONSTRAINT chk_payout_amount CHECK (amount > 0)
);

CREATE INDEX idx_instructor_payouts_instructor ON instructor_payouts(instructor_id);
CREATE INDEX idx_instructor_payouts_status ON instructor_payouts(status);
CREATE INDEX idx_instructor_payouts_period ON instructor_payouts(period_start, period_end);

CREATE TABLE payout_transactions (
    id                  BIGSERIAL PRIMARY KEY,
    instructor_payout_id BIGINT NOT NULL,
    gateway_name        VARCHAR(100) NOT NULL,
    gateway_reference   VARCHAR(255),
    amount              DECIMAL(12,2) NOT NULL,
    status              payout_tx_status NOT NULL DEFAULT 'INITIATED',
    failure_reason      TEXT,
    requested_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at        TIMESTAMPTZ,
    raw_response        JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payout_tx_payout FOREIGN KEY (instructor_payout_id) REFERENCES instructor_payouts(id)
);

CREATE INDEX idx_payout_tx_payout ON payout_transactions(instructor_payout_id);
CREATE INDEX idx_payout_tx_status ON payout_transactions(status);

-- ============================================
-- COURSE TABLES
-- ============================================

CREATE TABLE courses (
    id              BIGSERIAL PRIMARY KEY,
    instructor_id   BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL,
    description     TEXT,
    short_description VARCHAR(500),
    thumbnail_url   VARCHAR(500),
    price           DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_price  DECIMAL(12,2),
    status          course_status NOT NULL DEFAULT 'DRAFT',
    is_featured     BOOLEAN NOT NULL DEFAULT FALSE,
    published_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_courses_instructor FOREIGN KEY (instructor_id) REFERENCES instructor_details(id),
    CONSTRAINT uk_courses_slug UNIQUE (slug),
    CONSTRAINT chk_courses_price CHECK (price >= 0),
    CONSTRAINT chk_courses_discount CHECK (discount_price IS NULL OR discount_price < price)
);

CREATE INDEX idx_courses_instructor ON courses(instructor_id);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_featured ON courses(is_featured) WHERE is_featured = TRUE;

CREATE TABLE course_modules (
    id          BIGSERIAL PRIMARY KEY,
    course_id   BIGINT NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    position    INTEGER NOT NULL DEFAULT 0,
    is_free     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_modules_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_course_modules_position UNIQUE (course_id, position)
);

CREATE INDEX idx_course_modules_course ON course_modules(course_id);

CREATE TABLE course_lessons (
    id              BIGSERIAL PRIMARY KEY,
    module_id       BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    duration_seconds INTEGER,
    position        INTEGER NOT NULL DEFAULT 0,
    is_preview      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_lessons_module FOREIGN KEY (module_id) REFERENCES course_modules(id) ON DELETE CASCADE,
    CONSTRAINT uk_course_lessons_position UNIQUE (module_id, position)
);

CREATE INDEX idx_course_lessons_module ON course_lessons(module_id);

CREATE TABLE course_resources (
    id          BIGSERIAL PRIMARY KEY,
    lesson_id   BIGINT NOT NULL,
    type        resource_type NOT NULL,
    title       VARCHAR(255),
    video_key   VARCHAR(500),
    url         VARCHAR(1000),
    file_size   BIGINT,
    position    INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_resources_lesson FOREIGN KEY (lesson_id) REFERENCES course_lessons(id) ON DELETE CASCADE,
    CONSTRAINT chk_resource_has_content CHECK (video_key IS NOT NULL OR url IS NOT NULL)
);

CREATE INDEX idx_course_resources_lesson ON course_resources(lesson_id);

-- ============================================
-- QUIZ TABLES
-- ============================================

CREATE TABLE quizzes (
    id              BIGSERIAL PRIMARY KEY,
    lesson_id       BIGINT NOT NULL,
    title           VARCHAR(255),
    description     TEXT,
    passing_score   INTEGER NOT NULL DEFAULT 70,
    max_attempts    INTEGER NOT NULL DEFAULT 3,
    time_limit_mins INTEGER,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quizzes_lesson FOREIGN KEY (lesson_id) REFERENCES course_lessons(id) ON DELETE CASCADE,
    CONSTRAINT uk_quizzes_lesson UNIQUE (lesson_id),
    CONSTRAINT chk_quiz_passing_score CHECK (passing_score BETWEEN 0 AND 100)
);

CREATE TABLE quiz_questions (
    id              BIGSERIAL PRIMARY KEY,
    quiz_id         BIGINT NOT NULL,
    question_text   TEXT NOT NULL,
    explanation     TEXT,
    points          INTEGER NOT NULL DEFAULT 1,
    position        INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quiz_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    CONSTRAINT uk_quiz_questions_position UNIQUE (quiz_id, position)
);

CREATE TABLE quiz_options (
    id          BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct  BOOLEAN NOT NULL DEFAULT FALSE,
    position    INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quiz_options_question FOREIGN KEY (question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE
);

CREATE INDEX idx_quiz_options_question ON quiz_options(question_id);

CREATE TABLE quiz_attempts (
    id              BIGSERIAL PRIMARY KEY,
    quiz_id         BIGINT NOT NULL,
    user_id         VARCHAR(128) NOT NULL,  -- Firebase UID
    score           INTEGER NOT NULL DEFAULT 0,
    total_points    INTEGER NOT NULL,
    percentage      DECIMAL(5,2) NOT NULL,
    passed          BOOLEAN NOT NULL DEFAULT FALSE,
    time_taken_secs INTEGER,
    started_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quiz_attempts_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    CONSTRAINT fk_quiz_attempts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_quiz_attempts_quiz ON quiz_attempts(quiz_id);
CREATE INDEX idx_quiz_attempts_user ON quiz_attempts(user_id);

CREATE TABLE quiz_attempt_responses (
    id                  BIGSERIAL PRIMARY KEY,
    attempt_id          BIGINT NOT NULL,
    question_id         BIGINT NOT NULL,
    selected_option_id  BIGINT,
    is_correct          BOOLEAN,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quiz_responses_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_quiz_responses_question FOREIGN KEY (question_id) REFERENCES quiz_questions(id),
    CONSTRAINT fk_quiz_responses_option FOREIGN KEY (selected_option_id) REFERENCES quiz_options(id),
    CONSTRAINT uk_quiz_responses UNIQUE (attempt_id, question_id)
);

-- ============================================
-- LESSON Q&A TABLES
-- ============================================

CREATE TABLE lesson_questions (
    id          BIGSERIAL PRIMARY KEY,
    lesson_id   BIGINT NOT NULL,
    user_id     VARCHAR(128) NOT NULL,  -- Firebase UID
    title       VARCHAR(255),
    question    TEXT NOT NULL,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lesson_questions_lesson FOREIGN KEY (lesson_id) REFERENCES course_lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_questions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_lesson_questions_lesson ON lesson_questions(lesson_id);
CREATE INDEX idx_lesson_questions_user ON lesson_questions(user_id);

CREATE TABLE lesson_answers (
    id              BIGSERIAL PRIMARY KEY,
    question_id     BIGINT NOT NULL,
    instructor_id   BIGINT NOT NULL,
    answer          TEXT NOT NULL,
    is_accepted     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lesson_answers_question FOREIGN KEY (question_id) REFERENCES lesson_questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_answers_instructor FOREIGN KEY (instructor_id) REFERENCES instructor_details(id)
);

CREATE INDEX idx_lesson_answers_question ON lesson_answers(question_id);

-- ============================================
-- ENROLLMENT & PROGRESS TABLES
-- ============================================

CREATE TABLE course_enrollments (
    id          BIGSERIAL PRIMARY KEY,
    course_id   BIGINT NOT NULL,
    user_id     VARCHAR(128) NOT NULL,  -- Firebase UID
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_enrollments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_enrollments UNIQUE (course_id, user_id)
);

CREATE INDEX idx_enrollments_course ON course_enrollments(course_id);
CREATE INDEX idx_enrollments_user ON course_enrollments(user_id);

CREATE TABLE lesson_progress (
    id              BIGSERIAL PRIMARY KEY,
    lesson_id       BIGINT NOT NULL,
    user_id         VARCHAR(128) NOT NULL,  -- Firebase UID
    progress_pct    INTEGER NOT NULL DEFAULT 0,
    is_completed    BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at    TIMESTAMPTZ,
    last_position   INTEGER DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_progress_lesson FOREIGN KEY (lesson_id) REFERENCES course_lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_progress UNIQUE (lesson_id, user_id),
    CONSTRAINT chk_progress_pct CHECK (progress_pct BETWEEN 0 AND 100)
);

CREATE INDEX idx_progress_user ON lesson_progress(user_id);

-- ============================================
-- CERTIFICATE TABLES
-- ============================================

CREATE TABLE course_certificates (
    id              BIGSERIAL PRIMARY KEY,
    course_id       BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    template_url    VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_certificates_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_certificates_course UNIQUE (course_id)
);

CREATE TABLE issued_certificates (
    id              BIGSERIAL PRIMARY KEY,
    certificate_id  BIGINT NOT NULL,
    user_id         VARCHAR(128) NOT NULL,  -- Firebase UID
    certificate_uid VARCHAR(50) NOT NULL,
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_issued_cert_cert FOREIGN KEY (certificate_id) REFERENCES course_certificates(id),
    CONSTRAINT fk_issued_cert_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_issued_cert UNIQUE (certificate_id, user_id),
    CONSTRAINT uk_certificate_uid UNIQUE (certificate_uid)
);

CREATE INDEX idx_issued_cert_user ON issued_certificates(user_id);

-- ============================================
-- PRODUCT & CART TABLES
-- ============================================

CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL,
    description     TEXT,
    short_desc      VARCHAR(500),
    price           DECIMAL(12,2) NOT NULL,
    discount_price  DECIMAL(12,2),
    stock_quantity  INTEGER NOT NULL DEFAULT 0,
    sku             VARCHAR(100),
    image_url       VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_products_slug UNIQUE (slug),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT chk_products_price CHECK (price >= 0),
    CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_products_active ON products(is_active) WHERE is_active = TRUE;

CREATE TABLE carts (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(128) NOT NULL,  -- Firebase UID
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_carts_user UNIQUE (user_id)
);

CREATE TABLE cart_items (
    id          BIGSERIAL PRIMARY KEY,
    cart_id     BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    quantity    INTEGER NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uk_cart_items UNIQUE (cart_id, product_id),
    CONSTRAINT chk_cart_item_qty CHECK (quantity > 0)
);

-- ============================================
-- TRANSACTION & PAYMENT TABLES
-- ============================================

CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         VARCHAR(128) NOT NULL,  -- Firebase UID
    amount          DECIMAL(12,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'INR',
    status          transaction_status NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_transaction_amount CHECK (amount > 0)
);

CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_status ON transactions(status);

CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  BIGINT NOT NULL,
    payment_method  VARCHAR(50) NOT NULL,
    provider_ref    VARCHAR(255),
    status          payment_status NOT NULL DEFAULT 'PENDING',
    failure_reason  TEXT,
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

CREATE INDEX idx_payments_transaction ON payments(transaction_id);

CREATE TABLE course_purchases (
    id              BIGSERIAL PRIMARY KEY,
    user_id         VARCHAR(128) NOT NULL,  -- Firebase UID
    course_id       BIGINT NOT NULL,
    transaction_id  BIGINT NOT NULL,
    price_paid      DECIMAL(12,2) NOT NULL,
    purchased_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_purchase_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_course_purchase_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_course_purchase_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT uk_course_purchase UNIQUE (user_id, course_id)
);

-- ============================================
-- ORDER TABLES
-- ============================================

CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    user_id         VARCHAR(128) NOT NULL,  -- Firebase UID
    transaction_id  BIGINT,
    order_number    VARCHAR(50) NOT NULL,
    total_amount    DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    tax_amount      DECIMAL(12,2) DEFAULT 0,
    status          order_status NOT NULL DEFAULT 'PENDING',
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_orders_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT uk_orders_number UNIQUE (order_number)
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE TABLE order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    product_name    VARCHAR(255) NOT NULL,
    price           DECIMAL(12,2) NOT NULL,
    quantity        INTEGER NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_order_item_qty CHECK (quantity > 0)
);

-- ============================================
-- ADDRESS TABLES
-- ============================================

CREATE TABLE user_addresses (
    id              BIGSERIAL PRIMARY KEY,
    user_id         VARCHAR(128) NOT NULL,  -- Firebase UID
    label           VARCHAR(50),
    address_line1   VARCHAR(255) NOT NULL,
    address_line2   VARCHAR(255),
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100) NOT NULL,
    postal_code     VARCHAR(20) NOT NULL,
    country         VARCHAR(100) NOT NULL DEFAULT 'India',
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_addresses_user ON user_addresses(user_id);
CREATE UNIQUE INDEX idx_user_addresses_default ON user_addresses(user_id) WHERE is_default = TRUE;

CREATE TABLE order_addresses (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    user_address_id BIGINT,
    recipient_name  VARCHAR(255) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    address_line1   VARCHAR(255) NOT NULL,
    address_line2   VARCHAR(255),
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100) NOT NULL,
    postal_code     VARCHAR(20) NOT NULL,
    country         VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_addresses_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_addresses_user_addr FOREIGN KEY (user_address_id) REFERENCES user_addresses(id) ON DELETE SET NULL,
    CONSTRAINT uk_order_addresses UNIQUE (order_id)
);

-- ============================================
-- WORKSHOP TABLES
-- ============================================

CREATE TABLE workshops (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL,
    description     TEXT,
    thumbnail_url   VARCHAR(500),
    default_fee     DECIMAL(12,2) NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_workshops_slug UNIQUE (slug)
);

CREATE TABLE workshop_registrations (
    id              BIGSERIAL PRIMARY KEY,
    workshop_id     BIGINT NOT NULL,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    organization    VARCHAR(255),
    message         TEXT,
    status          registration_status NOT NULL DEFAULT 'PENDING',
    registered_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workshop_reg_workshop FOREIGN KEY (workshop_id) REFERENCES workshops(id)
);

CREATE INDEX idx_workshop_reg_workshop ON workshop_registrations(workshop_id);
CREATE INDEX idx_workshop_reg_email ON workshop_registrations(email);

CREATE TABLE workshop_sessions (
    id                      BIGSERIAL PRIMARY KEY,
    workshop_registration_id BIGINT NOT NULL,
    mode                    workshop_mode NOT NULL,
    location                VARCHAR(255),
    meeting_link            VARCHAR(500),
    start_date              DATE NOT NULL,
    end_date                DATE NOT NULL,
    start_time              TIME,
    end_time                TIME,
    fee                     DECIMAL(12,2) NOT NULL,
    max_participants        INTEGER,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workshop_session_reg FOREIGN KEY (workshop_registration_id) REFERENCES workshop_registrations(id),
    CONSTRAINT chk_session_dates CHECK (end_date >= start_date)
);

CREATE TABLE workshop_earnings (
    id                  BIGSERIAL PRIMARY KEY,
    workshop_session_id BIGINT NOT NULL,
    total_participants  INTEGER NOT NULL DEFAULT 0,
    total_amount        DECIMAL(12,2) NOT NULL,
    notes               TEXT,
    recorded_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workshop_earning_session FOREIGN KEY (workshop_session_id) REFERENCES workshop_sessions(id),
    CONSTRAINT uk_workshop_earning UNIQUE (workshop_session_id)
);

-- ============================================
-- RATING TABLES
-- ============================================

CREATE TABLE course_ratings (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(128) NOT NULL,  -- Firebase UID
    course_id   BIGINT NOT NULL,
    rating      INTEGER NOT NULL,
    review      TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_ratings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_course_ratings_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_course_ratings UNIQUE (user_id, course_id),
    CONSTRAINT chk_course_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_course_ratings_course ON course_ratings(course_id);

CREATE TABLE product_ratings (
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(128) NOT NULL,  -- Firebase UID
    product_id  BIGINT NOT NULL,
    rating      INTEGER NOT NULL,
    review      TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_ratings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_product_ratings_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uk_product_ratings UNIQUE (user_id, product_id),
    CONSTRAINT chk_product_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_product_ratings_product ON product_ratings(product_id);

-- ============================================
-- NOTIFICATION TABLE
-- ============================================

CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         VARCHAR(128) NOT NULL,  -- Firebase UID
    type            VARCHAR(50) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    action_url      VARCHAR(500),
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

-- ============================================
-- UPDATE TIMESTAMP TRIGGER FUNCTION
-- ============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to all tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_profiles_updated_at BEFORE UPDATE ON user_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_admin_details_updated_at BEFORE UPDATE ON admin_details
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_instructor_details_updated_at BEFORE UPDATE ON instructor_details
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_student_details_updated_at BEFORE UPDATE ON student_details
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_instructor_payment_details_updated_at BEFORE UPDATE ON instructor_payment_details
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_instructor_payouts_updated_at BEFORE UPDATE ON instructor_payouts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payout_transactions_updated_at BEFORE UPDATE ON payout_transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_courses_updated_at BEFORE UPDATE ON courses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_course_modules_updated_at BEFORE UPDATE ON course_modules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_course_lessons_updated_at BEFORE UPDATE ON course_lessons
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_course_resources_updated_at BEFORE UPDATE ON course_resources
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_quizzes_updated_at BEFORE UPDATE ON quizzes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_quiz_questions_updated_at BEFORE UPDATE ON quiz_questions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_quiz_options_updated_at BEFORE UPDATE ON quiz_options
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_quiz_attempts_updated_at BEFORE UPDATE ON quiz_attempts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lesson_questions_updated_at BEFORE UPDATE ON lesson_questions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lesson_answers_updated_at BEFORE UPDATE ON lesson_answers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_course_enrollments_updated_at BEFORE UPDATE ON course_enrollments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_lesson_progress_updated_at BEFORE UPDATE ON lesson_progress
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_course_certificates_updated_at BEFORE UPDATE ON course_certificates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_issued_certificates_updated_at BEFORE UPDATE ON issued_certificates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_carts_updated_at BEFORE UPDATE ON carts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cart_items_updated_at BEFORE UPDATE ON cart_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_course_purchases_updated_at BEFORE UPDATE ON course_purchases
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_order_items_updated_at BEFORE UPDATE ON order_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_addresses_updated_at BEFORE UPDATE ON user_addresses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_order_addresses_updated_at BEFORE UPDATE ON order_addresses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workshops_updated_at BEFORE UPDATE ON workshops
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workshop_registrations_updated_at BEFORE UPDATE ON workshop_registrations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workshop_sessions_updated_at BEFORE UPDATE ON workshop_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workshop_earnings_updated_at BEFORE UPDATE ON workshop_earnings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_course_ratings_updated_at BEFORE UPDATE ON course_ratings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_product_ratings_updated_at BEFORE UPDATE ON product_ratings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- END OF SCHEMA
-- ============================================
