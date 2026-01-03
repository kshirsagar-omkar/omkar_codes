# STEM Database Schema - Corrections & Optimization

## Phase 2: Corrected PostgreSQL Schema

### Summary of Changes

| Category | Original Issues | Corrections Applied |
|----------|-----------------|---------------------|
| **Primary Keys** | `INT` type | Changed to `BIGSERIAL` for scalability |
| **Timestamps** | `TIMESTAMP` without TZ | Changed to `TIMESTAMPTZ` (TIMESTAMP WITH TIME ZONE) |
| **Audit Columns** | Missing `updated_at` in most tables | Added `created_at` and `updated_at` to ALL tables |
| **String Fields** | Untyped `string` | Proper `VARCHAR(n)` or `TEXT` based on content |
| **Enums** | Plain strings for status/type | PostgreSQL ENUM types for type safety |
| **Constraints** | Missing UNIQUE, NOT NULL | Added proper constraints |
| **Foreign Keys** | Incorrect references | Fixed to reference correct tables |
| **Polymorphic FK** | `RATING.target_type/target_id` | Split into `COURSE_RATING` and `PRODUCT_RATING` |
| **Decimal Precision** | Unspecified | `DECIMAL(12,2)` for money fields |

---

## PostgreSQL ENUM Types

```sql
-- User & Status Enums
CREATE TYPE user_role AS ENUM ('ADMIN', 'INSTRUCTOR', 'STUDENT');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION');

-- Course Enums
CREATE TYPE course_status AS ENUM ('DRAFT', 'UNDER_REVIEW', 'PUBLISHED', 'ARCHIVED');
CREATE TYPE resource_type AS ENUM ('VIDEO', 'PDF', 'DOCUMENT', 'LINK', 'IMAGE');

-- Payment & Order Enums
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');
CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');
CREATE TYPE payout_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');
CREATE TYPE payout_tx_status AS ENUM ('INITIATED', 'SUCCESS', 'FAILED');

-- Workshop Enums
CREATE TYPE workshop_mode AS ENUM ('ONLINE', 'OFFLINE', 'HYBRID');
CREATE TYPE registration_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED');
```

---

## Corrected Table Definitions

### 1. USER

```sql
CREATE TABLE users (
    id                BIGSERIAL PRIMARY KEY,
    email             VARCHAR(255) NOT NULL,
    password_hash     VARCHAR(255) NOT NULL,  -- ADDED: Was missing
    role              user_role NOT NULL DEFAULT 'STUDENT',
    status            user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified_at TIMESTAMPTZ,  -- ADDED: Email verification tracking
    created_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
```

**Changes:**
- Renamed table to `users` (reserved word avoidance)
- Added `password_hash` column
- Added `email_verified_at` for verification tracking
- Added `updated_at` column
- Added UNIQUE constraint on email
- Added indexes

---

### 2. USER_PROFILE

```sql
CREATE TABLE user_profiles (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
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
```

**Changes:**
- Added `created_at` and `updated_at`
- Added UNIQUE on `user_id` (1:1 relationship)
- Added UNIQUE on `phone`
- Added CASCADE delete

---

### 3. ADMIN_DETAIL

```sql
CREATE TABLE admin_details (
    id          BIGSERIAL PRIMARY KEY,
    profile_id  BIGINT NOT NULL,
    permissions JSONB NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_admin_details_profile FOREIGN KEY (profile_id) REFERENCES user_profiles(id) ON DELETE CASCADE,
    CONSTRAINT uk_admin_details_profile_id UNIQUE (profile_id)
);
```

**Changes:**
- Changed `JSON` to `JSONB` (indexable, more efficient)
- Added audit columns
- Added UNIQUE on `profile_id`

---

### 4. INSTRUCTOR_DETAIL

```sql
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
```

**Changes:**
- Expanded `profile` JSON to structured columns where possible
- Added verification tracking
- Added audit columns

---

### 5. STUDENT_DETAIL

```sql
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
```

---

### 6. INSTRUCTOR_PAYMENT_DETAIL

```sql
CREATE TABLE instructor_payment_details (
    id                    BIGSERIAL PRIMARY KEY,
    instructor_id         BIGINT NOT NULL,  -- Changed: Now references instructor_details
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

-- Only one active payment detail per instructor
CREATE UNIQUE INDEX idx_instructor_payment_active ON instructor_payment_details(instructor_id) WHERE is_active = TRUE;
```

**Changes:**
- FK now correctly references `instructor_details`
- Added verification fields
- Added partial unique index for active payment

---

### 7. INSTRUCTOR_PAYOUT

```sql
CREATE TABLE instructor_payouts (
    id              BIGSERIAL PRIMARY KEY,
    instructor_id   BIGINT NOT NULL,  -- Changed: References instructor_details
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
```

---

### 8. PAYOUT_TRANSACTION

```sql
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
```

---

### 9. COURSE

```sql
CREATE TABLE courses (
    id              BIGSERIAL PRIMARY KEY,
    instructor_id   BIGINT NOT NULL,  -- Changed: References instructor_details
    title           VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL,  -- ADDED: SEO-friendly URL
    description     TEXT,
    short_description VARCHAR(500),  -- ADDED
    thumbnail_url   VARCHAR(500),  -- ADDED
    price           DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_price  DECIMAL(12,2),  -- ADDED
    status          course_status NOT NULL DEFAULT 'DRAFT',
    is_featured     BOOLEAN NOT NULL DEFAULT FALSE,  -- ADDED
    published_at    TIMESTAMPTZ,  -- ADDED
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
```

---

### 10. COURSE_MODULE

```sql
CREATE TABLE course_modules (
    id          BIGSERIAL PRIMARY KEY,
    course_id   BIGINT NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,  -- ADDED
    position    INTEGER NOT NULL DEFAULT 0,
    is_free     BOOLEAN NOT NULL DEFAULT FALSE,  -- Renamed from is_paid (inverted logic is clearer)
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_course_modules_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_course_modules_position UNIQUE (course_id, position)
);

CREATE INDEX idx_course_modules_course ON course_modules(course_id);
```

---

### 11. COURSE_LESSON

```sql
CREATE TABLE course_lessons (
    id              BIGSERIAL PRIMARY KEY,
    module_id       BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,  -- ADDED
    duration_seconds INTEGER,  -- ADDED: Lesson duration
    position        INTEGER NOT NULL DEFAULT 0,
    is_preview      BOOLEAN NOT NULL DEFAULT FALSE,  -- ADDED: Preview lesson
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_course_lessons_module FOREIGN KEY (module_id) REFERENCES course_modules(id) ON DELETE CASCADE,
    CONSTRAINT uk_course_lessons_position UNIQUE (module_id, position)
);

CREATE INDEX idx_course_lessons_module ON course_lessons(module_id);
```

---

### 12. COURSE_RESOURCE

```sql
CREATE TABLE course_resources (
    id          BIGSERIAL PRIMARY KEY,
    lesson_id   BIGINT NOT NULL,
    type        resource_type NOT NULL,
    title       VARCHAR(255),  -- ADDED
    video_key   VARCHAR(500),
    url         VARCHAR(1000),
    file_size   BIGINT,  -- ADDED: File size in bytes
    position    INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_course_resources_lesson FOREIGN KEY (lesson_id) REFERENCES course_lessons(id) ON DELETE CASCADE,
    CONSTRAINT chk_resource_has_content CHECK (video_key IS NOT NULL OR url IS NOT NULL)
);

CREATE INDEX idx_course_resources_lesson ON course_resources(lesson_id);
```

---

### 13-20. QUIZ SYSTEM

```sql
-- QUIZ
CREATE TABLE quizzes (
    id              BIGSERIAL PRIMARY KEY,
    lesson_id       BIGINT NOT NULL,
    title           VARCHAR(255),  -- ADDED
    description     TEXT,  -- ADDED
    passing_score   INTEGER NOT NULL DEFAULT 70,  -- ADDED: Percentage
    max_attempts    INTEGER NOT NULL DEFAULT 3,
    time_limit_mins INTEGER,  -- ADDED: Time limit
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_quizzes_lesson FOREIGN KEY (lesson_id) REFERENCES course_lessons(id) ON DELETE CASCADE,
    CONSTRAINT uk_quizzes_lesson UNIQUE (lesson_id),
    CONSTRAINT chk_quiz_passing_score CHECK (passing_score BETWEEN 0 AND 100)
);

-- QUIZ_QUESTION
CREATE TABLE quiz_questions (
    id              BIGSERIAL PRIMARY KEY,
    quiz_id         BIGINT NOT NULL,
    question_text   TEXT NOT NULL,
    explanation     TEXT,  -- ADDED: Explanation after answer
    points          INTEGER NOT NULL DEFAULT 1,  -- ADDED
    position        INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_quiz_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    CONSTRAINT uk_quiz_questions_position UNIQUE (quiz_id, position)
);

-- QUIZ_OPTION
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

-- QUIZ_ATTEMPT
CREATE TABLE quiz_attempts (
    id              BIGSERIAL PRIMARY KEY,
    quiz_id         BIGINT NOT NULL,
    student_id      BIGINT NOT NULL,  -- Changed: References student_details
    score           INTEGER NOT NULL DEFAULT 0,
    total_points    INTEGER NOT NULL,
    percentage      DECIMAL(5,2) NOT NULL,
    passed          BOOLEAN NOT NULL DEFAULT FALSE,
    time_taken_secs INTEGER,  -- ADDED
    started_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_quiz_attempts_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    CONSTRAINT fk_quiz_attempts_student FOREIGN KEY (student_id) REFERENCES student_details(id)
);

CREATE INDEX idx_quiz_attempts_quiz ON quiz_attempts(quiz_id);
CREATE INDEX idx_quiz_attempts_student ON quiz_attempts(student_id);

-- QUIZ_ATTEMPT_ANSWER (Renamed from QUIZ_ATTEMPT_ANSWER for clarity)
CREATE TABLE quiz_attempt_responses (
    id                  BIGSERIAL PRIMARY KEY,
    attempt_id          BIGINT NOT NULL,
    question_id         BIGINT NOT NULL,
    selected_option_id  BIGINT,
    is_correct          BOOLEAN,  -- ADDED: Denormalized for performance
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_quiz_responses_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_quiz_responses_question FOREIGN KEY (question_id) REFERENCES quiz_questions(id),
    CONSTRAINT fk_quiz_responses_option FOREIGN KEY (selected_option_id) REFERENCES quiz_options(id),
    CONSTRAINT uk_quiz_responses UNIQUE (attempt_id, question_id)
);
```

---

### 21-22. LESSON Q&A

```sql
-- LESSON_QUESTION
CREATE TABLE lesson_questions (
    id          BIGSERIAL PRIMARY KEY,
    lesson_id   BIGINT NOT NULL,
    student_id  BIGINT NOT NULL,  -- Changed: References student_details
    title       VARCHAR(255),  -- ADDED
    question    TEXT NOT NULL,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,  -- ADDED
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_lesson_questions_lesson FOREIGN KEY (lesson_id) REFERENCES course_lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_questions_student FOREIGN KEY (student_id) REFERENCES student_details(id)
);

CREATE INDEX idx_lesson_questions_lesson ON lesson_questions(lesson_id);
CREATE INDEX idx_lesson_questions_student ON lesson_questions(student_id);

-- LESSON_ANSWER
CREATE TABLE lesson_answers (
    id              BIGSERIAL PRIMARY KEY,
    question_id     BIGINT NOT NULL,
    instructor_id   BIGINT NOT NULL,  -- Changed: References instructor_details
    answer          TEXT NOT NULL,
    is_accepted     BOOLEAN NOT NULL DEFAULT FALSE,  -- ADDED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_lesson_answers_question FOREIGN KEY (question_id) REFERENCES lesson_questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_answers_instructor FOREIGN KEY (instructor_id) REFERENCES instructor_details(id)
);

CREATE INDEX idx_lesson_answers_question ON lesson_answers(question_id);
```

---

### 23-25. ENROLLMENT & PROGRESS

```sql
-- COURSE_ENROLLMENT
CREATE TABLE course_enrollments (
    id          BIGSERIAL PRIMARY KEY,
    course_id   BIGINT NOT NULL,
    student_id  BIGINT NOT NULL,  -- Changed: References student_details
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMPTZ,  -- ADDED: For time-limited access
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES student_details(id),
    CONSTRAINT uk_enrollments UNIQUE (course_id, student_id)
);

CREATE INDEX idx_enrollments_course ON course_enrollments(course_id);
CREATE INDEX idx_enrollments_student ON course_enrollments(student_id);

-- LESSON_PROGRESS
CREATE TABLE lesson_progress (
    id              BIGSERIAL PRIMARY KEY,
    lesson_id       BIGINT NOT NULL,
    student_id      BIGINT NOT NULL,  -- Changed: References student_details
    progress_pct    INTEGER NOT NULL DEFAULT 0,  -- ADDED: 0-100
    is_completed    BOOLEAN NOT NULL DEFAULT FALSE,  -- ADDED
    completed_at    TIMESTAMPTZ,
    last_position   INTEGER DEFAULT 0,  -- ADDED: Video position in seconds
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_progress_lesson FOREIGN KEY (lesson_id) REFERENCES course_lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_student FOREIGN KEY (student_id) REFERENCES student_details(id),
    CONSTRAINT uk_progress UNIQUE (lesson_id, student_id),
    CONSTRAINT chk_progress_pct CHECK (progress_pct BETWEEN 0 AND 100)
);

CREATE INDEX idx_progress_student ON lesson_progress(student_id);
```

---

### 26-27. CERTIFICATES

```sql
-- COURSE_CERTIFICATE (Template)
CREATE TABLE course_certificates (
    id              BIGSERIAL PRIMARY KEY,
    course_id       BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    template_url    VARCHAR(500),  -- ADDED
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,  -- ADDED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_certificates_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_certificates_course UNIQUE (course_id)
);

-- ISSUED_CERTIFICATE
CREATE TABLE issued_certificates (
    id              BIGSERIAL PRIMARY KEY,
    certificate_id  BIGINT NOT NULL,
    student_id      BIGINT NOT NULL,  -- Changed: References student_details
    certificate_uid VARCHAR(50) NOT NULL,  -- ADDED: Unique verification code
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_issued_cert_cert FOREIGN KEY (certificate_id) REFERENCES course_certificates(id),
    CONSTRAINT fk_issued_cert_student FOREIGN KEY (student_id) REFERENCES student_details(id),
    CONSTRAINT uk_issued_cert UNIQUE (certificate_id, student_id),
    CONSTRAINT uk_certificate_uid UNIQUE (certificate_uid)
);

CREATE INDEX idx_issued_cert_student ON issued_certificates(student_id);
```

---

### 28-29. PRODUCTS

```sql
-- PRODUCT
CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL,  -- ADDED
    description     TEXT,
    short_desc      VARCHAR(500),  -- ADDED
    price           DECIMAL(12,2) NOT NULL,
    discount_price  DECIMAL(12,2),  -- ADDED
    stock_quantity  INTEGER NOT NULL DEFAULT 0,
    sku             VARCHAR(100),  -- ADDED
    image_url       VARCHAR(500),  -- ADDED
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_products_slug UNIQUE (slug),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT chk_products_price CHECK (price >= 0),
    CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_products_active ON products(is_active) WHERE is_active = TRUE;
```

---

### 30-32. CART

```sql
-- CART
CREATE TABLE carts (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_carts_user UNIQUE (user_id)  -- One cart per user
);

-- CART_ITEM (price removed - fetch from product at checkout)
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
```

---

### 33-38. TRANSACTIONS, PAYMENTS, ORDERS

```sql
-- TRANSACTION
CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'INR',  -- ADDED
    status          transaction_status NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(100),  -- ADDED: External reference
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_transaction_amount CHECK (amount > 0)
);

CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_status ON transactions(status);

-- PAYMENT
CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  BIGINT NOT NULL,
    payment_method  VARCHAR(50) NOT NULL,  -- UPI, CARD, NETBANKING, WALLET
    provider_ref    VARCHAR(255),
    status          payment_status NOT NULL DEFAULT 'PENDING',
    failure_reason  TEXT,  -- ADDED
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payments_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

CREATE INDEX idx_payments_transaction ON payments(transaction_id);

-- COURSE_PURCHASE
CREATE TABLE course_purchases (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    course_id       BIGINT NOT NULL,
    transaction_id  BIGINT NOT NULL,
    price_paid      DECIMAL(12,2) NOT NULL,  -- ADDED: Price at time of purchase
    purchased_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_course_purchase_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_course_purchase_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_course_purchase_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT uk_course_purchase UNIQUE (user_id, course_id)
);

-- ORDER
CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    transaction_id  BIGINT,
    order_number    VARCHAR(50) NOT NULL,  -- ADDED: Human-readable order number
    total_amount    DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0,  -- ADDED
    tax_amount      DECIMAL(12,2) DEFAULT 0,  -- ADDED
    status          order_status NOT NULL DEFAULT 'PENDING',
    notes           TEXT,  -- ADDED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_orders_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    CONSTRAINT uk_orders_number UNIQUE (order_number)
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

-- ORDER_ITEM (with snapshot data)
CREATE TABLE order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    product_name    VARCHAR(255) NOT NULL,  -- ADDED: Snapshot
    price           DECIMAL(12,2) NOT NULL,
    quantity        INTEGER NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_order_item_qty CHECK (quantity > 0)
);
```

---

### 39-40. ADDRESSES

```sql
-- USER_ADDRESS
CREATE TABLE user_addresses (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    label           VARCHAR(50),  -- ADDED: Home, Office, etc.
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

-- ORDER_ADDRESS (Snapshot - intentionally denormalized)
CREATE TABLE order_addresses (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    user_address_id BIGINT,  -- Original reference (can be null if deleted)
    recipient_name  VARCHAR(255) NOT NULL,  -- ADDED
    phone           VARCHAR(20) NOT NULL,  -- ADDED
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
```

---

### 41-44. WORKSHOPS

```sql
-- WORKSHOP
CREATE TABLE workshops (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL,  -- ADDED
    description     TEXT,
    thumbnail_url   VARCHAR(500),  -- ADDED
    default_fee     DECIMAL(12,2) NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,  -- ADDED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_workshops_slug UNIQUE (slug)
);

-- WORKSHOP_REGISTRATION
CREATE TABLE workshop_registrations (
    id              BIGSERIAL PRIMARY KEY,
    workshop_id     BIGINT NOT NULL,
    user_id         BIGINT,  -- ADDED: Link to user if logged in
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    organization    VARCHAR(255),
    message         TEXT,
    status          registration_status NOT NULL DEFAULT 'PENDING',  -- ADDED
    registered_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_workshop_reg_workshop FOREIGN KEY (workshop_id) REFERENCES workshops(id),
    CONSTRAINT fk_workshop_reg_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_workshop_reg_workshop ON workshop_registrations(workshop_id);
CREATE INDEX idx_workshop_reg_email ON workshop_registrations(email);

-- WORKSHOP_SESSION
CREATE TABLE workshop_sessions (
    id                      BIGSERIAL PRIMARY KEY,
    workshop_registration_id BIGINT NOT NULL,
    mode                    workshop_mode NOT NULL,
    location                VARCHAR(255),
    meeting_link            VARCHAR(500),
    start_date              DATE NOT NULL,
    end_date                DATE NOT NULL,
    start_time              TIME,  -- ADDED
    end_time                TIME,  -- ADDED
    fee                     DECIMAL(12,2) NOT NULL,
    max_participants        INTEGER,  -- ADDED
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_workshop_session_reg FOREIGN KEY (workshop_registration_id) REFERENCES workshop_registrations(id),
    CONSTRAINT chk_session_dates CHECK (end_date >= start_date)
);

-- WORKSHOP_EARNING
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
```

---

### 45-46. RATINGS (Split from polymorphic)

```sql
-- COURSE_RATING (Replaces polymorphic RATING for courses)
CREATE TABLE course_ratings (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    course_id   BIGINT NOT NULL,
    rating      INTEGER NOT NULL,
    review      TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,  -- ADDED: Verified purchase
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_course_ratings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_course_ratings_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_course_ratings UNIQUE (user_id, course_id),
    CONSTRAINT chk_course_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_course_ratings_course ON course_ratings(course_id);

-- PRODUCT_RATING (Replaces polymorphic RATING for products)
CREATE TABLE product_ratings (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    rating      INTEGER NOT NULL,
    review      TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,  -- ADDED: Verified purchase
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_product_ratings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_product_ratings_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uk_product_ratings UNIQUE (user_id, product_id),
    CONSTRAINT chk_product_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_product_ratings_product ON product_ratings(product_id);
```

---

### 47. NOTIFICATION

```sql
CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    type            VARCHAR(50) NOT NULL,  -- ADDED: SYSTEM, COURSE, ORDER, etc.
    title           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    action_url      VARCHAR(500),  -- ADDED: Link to related resource
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMPTZ,  -- ADDED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;
```

---

## Index Recommendations Summary

| Table | Recommended Index | Reason |
|-------|-------------------|--------|
| `users` | `email` | Login queries |
| `courses` | `status`, `instructor_id` | Filtering |
| `course_enrollments` | `(course_id, student_id)` | Enrollment checks |
| `quiz_attempts` | `(quiz_id, student_id)` | Attempt tracking |
| `orders` | `user_id`, `status` | Order history |
| `notifications` | `(user_id, is_read)` | Unread count |

All indexes are included in the schema above.
