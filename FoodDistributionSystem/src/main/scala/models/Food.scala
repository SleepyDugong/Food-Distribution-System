package models

import java.time.LocalDateTime

case class Food(
  id: String,
  name: String,
  category: String = "Other",
  expirationDate: LocalDateTime,
  nutritionalValue: Double = 0.0,
  allergens: List[String] = List.empty
) {
  def isExpiring(days: Int): Boolean = {
    expirationDate.isBefore(LocalDateTime.now().plusDays(days))
  }
  
  def isExpired: Boolean = expirationDate.isBefore(LocalDateTime.now())
  
  override def toString: String = s"$name ($category) - Expires: ${expirationDate.toLocalDate}"
}

case class Donation(
  id: String,
  donor: Donor,
  items: Map[Food, Double],
  donationDate: LocalDateTime = LocalDateTime.now(),
  status: String = "Pending",
  notes: String = ""
) {
  def totalWeight: Double = items.values.sum
  def totalItems: Int = items.size
  
  override def toString: String = s"Donation ${id.take(8)} from ${donor.name} - ${totalWeight}kg (${totalItems} items)"
}

// Food category traits for type safety
trait Grain extends Food
trait Vegetable extends Food  
trait Protein extends Food
trait Perishable extends Food

case class SystemStats(
  totalDonors: Int = 0,
  totalBeneficiaries: Int = 0,
  totalVolunteers: Int = 0,
  totalDistributionCenters: Int = 0,
  totalDonationsKg: Double = 0.0,
  totalDistributedKg: Double = 0.0,
  totalEvents: Int = 0,
  averageResponseTime: Double = 0.0
) {
  def distributionEfficiency: Double = {
    if (totalDonationsKg > 0) (totalDistributedKg / totalDonationsKg) * 100 else 0.0
  }
  
  def averageDistributionPerEvent: Double = {
    if (totalEvents > 0) totalDistributedKg / totalEvents else 0.0
  }
}