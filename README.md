# stockhunt-test-task

# stockhunt-test-task


This is a Spring Boot project built with Maven. The web hook listener service would accept a trade alert from trade view with webhook.secret password as a header , and payload having alert details as the request body in json format.
It should return an error if the payload  is not validated or a success response if it is valid. Based on the valid payload alert we have to automate the trade by considering the inversion logic , stoploss calculations for the order and risk management for the order before placing it on the Dhan Broker API for execution.

## Prerequisites

Before you begin, ensure you have met the following requirements:

- Java JDK 17
- Maven installed
- Git installed

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/sayone-tech/stockhunt-test-task.git
```
```bash
cd your-repository
```

### Add properties value

You need to add the `webhook.secret` to the `src/main/resources/application.properties` file to access the /alert API before executing the application.

### Build the Project

```bash
mvn clean install
```

### Run the Application

You can run the Spring Boot application using Maven:

```bash
mvn spring-boot:run
```


### Access the Application

Once the application is running, you can call the api using it at `http://localhost:8080/api/v1/postcode/validate`.

Request Header:
```text
webhook.secret = input password added in the property file
```
Request Body:
```json lines
{
  "Name of strategy": "string",
  "Symbol ID": "string",
  "Action": "string (buy/sell)",
  "quantity": "integer",
  "Inverse": "boolean",
  "SL pertrade%": "float",
  "Maxdayriskamount": "float"
}
```

## Running Tests

Before running the test You need to add the `webhook.secret` to the `src/test/resources/application.properties` file.

To run the tests, use the following Maven command:

```bash
mvn test
```