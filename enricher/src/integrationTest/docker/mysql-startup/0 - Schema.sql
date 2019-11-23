CREATE TABLE `kafka_game_coordinator`.`user` (
  `user_id` BIGINT(19) UNSIGNED NOT NULL,
  `rank` INT UNSIGNED NULL,
  `auth_token` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE INDEX `AUTH_TOKEN` (`auth_token` ASC) VISIBLE);

CREATE TABLE `kafka_game_coordinator`.`user_status` (
  `user_id` BIGINT(19) unsigned NOT NULL,
  `status` varchar(45) DEFAULT NULL,
  `updated_ts` BIGINT(19) unsigned NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE);

