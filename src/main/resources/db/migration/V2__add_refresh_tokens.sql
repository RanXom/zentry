CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                token VARCHAR(255) UNIQUE NOT NULL,
                                expiry_date TIMESTAMP NOT NULL,
                                user_id BIGINT NOT NULL,
                                CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);