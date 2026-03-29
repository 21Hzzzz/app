CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    buyer_last_read_message_id BIGINT NULL,
    seller_last_read_message_id BIGINT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_buyer_seller (order_id, buyer_id, seller_id),
    CONSTRAINT fk_chat_session_order
        FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_chat_session_buyer
        FOREIGN KEY (buyer_id) REFERENCES user(id),
    CONSTRAINT fk_chat_session_seller
        FOREIGN KEY (seller_id) REFERENCES user(id)
);

SET @buyer_last_read_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'chat_session'
      AND column_name = 'buyer_last_read_message_id'
);

SET @buyer_last_read_sql := IF(
    @buyer_last_read_exists = 0,
    'ALTER TABLE chat_session ADD COLUMN buyer_last_read_message_id BIGINT NULL AFTER seller_id',
    'SELECT 1'
);

PREPARE stmt FROM @buyer_last_read_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @seller_last_read_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'chat_session'
      AND column_name = 'seller_last_read_message_id'
);

SET @seller_last_read_sql := IF(
    @seller_last_read_exists = 0,
    'ALTER TABLE chat_session ADD COLUMN seller_last_read_message_id BIGINT NULL AFTER buyer_last_read_message_id',
    'SELECT 1'
);

PREPARE stmt FROM @seller_last_read_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chat_message_session_id_id (session_id, id),
    KEY idx_chat_message_session_sender_id_id (session_id, sender_id, id),
    CONSTRAINT fk_chat_message_session
        FOREIGN KEY (session_id) REFERENCES chat_session(id),
    CONSTRAINT fk_chat_message_sender
        FOREIGN KEY (sender_id) REFERENCES user(id)
);

SET @idx_chat_message_session_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'chat_message'
      AND index_name = 'idx_chat_message_session_id_id'
);

SET @idx_chat_message_session_sql := IF(
    @idx_chat_message_session_exists = 0,
    'CREATE INDEX idx_chat_message_session_id_id ON chat_message (session_id, id)',
    'SELECT 1'
);

PREPARE stmt FROM @idx_chat_message_session_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_chat_message_sender_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'chat_message'
      AND index_name = 'idx_chat_message_session_sender_id_id'
);

SET @idx_chat_message_sender_sql := IF(
    @idx_chat_message_sender_exists = 0,
    'CREATE INDEX idx_chat_message_session_sender_id_id ON chat_message (session_id, sender_id, id)',
    'SELECT 1'
);

PREPARE stmt FROM @idx_chat_message_sender_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
