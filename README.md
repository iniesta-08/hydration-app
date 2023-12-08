# HYDROFIT Mobile Application

## Overview

This project employs a client-server model, where the mobile application serves as the client, interacting with a backend server encapsulated within a Docker container. The use of Docker ensures consistency in the server environment, facilitating deployment and enhancing overall reliability.

## Architecture - Client-Server Model

The project adopts a typical client-server architecture, with the mobile application acting as the client. This client communicates with a backend server, which is encapsulated within a Docker container. This separation of concerns allows for scalable and maintainable development.

## Consistency and Ease of Deployment

The backend server is containerized using Docker, ensuring a consistent and reproducible environment. This consistency greatly simplifies deployment processes, making it easier to manage and reducing the chances of deployment-related issues.

## Testing (Android App)

The testing process for the Android application is comprehensive and includes the following steps:

1. **Acquiring Necessary Permissions:** Ensuring that the application requests and handles required permissions correctly.

2. **Manual Data Entry:** Thorough testing involves manual data entry to simulate real-world user interactions and inputs.

3. **Simulating Changes in Server Output:** The application is tested by simulating different scenarios of server output to validate its robustness and responsiveness.

This rigorous testing approach aims to guarantee the functionality and reliability of the mobile application under various conditions.

## Development Tools

The Android Studio Integrated Development Environment (IDE) is employed for the development and testing of the Android application. Android Studio provides a feature-rich environment with tools for designing, coding, and testing Android applications efficiently.

## Getting Started

To set up and run the project locally, follow these steps:

1. Clone the repository to your local machine.
2. Install Android Studio and make sure to run with latest android studio version (>= hedgehog).
3. Open the "app-final" project in Android Studio.
4. Set up the Docker environment for the backend server.
5. In HydrofitApplication.kt set the value if constant IP to the IP of your server.
5. Build and run the Android application on your preferred emulator or physical device.

To set up the server - 
## Preqrequisites:
* Have docker and docker-compose installed

## Run Server
```bash
    docker-compose up
```

## Bring down server
```bash
    docker-compose down
```

## Run test cases
```bash
    docker exec -it hydrofitserver pytest
```

## Dependencies

- Android Studio
- Docker

## Contributors

- Gaurav Chandrashekhar Kulkarni 
- Shreyas Chandrashekhar Kirtane
- Aniket Agrawal
- Krithish Goli
- Sri Raghav Bobburi 
- Amogha Bheemanakone Narappa
