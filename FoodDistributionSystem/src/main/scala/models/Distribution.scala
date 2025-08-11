package models

import java.time.LocalDateTime
import scala.collection.mutable

case class DistributionCenter(
  id: String,
  name: String,
  address: String,
  capacity: Double,
  private val _currentInventory: mutable.Map[Food, Double] = mutable.Map.empty,
  operatingHours: String = "9:00 AM - 5:00 PM",
  contactInfo: String = "",
  isActive: Boolean = true
) {
  
  def currentCapacityUsed: Double = _currentInventory.values.sum
  def availableCapacity: Double = capacity - currentCapacityUsed
  def capacityUtilization: Double = if (capacity > 0) (currentCapacityUsed / capacity) * 100 else 0.0
  
  def addInventory(food: Food, quantity: Double): Unit = {
    if (availableCapacity >= quantity) {
      val currentQuantity = _currentInventory.getOrElse(food, 0.0)
      _currentInventory(food) = currentQuantity + quantity
    }
  }
  
  def removeInventory(food: Food, quantity: Double): Boolean = {
    val currentQuantity = _currentInventory.getOrElse(food, 0.0)
    if (currentQuantity >= quantity) {
      if (currentQuantity == quantity) {
        _currentInventory.remove(food)
      } else {
        _currentInventory(food) = currentQuantity - quantity
      }
      true
    } else {
      false
    }
  }
  
  def getInventory: Map[Food, Double] = _currentInventory.toMap
  
  def getExpiringItems(days: Int): List[(Food, Double)] = {
    _currentInventory.filter { case (food, _) =>
      food.isExpiring(days)
    }.toList
  }
  
  override def toString: String = s"$name - Capacity: ${currentCapacityUsed}/${capacity}kg (${capacityUtilization.formatted("%.1f")}%)"
}

case class DistributionEvent(
  id: String,
  center: DistributionCenter,
  beneficiaries: List[Beneficiary],
  distributedItems: Map[Food, Double],
  eventDate: LocalDateTime = LocalDateTime.now(),
  volunteers: List[Volunteer] = List.empty,
  status: String = "Completed"
) {
  def totalDistributed: Double = distributedItems.values.sum
  def beneficiariesCount: Int = beneficiaries.size
  
  override def toString: String = s"Event ${id.take(8)} at ${center.name} - ${totalDistributed}kg to ${beneficiariesCount} beneficiaries"
}