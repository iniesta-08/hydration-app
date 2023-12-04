# HYDROFIT PYTHON BACKEND SERVER

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
    docker exec -it hydrofit pytest
```
# Hydrofit Mobile Application

An intuitive application designed to encourage users to exercise and stay hydrated through
physiological feedback and timed alerts. This application constitutes the UI component of the
Guardian Angel app, implementing features such as the Dashboard, Profile Page, Fitness Logging Page,
Hydration Logging Page, and local timed alerts to prompt users to engage in fitness activities.

## Installation

To run the Hydrofit Mobile Application, follow these instructions:

1. Directly install the `app-debug.apk` file on an Android device.

   or

2. Open the app in Android Studio and run it to launch on an emulator or connected device.

## Testing

Follow these steps for testing the application:

1. The first launch flow acquires necessary permissions and saves profile data.

2. Manually fill in data by pressing the "Update Logs" button in the Dashboard.

3. Fill in demo details and record heart rate.

4. The server output is mocked within the app. To test multiple values, go to `MainActivity.kt` and
   change the values of top-level variables: `FITNESS_METER` and `HYDRATION_METER`, then rerun the
   app.

5. To test the step counter, walk around and observe the changes in the step counter notification.

6. To test reminder alerts, change the device time, and the alerts will be posted. Clicking on the
   reminder alert will take you to the Fitness Logs page.

WARNING - DO NOT RUN THE CAMERA ON AN EMULATOR