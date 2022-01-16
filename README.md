## ZIO-SQL example application

# Introduction
This repo has as an example code to demonstrate how to use zio-sql to communicate with sql database of your choice (this repo uses postgresql). It contains also zio-http, zio-config, zio-json and other libraries of the ecosystem.

App exposes an http api that query and modify the state of db. Database contains simple eshop-like `customers` and `orders` tables.

# To launch
1. clone this repo
2. cd zio-sql-example
3. docker-compose up (you need to have docker running)
4. sbt run
5. example of requests:
    - GET  localhost:8080/orders
    - GET  localhost:8080/orders/count
    - GET  localhost:8080/orders/04912093-cc2e-46ac-b64c-1bd7bb7758c3
    - GET  localhost:8080/customers/
    - GET  localhost:8080/customers/orders/join
    - GET  localhost:8080/customers/orders/latest-date
    - GET  localhost:8080/customers/orders/count
    - GET  localhost:8080/customers/60b01fc9-c902-4468-8d49-3c0f989def37
    - POST localhost:8080/orders
```json
{
    "id": "f02d8efb-d912-48f5-b7f8-ee6e589ab81e",
    "customerId":"bf356a10-9047-43e3-8df0-8935f0133eef",
    "date":"2022-03-25"
}
```
- POST localhost:8080/customers
```json
{
    "id": "6d90ce5b-55d4-4725-8c9f-9bb5c8104e62",
    "fname": "Ronald",
    "lname": "Russell",
    "verified": false,
    "dateOfBirth": "1965-01-05"
}
```

# TODO
1. Tests
2. Transactions