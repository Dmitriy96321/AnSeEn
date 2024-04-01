CREATE TABLE site (
                      id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                      status ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL,
                      status_time DATETIME NOT NULL,
                      last_error TEXT,
                      url VARCHAR(255) NOT NULL,
                      name VARCHAR(255) NOT NULL
);