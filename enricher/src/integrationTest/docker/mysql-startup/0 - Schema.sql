CREATE TABLE `kafka_game_coordinator`.`user` (
  `user_id` INT UNSIGNED NOT NULL,
  `rank` INT UNSIGNED NULL,
  `auth_token` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE INDEX `AUTH_TOKEN` (`auth_token` ASC) VISIBLE);

CREATE TABLE `kafka_game_coordinator`.`user_status` (
  `user_id` INT UNSIGNED NOT NULL,
  `status` VARCHAR(45) NULL,
  `updated_ts` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `kafka_game_coordinator`.`user` (`user_id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);

