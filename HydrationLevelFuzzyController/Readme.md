
# Hydration Level Fuzzy Logic Controller

## Overview

This repository contains the MATLAB files for the Hydration Level Fuzzy Logic Controller, designed as part of the Hydration Tracking App. The controller uses fuzzy logic to estimate the hydration level of a user based on various inputs and provides recommendations for water intake.

## Files

-   `HydrationController.m`: The main MATLAB script for the fuzzy logic controller.
-   `HydrationController.fis`: The Fuzzy Inference System (FIS) file containing the defined membership functions and rules.

## Inputs

The fuzzy logic controller takes the following inputs:

1.  **Time Since Last Drink (minutes)**: The duration since the user's last drink.
2.  **Amount of Last Drink (milliliters)**: The volume of the user's last drink.
3.  **Fitness Level**: An arbitrary scale (e.g., 1-100) representing the user's level of physical activity.

## Output

-   **Hydration Level**: An estimation of the user's current hydration status, represented on an arbitrary scale (e.g., 1-10), where higher values indicate better hydration.

## Basic Rules

Some of the basic rules implemented in the fuzzy logic controller include:

1.  If the time since the last drink is short and the amount of the last drink is large, then the hydration level is high.
2.  If the time since the last drink is long and the physical activity level is high, then the hydration level is low.

## Usage

To use the controller, load the `.fis` file into MATLAB and provide the input values. The script `HydrationController.m` matlab code for the controller.