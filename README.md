# Event-Driven-WebClient
This Handle Request and Response From Message Broker also call thrid party application.

# This Service call : SCORING SERVICE. 
- Scoring service is use for get scores with financial data sources. 
- Request and response will get / return to message broker kafka
- Scoring service also handle retry call if http status response from third party 5xx
- Have feature to check function scoring using rest api

## 🚀 Tech Stack
- Java 11
- Spring Boot 2.x
- PostgreSQL
- JPA (Hibernate) & Native
- Redis
- Webclient
- Docker
- REST API
- Event Drivent Using Kafka

## 📂 Features
- Resquest and Response using REST API and Kafka
- Call Third Party App and handle response 