CREATE TABLE site (
                      id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                      status ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL,
                      status_time DATETIME NOT NULL,
                      last_error TEXT,
                      url VARCHAR(255) NOT NULL,
                      name VARCHAR(255) NOT NULL
);
CREATE TABLE page(
                     id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                     site_id INT NOT NULL,
                     path TEXT NOT NULL,
                     code INT NOT NULL,
                     content MEDIUMTEXT NOT NULL,
                     KEY path_key (path(30))
);
CREATE TABLE lemma (
                       id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                       site_id INT NOT NULL,
                       lemma VARCHAR(255) NOT NULL,
                       frequency INT NOT NULL
);
CREATE TABLE `indexes` (
                           id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                           page_id INT NOT NULL,
                           lemma_id INT NOT NULL,
                           ranks FLOAT NOT NULL
);