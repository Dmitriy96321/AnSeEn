CREATE TABLE page(
                     id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                     site_id INT NOT NULL,
                     path TEXT NOT NULL,
                     code INT NOT NULL,
                     content MEDIUMTEXT NOT NULL
);
ALTER TABLE page
    ADD KEY(path(30));