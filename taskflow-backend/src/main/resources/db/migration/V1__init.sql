CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `avatar_url` VARCHAR(500),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `workspace` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    `owner_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    FOREIGN KEY (`owner_id`) REFERENCES `user`(`id`)
);

CREATE TABLE IF NOT EXISTS `workspace_member` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `workspace_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `role` VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_workspace_user` (`workspace_id`, `user_id`),
    FOREIGN KEY (`workspace_id`) REFERENCES `workspace`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);

CREATE TABLE IF NOT EXISTS `board` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `workspace_id` BIGINT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    `created_by` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    FOREIGN KEY (`workspace_id`) REFERENCES `workspace`(`id`),
    FOREIGN KEY (`created_by`) REFERENCES `user`(`id`)
);

CREATE TABLE IF NOT EXISTS `board_column` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `board_id` BIGINT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `color` VARCHAR(20) DEFAULT '#409EFF',
    `position` INT NOT NULL DEFAULT 0,
    `wip_limit` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`board_id`) REFERENCES `board`(`id`)
);

CREATE TABLE IF NOT EXISTS `task` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `workspace_id` BIGINT NOT NULL,
    `board_column_id` BIGINT,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    `status` VARCHAR(20) NOT NULL DEFAULT 'TODO',
    `start_date` DATE,
    `due_date` DATE,
    `progress` INT DEFAULT 0,
    `assignee_id` BIGINT,
    `creator_id` BIGINT NOT NULL,
    `parent_task_id` BIGINT,
    `position` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    FOREIGN KEY (`workspace_id`) REFERENCES `workspace`(`id`),
    FOREIGN KEY (`board_column_id`) REFERENCES `board_column`(`id`),
    FOREIGN KEY (`assignee_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`creator_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`parent_task_id`) REFERENCES `task`(`id`),
    INDEX `idx_task_workspace` (`workspace_id`),
    INDEX `idx_task_status` (`status`),
    INDEX `idx_task_assignee` (`assignee_id`),
    INDEX `idx_task_due_date` (`due_date`)
);

CREATE TABLE IF NOT EXISTS `subtask` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `is_completed` TINYINT DEFAULT 0,
    `position` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`task_id`) REFERENCES `task`(`id`)
);

CREATE TABLE IF NOT EXISTS `task_dependency` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `predecessor_task_id` BIGINT NOT NULL,
    `successor_task_id` BIGINT NOT NULL,
    `dependency_type` VARCHAR(20) NOT NULL DEFAULT 'FINISH_TO_START',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_dependency` (`predecessor_task_id`, `successor_task_id`),
    FOREIGN KEY (`predecessor_task_id`) REFERENCES `task`(`id`),
    FOREIGN KEY (`successor_task_id`) REFERENCES `task`(`id`)
);

CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    FOREIGN KEY (`task_id`) REFERENCES `task`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    INDEX `idx_comment_task` (`task_id`)
);

CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `type` VARCHAR(50) NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` VARCHAR(500),
    `reference_id` BIGINT,
    `is_read` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    INDEX `idx_notification_user` (`user_id`, `is_read`)
);

CREATE TABLE IF NOT EXISTS `refresh_token` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `token` VARCHAR(500) NOT NULL UNIQUE,
    `expires_at` DATETIME NOT NULL,
    `is_revoked` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    INDEX `idx_refresh_token` (`token`)
);
