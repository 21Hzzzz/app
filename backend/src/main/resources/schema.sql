CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_buyer_seller (order_id, buyer_id, seller_id),
    CONSTRAINT fk_chat_session_order
        FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_chat_session_buyer
        FOREIGN KEY (buyer_id) REFERENCES user(id),
    CONSTRAINT fk_chat_session_seller
        FOREIGN KEY (seller_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_message_session
        FOREIGN KEY (session_id) REFERENCES chat_session(id),
    CONSTRAINT fk_chat_message_sender
        FOREIGN KEY (sender_id) REFERENCES user(id)
);
