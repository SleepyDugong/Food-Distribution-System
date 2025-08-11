package models

import java.time.LocalDateTime

// Base trait for all people in the system
trait Person {
  val id: String
  val name: String
  val email: String
  val phone: String
  val address: String
  val registrationDate: LocalDateTime
}

case class Donor(
  id: String,
  name: String,
  email: String,
  phone: String,
  address: String,
  registrationDate: LocalDateTime = LocalDateTime.now(),
  donorType: String = "Individual",
  totalDonations: Double = 0.0,
  isActive: Boolean = true
) extends Person {
  override def toString: String = s"$name ($donorType) - Total: ${totalDonations}kg"
}

case class Beneficiary(
  id: String,
  name: String,
  email: String,
  phone: String,
  address: String,
  registrationDate: LocalDateTime = LocalDateTime.now(),
  householdSize: Int,
  needsLevel: String = "Medium",
  dietaryRestrictions: List[String] = List.empty,
  isActive: Boolean = true
) extends Person {
  override def toString: String = s"$name (Household: $householdSize) - Needs: $needsLevel"
}

case class Volunteer(
  id: String,
  name: String,
  email: String,
  phone: String,
  address: String,
  registrationDate: LocalDateTime = LocalDateTime.now(),
  skills: List[String] = List.empty,
  availability: String = "Flexible",
  hoursContributed: Double = 0.0,
  isActive: Boolean = true
) extends Person {
  override def toString: String = s"$name - Skills: ${skills.mkString(", ")} - Hours: $hoursContributed"
}