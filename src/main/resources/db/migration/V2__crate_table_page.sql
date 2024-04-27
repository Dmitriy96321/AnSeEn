CREATE TABLE page(
                     id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                     site_id INT NOT NULL,
                     path TEXT NOT NULL,
                     code INT NOT NULL,
                     content MEDIUMTEXT NOT NULL,
                     CONSTRAINT unique_path UNIQUE (path(30)),
                     KEY path_key (path(30))

);