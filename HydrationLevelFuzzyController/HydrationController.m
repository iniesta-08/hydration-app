% Create a new fuzzy inference system
fis = mamfis('Name', 'hydration', 'AndMethod', 'min', 'OrMethod', 'max');

% Set additional properties
fis.ImplicationMethod = 'min';
fis.AggregationMethod = 'max';
fis.DefuzzificationMethod = 'centroid';

% Define input variables
fis = addInput(fis, [0 240], 'Name', 'Time since last drink');
fis = addMF(fis, 'Time since last drink', 'trimf', [0 0 90], 'Name', 'Low');
fis = addMF(fis, 'Time since last drink', 'trimf', [60 90 150], 'Name', 'Medium');
fis = addMF(fis, 'Time since last drink', 'trimf', [140 240 240], 'Name', 'High');

fis = addInput(fis, [0 100], 'Name', 'FitnessLevel');
fis = addMF(fis, 'FitnessLevel', 'trimf', [0 25 50], 'Name', 'Poor');
fis = addMF(fis, 'FitnessLevel', 'trimf', [25 50 75], 'Name', 'Average');
fis = addMF(fis, 'FitnessLevel', 'trimf', [50 75 100], 'Name', 'Good');
fis = addMF(fis, 'FitnessLevel', 'trimf', [75 100 100], 'Name', 'Excellent');

fis = addInput(fis, [0 1000], 'Name', 'Amount of last drink');
fis = addMF(fis, 'Amount of last drink', 'trimf', [0 0 250], 'Name', 'Small');
fis = addMF(fis, 'Amount of last drink', 'trimf', [250 400 650], 'Name', 'Medium');
fis = addMF(fis, 'Amount of last drink', 'trimf', [650 1000 1000], 'Name', 'High');

% Define output variable
fis = addOutput(fis, [0 10], 'Name', 'Hydration level');
fis = addMF(fis, 'Hydration level', 'trimf', [0 0 3], 'Name', 'Low');
fis = addMF(fis, 'Hydration level', 'trimf', [3 5 8], 'Name', 'Medium');
fis = addMF(fis, 'Hydration level', 'trimf', [8 10 10], 'Name', 'High');

% Define rules
% The format for each rule is [Input1 MF, Input2 MF, Input3 MF, Output MF, Weight, Connection Type]
% Connection Type: 1 for 'AND', 2 for 'OR'

% Example rules:
% Rule 1: If 'Time since last drink' is High AND 'FitnessLevel' is Poor AND 'Amount of last drink' is Small THEN 'Hydration level' is Low
rule1 = [3 1 1 1 1 1];

% Rule 2: If 'Time since last drink' is Medium AND 'FitnessLevel' is Average THEN 'Hydration level' is Medium
rule2 = [2 2 0 2 1 1];

% Rule 3: If 'FitnessLevel' is Good AND 'Amount of last drink' is High THEN 'Hydration level' is High
rule3 = [0 3 3 3 1 1];


% Rule 4: If 'Time since last drink' is Low AND 'FitnessLevel' is Good THEN 'Hydration level' is Medium
rule4 = [1 3 0 2 1 1];

% Rule 5: If 'Amount of last drink' is Medium THEN 'Hydration level' is Medium
rule5 = [0 0 2 2 1 1];

% Rule 6: If 'Time since last drink' is High AND 'FitnessLevel' is Excellent THEN 'Hydration level' is High
rule6 = [3 4 0 3 1 1];

% Rule 7: If 'FitnessLevel' is Poor AND 'Amount of last drink' is Small THEN 'Hydration level' is Low
rule7 = [0 1 1 1 1 1];

% Rule 8: If 'Time since last drink' is Medium AND 'FitnessLevel' is Average AND 'Amount of last drink' is High THEN 'Hydration level' is Medium
rule8 = [2 2 3 2 1 1];

% Rule 9: If 'FitnessLevel' is Good AND 'Amount of last drink' is Medium THEN 'Hydration level' is High
rule9 = [0 3 2 3 1 1];

% Add these rules to the FIS
fis = addRule(fis, [rule1; rule2; rule3; rule4; rule5; rule6; rule7; rule8; rule9]);


% Save the FIS to a file
writeFIS(fis, 'Hydration.fis');

% Load the FIS
fis = readfis('hydration.fis');

% Define sets of input values for evaluation
% Format: [Time since last drink, FitnessLevel, Amount of last drink]
inputs = [
    120, 30, 500;  % Example 1
    60,  70, 300;  % Example 2
    180, 80, 100;  % Example 3
    30,  40, 700;  % Example 4
    150, 20, 400   % Example 5
];

% Evaluate the FIS for each set of inputs
for i = 1:size(inputs, 1)
    output = evalfis(fis, inputs(i, :));
    disp(['Example ', num2str(i), ' - Hydration level: ', num2str(output)]);
end

