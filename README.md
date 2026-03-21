# 前端

cd frontend
pnpm install
pnpm run dev

# 后端

cd backend
.\mvnw.cmd -U clean spring-boot:run

# 数据库

docker run -d --name snack -e MYSQL_ROOT_PASSWORD=1234 -p 3306:3306 mysql:8

CREATE DATABASE snack
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

USE snack;

CREATE TABLE user (
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
password_hash VARCHAR(255) NOT NULL COMMENT '密码',
role ENUM('user', 'admin') NOT NULL COMMENT '角色',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

CREATE TABLE snack (
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '零食ID',
name VARCHAR(100) NOT NULL COMMENT '零食名称',
image_url VARCHAR(255) NULL COMMENT '商品图片地址',
price DECIMAL(10,2) NOT NULL COMMENT '价格',
stock INT NOT NULL COMMENT '库存数量',
description VARCHAR(255) COMMENT '描述',
seller_id BIGINT NOT NULL COMMENT '上架人ID',
status ENUM('on_sale', 'sold_out', 'off_shelf') NOT NULL DEFAULT 'on_sale' COMMENT '商品状态',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上架时间',
FOREIGN KEY (seller_id) REFERENCES user(id)
);

CREATE TABLE orders (
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
buyer_id BIGINT NOT NULL COMMENT '购买人ID',
total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
FOREIGN KEY (buyer_id) REFERENCES user(id)
);

CREATE TABLE order_item (
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
order_id BIGINT NOT NULL COMMENT '订单ID',
snack_id BIGINT NOT NULL COMMENT '零食ID',
quantity INT NOT NULL COMMENT '购买数量',
price DECIMAL(10,2) NOT NULL COMMENT '购买时单价',
subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
FOREIGN KEY (order_id) REFERENCES orders(id),
FOREIGN KEY (snack_id) REFERENCES snack(id)
);

CREATE TABLE chat_session (
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
order_id BIGINT NOT NULL COMMENT '订单ID',
buyer_id BIGINT NOT NULL COMMENT '买家ID',
seller_id BIGINT NOT NULL COMMENT '卖家ID',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
UNIQUE KEY uk_order_buyer_seller (order_id, buyer_id, seller_id),
FOREIGN KEY (order_id) REFERENCES orders(id),
FOREIGN KEY (buyer_id) REFERENCES user(id),
FOREIGN KEY (seller_id) REFERENCES user(id)
) COMMENT='聊天会话表';

CREATE TABLE chat_message (
id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
session_id BIGINT NOT NULL COMMENT '会话ID',
sender_id BIGINT NOT NULL COMMENT '发送者ID',
content VARCHAR(1000) NOT NULL COMMENT '消息内容',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
FOREIGN KEY (session_id) REFERENCES chat_session(id),
FOREIGN KEY (sender_id) REFERENCES user(id)
) COMMENT='聊天消息表';
