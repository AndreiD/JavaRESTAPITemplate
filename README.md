# Rest API Template - Spring Framework

## Overview
This is my REST API has been built using Java 10, Spring Boot, Spring Cloud Feign, Swagger, Lombok, Junit5 and Docker.

## Quick start
To run the project execute from the project root

```
docker-compose up
```

This will pull the pre-built images from Docker Hub and run it in docker.

Navigate to: http://localhost:8080/swagger-ui.html to access the API through SWAGGER

## API
| Endpoint        | Description |    
| ------------- |:-------------:
| GET /products | Get products. Query parameter 'priceLabel' can be specified with possible values: 'ShowWasNow' or 'ShowWasThenNow' or 'ShowPercDiscount' |


## Tests
You can run all unit and integration tests by executing from the project root
```
mvn clean verify
```

External API can change so integration tests may fail because the application is not able to retrieve any data

## Useful Commands
A [run.sh](run.sh) Bash script has been written to provide useful commands for the project. It is executed in the following way:

``` ./run.sh {build|start|stop|run|push|logs}```

- build: build the application

- start: start the docker containers

- stop: stop and remove the docker containers

- run: build and start

- push: push the images to Docker Hub

- logs: shows log output from docker containers