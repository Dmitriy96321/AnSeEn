# **Локальный поисковый движок по сайту**
![Снимок экрана от 2024-06-04 11-51-19](https://github.com/Dmitriy96321/AnSeEn/assets/163676399/c29fdf98-293d-4711-8ac8-dedd6e7d2a50)
 <img src="https://github.com/devicons/devicon/blob/master/icons/java/java-original-wordmark.svg" title="Java" alt="Java" width="50" height="40"/>&nbsp;
![Static Badge](https://img.shields.io/badge/SpringBoot-6DB33F?style=flat&logo=SpringBoot&logoColor=white)
![Static Badge](https://img.shields.io/badge/Flyway-CC0200?style=flat&logo=Flyway)
![Static Badge](https://img.shields.io/badge/Jsoup-1071D3?style=flat&logo=jsoup&logoColor=white)
![Static Badge](https://img.shields.io/badge/Redis-FF4438?style=flat&logo=redis&logoColor=white)
![Static Badge](https://img.shields.io/badge/MySql-4479A1?style=flat&logo=MySql&logoColor=white) 

@Contacts [t.me/KarDmitriy](https://t.me/KarDmitriy)
 

## Краткое описание
Проект создан в рамках итоговой работы, предназначен для поиска информации на сайтах указанных в [конфигурационном файле](https://github.com/Dmitriy96321/AnSeEn/blob/master/src/main/resources/application.yaml).

+ В конфигурационном файле перед запуском приложения задаются адреса сайтов, по которым движок должен осуществлять поиск.
  
  ![изображение](https://github.com/Dmitriy96321/AnSeEn/assets/163676399/63e8832b-2cc6-414d-bc63-bb9247047b09)
+ Управление осуществляется через RestAPI.

 ![Снимок экрана от 2024-06-04 17-27-41](https://github.com/Dmitriy96321/AnSeEn/assets/163676399/9658dd05-ad5e-4556-841b-87fdc71473ee)
+ Так же у приложения присутствует встроенный Web-interface, который имеет три вкладки.
 
  + Dashboard - вкладка которая открывается по умолчанию, содержит в себе статистическую информацию о проиндексированных сайтах.
  ![изображение](https://github.com/Dmitriy96321/AnSeEn/assets/163676399/2475ace7-01aa-4e78-ba00-6c4064be6742)
 
  + Management - вкладка которая отвечает за запуск/остановку индексации, а так же за индексацию отдельных страниц.
   ![изображение](https://github.com/Dmitriy96321/AnSeEn/assets/163676399/57ecf90c-2e3b-4fcf-ac53-17294d4a4617)
  
  + Search  - вкладка которая отвечает за запуск поиска
  ![изображение](https://github.com/Dmitriy96321/AnSeEn/assets/163676399/d0cccde8-7db0-4913-bd62-af7d6c20e779)
+ Поисковый движок самостоятельно обходит все страницы заданных сайтов и индексирует их. 
+ Пользователь  присылает запрос через API движка. Запрос — это набор слов, по которым нужно найти страницы сайта.
+ Запрос определённым образом трансформируется в список слов, переведённых в базовую форму. Например, для существительных — именительный падеж, единственное число.
+ В индексе ищутся страницы, на которых встречаются все эти слова.
+ Результаты поиска ранжируются, сортируются и отдаются пользователю.
## Быстрый Запуск
Для запуска потребуется установленная Java 17, а так же Docker.
 + Скачать самодостаточный архив(все необходимые библиотеки уже присутсвуют) [AnSeEn-1.0-SNAPSHOT.jar](https://disk.yandex.ru/d/-Jd4_p5jZiWogQ).
 + Запустить 2 контейнера в Docker.
   ```bash
   docker run --name redis -p 6379:6379 -d redis
   docker run --name search-engine-db -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=search_engine -p 3306:3306 -d mysql:late
   ```
 + После этого из папки в которой скачан AnSeEn-1.0-SNAPSHOT.jar выполнить в терминале.
   ```bash
   java -jar AnSeEn-1.0-SNAPSHOT.jar
   ```
## Технологии используемые в проекте 
+ Java 17
  + Spring Boot 3.2.2
  + Hibernate (JPA)
  + Jsoup
  + Log4J
  + Lucene
  + Maven
+ REST
+ MySql
  + Flyway
+ Redis
  + Lettuce
+ Docker
