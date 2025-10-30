# emd-payment-core
Service that manages the ability to make payments via third-party Apps

The microservice is responsible for:
- Registering a retrieval token by the user to access the digital notification
- Retrieving the retrieval token to obtain all information related to the retrieval token
- Redirecting to the relevant page to view the digital notification
- Retrieving all payment attempts for a given TPP
- Retrieving all payment attempts by a given citizen for a given TPP

## API Documentation

API specification: [openapi.payment.yaml](https://github.com/pagopa/cstar-infrastructure/blob/main/src/domains/mil-app-poc/api/emd_payment_core/openapi.payment.yaml)

## Components

---

#### [PaymentService](src/main/java/it/gov/pagopa/emd/payment/service/PaymentServiceImpl.java)

Main class that handles CRUD operations with retrieval and paymentAttempt entities

---

#### [RetrievalRepository](src/main/java/it/gov/pagopa/emd/payment/repository/RetrievalRepository.java) - [PaymentAttemptRepository](src/main/java/it/gov/pagopa/emd/payment/repository/PaymentAttemptRepository.java)

Repositories used for operations performed on the database

Collections used: 'retrieval' - 'payment_attempt'

---

#### [TppConnector](src/main/java/it/gov/pagopa/emd/payment/connector/TppConnectorImpl.java)

Class that interact with emd-tpp service to retrieve the TPP data