-- Label system
CREATE TABLE IF NOT EXISTS `label` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `workspace_id` BIGINT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `color` VARCHAR(20) NOT NULL DEFAULT '#165DFF',
    `created_by` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    FOREIGN KEY (`workspace_id`) REFERENCES `workspace`(`id`),
    FOREIGN KEY (`created_by`) REFERENCES `user`(`id`),
    INDEX `idx_label_workspace` (`workspace_id`)
);

CREATE TABLE IF NOT EXISTS `task_label` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id` BIGINT NOT NULL,
    `label_id` BIGINT NOT NULL,
    UNIQUE KEY `uk_task_label` (`task_id`, `label_id`),
    FOREIGN KEY (`task_id`) REFERENCES `task`(`id`),
    FOREIGN KEY (`label_id`) REFERENCES `label`(`id`)
);

-- Activity log system
CREATE TABLE IF NOT EXISTS `task_activity` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `action` VARCHAR(50) NOT NULL,
    `field_name` VARCHAR(50),
    `old_value` VARCHAR(500),
    `new_value` VARCHAR(500),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`task_id`) REFERENCES `task`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    INDEX `idx_activity_task` (`task_id`),
    INDEX `idx_activity_created` (`created_at`)
);
