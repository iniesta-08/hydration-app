package com.example.water

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity

class InputActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.input_dialog)

        val ageEditText = findViewById<EditText>(R.id.ageEditText)
        val heightEditText = findViewById<EditText>(R.id.heightEditText)
        val weightEditText = findViewById<EditText>(R.id.weightEditText)
        val confirmButton = findViewById<Button>(R.id.confirmButton)

        confirmButton.setOnClickListener {
            val age = ageEditText.text.toString().toIntOrNull() ?: 0
            val height = heightEditText.text.toString().toDoubleOrNull() ?: 0.0
            val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0

            // Calculate suggested water intake level based on age, height, and weight
            val suggestedWaterIntake = calculateSuggestedWaterIntake(age, height, weight)

            // Return the result to the calling activity
            val resultIntent = Intent()
            resultIntent.putExtra("suggestedWaterIntake", suggestedWaterIntake)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun calculateSuggestedWaterIntake(age: Int, height: Double, weight: Double): Int {
        // Harris-Benedict equation to calculate Basal Metabolic Rate (BMR)
        val bmr: Double
        if (age < 18) {
            // For individuals under 18 years old, use the Harris-Benedict equation for children and adolescents
            bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
        } else {
            // For individuals 18 years and older, use the adult Harris-Benedict equation
            bmr = 655.1 + (9.563 * weight) + (1.850 * height) - (4.676 * age)
        }

        // Adjust for physical activity level (PAL)
        val physicalActivityLevel = 1.5 // Moderate physical activity level (you can adjust this based on user input)
        val suggestedWaterIntake = (bmr * physicalActivityLevel / 30).toInt()

        val resultIntent = Intent()
        resultIntent.putExtra("suggestedWaterIntake", suggestedWaterIntake)
        resultIntent.putExtra("height", height)
        resultIntent.putExtra("weight", weight)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()

        return suggestedWaterIntake
    }

}
