CREATE TABLE lemma (
                      id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                      site_id INT NOT NULL,
                      lemma VARCHAR(255) NOT NULL,
                      frequency INT NOT NULL
);