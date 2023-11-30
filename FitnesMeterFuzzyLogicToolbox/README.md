# Fitness Meter Fuzzy Logic Controller

## Overview

This repository contains the MATLAB files for the Fitness Meter Fuzzy Logic Controller. This controller is designed to estimate a user's fitness level based on heart rate, exercise duration, and step count using fuzzy logic principles. The system provides a quantified fitness level, aiding in personal health and fitness tracking.

## Files

- `FitnessMeter.m`: The main MATLAB script for the fuzzy logic controller.
- `FitnessMeter.fis`: The Fuzzy Inference System (FIS) file with defined membership functions and rules.

## Inputs

The fuzzy logic controller accepts the following inputs:

1. **Heart Rate (bpm)**: The user's heart rate during exercise.
2. **Exercise Duration (minutes)**: The duration of the user's exercise session.
3. **Step Count**: The total number of steps taken by the user.

## Output

- **Fitness Level**: A score on a scale from 0 to 100, indicating the user's overall fitness level. Categories are Poor, Average, Good, and Excellent.

## Membership Functions

- **Heart Rate**: Categorized as Low, Medium, and High.
- **Exercise Duration**: Classified as Short, Moderate, and Long.
- **Step Count**: Defined as Low, Medium, and High.
- **Fitness Level**: Divided into Poor, Average, Good, and Excellent.

## Rules

The controller uses a set of rules to evaluate the fitness level based on the input parameters. These rules consider various combinations of heart rate, exercise duration, and step count to assign a fitness level.

## Usage

To use the controller:
1. Load the `.fis` file into MATLAB.
2. Run the `FitnessMeter.m` script, providing the necessary input values.
3. The script will output the estimated fitness level based on the provided inputs.

## Example Evaluations

The script includes example input sets for evaluation, demonstrating how different combinations of heart rate, exercise duration, and step count affect the estimated fitness level.

## Customization

Users can modify the membership functions and rules in the `FitnessMeter.fis` file to suit specific requirements or to experiment with different fuzzy logic configurations.

---

This ReadMe provides a comprehensive guide to understanding and utilizing the Fitness Meter Fuzzy Logic Controller. It covers the key components, usage instructions, and the logic behind the system.
