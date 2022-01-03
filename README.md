## ZIO-SQL example application

# ZIO-SQL is not yet released...
so if you want to try out zio-sql and app in this repo, do:
1. clone zio-sql and cd into zio-sql
2. sbt publishLocal
3. find published version from $HOME/.ivy2/local/dev.zio/zio-sql_2.13/
4. replace zioSqlVersion value in build.sbt

# Introduction
This repo has as an example code to demonstrate how to use zio-sql to communicate with sql database of your choice (this repo uses postgresql). It contains also zio-http, zio-config, zio-json and other libraries of the ecosystem.

# To launch
1. clone this repo
2. cd zio-sql-example
3. docker-compose up (you need to have docker running)
4. sbt run
5. example of requests:
    GET localhost:8080/orders
    GET localhost:8080/orders/04912093-cc2e-46ac-b64c-1bd7bb7758c3
    GET localhost:8080/orders/count
    
    GET localhost:8080/customers/
    GET localhost:8080/customers/60b01fc9-c902-4468-8d49-3c0f989def37

# Contents
1. Inserts
2. Selects
3. Deletes
4. Subqueries
5. Joins
6. Correlated subqueries
7. Transactions

# TODO
1. README
2. Transactions
3. Tests
4. HttpRoutes.scala