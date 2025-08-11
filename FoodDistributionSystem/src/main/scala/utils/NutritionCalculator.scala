package utils

import models._

object NutritionCalculator {
  
  // Nutrition data per kg for different food categories
  private val nutritionData = Map(
    "Grains" -> Map("calories" -> 3000.0, "protein" -> 100.0, "carbs" -> 600.0, "fat" -> 30.0),
    "Protein" -> Map("calories" -> 2000.0, "protein" -> 250.0, "carbs" -> 0.0, "fat" -> 150.0),
    "Dairy" -> Map("calories" -> 600.0, "protein" -> 35.0, "carbs" -> 50.0, "fat" -> 35.0),
    "Fruits" -> Map("calories" -> 500.0, "protein" -> 10.0, "carbs" -> 120.0, "fat" -> 2.0),
    "Vegetables" -> Map("calories" -> 250.0, "protein" -> 30.0, "carbs" -> 50.0, "fat" -> 3.0),
    "Other" -> Map("calories" -> 1500.0, "protein" -> 50.0, "carbs" -> 200.0, "fat" -> 50.0)
  )
  
  case class NutritionInfo(
    calories: Double,
    protein: Double,
    carbohydrates: Double,
    fat: Double
  )
  
  def calculateNutrition(food: Food, quantity: Double): NutritionInfo = {
    val baseNutrition = nutritionData.getOrElse(food.category, nutritionData("Other"))
    
    NutritionInfo(
      calories = baseNutrition("calories") * quantity,
      protein = baseNutrition("protein") * quantity,
      carbohydrates = baseNutrition("carbs") * quantity,
      fat = baseNutrition("fat") * quantity
    )
  }
  
  def calculateTotalNutrition(items: Map[Food, Double]): NutritionInfo = {
    val nutritionValues = items.map { case (food, quantity) =>
      calculateNutrition(food, quantity)
    }
    
    nutritionValues.foldLeft(NutritionInfo(0.0, 0.0, 0.0, 0.0)) { (total, current) =>
      NutritionInfo(
        calories = total.calories + current.calories,
        protein = total.protein + current.protein,
        carbohydrates = total.carbohydrates + current.carbohydrates,
        fat = total.fat + current.fat
      )
    }
  }
  
  def calculateDailyNutritionForHousehold(householdSize: Int): NutritionInfo = {
    // Basic daily nutrition requirements per person (approximate)
    val dailyPerPerson = NutritionInfo(
      calories = 2000.0,
      protein = 50.0,
      carbohydrates = 300.0,
      fat = 65.0
    )
    
    NutritionInfo(
      calories = dailyPerPerson.calories * householdSize,
      protein = dailyPerPerson.protein * householdSize,
      carbohydrates = dailyPerPerson.carbohydrates * householdSize,
      fat = dailyPerPerson.fat * householdSize
    )
  }
  
  def assessNutritionalAdequacy(provided: NutritionInfo, required: NutritionInfo): Map[String, Double] = {
    Map(
      "calories" -> (provided.calories / required.calories * 100),
      "protein" -> (provided.protein / required.protein * 100),
      "carbohydrates" -> (provided.carbohydrates / required.carbohydrates * 100),
      "fat" -> (provided.fat / required.fat * 100)
    )
  }
}