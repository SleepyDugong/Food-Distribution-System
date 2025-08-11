package services

import models._ // Fixed: Changed from models.* to models._
import scala.collection.mutable.ListBuffer
import java.time.LocalDateTime

class FoodDistributionService {
  
  private val donors = ListBuffer[Donor]()
  private val beneficiaries = ListBuffer[Beneficiary]()
  private val volunteers = ListBuffer[Volunteer]()
  private val distributionCenters = ListBuffer[DistributionCenter]()
  private val donations = ListBuffer[Donation]()
  private val distributionEvents = ListBuffer[DistributionEvent]()

  // Register a person (donor, beneficiary, or volunteer)
  def registerPerson[T <: Person](person: T): Unit = {
    person match {
      case donor: Donor => donors += donor
      case beneficiary: Beneficiary => beneficiaries += beneficiary
      case volunteer: Volunteer => volunteers += volunteer
    }
  }

  def addDistributionCenter(center: DistributionCenter): Unit = {
    distributionCenters += center
  }

  def processDonation(donation: Donation): Boolean = {
    // Find a suitable distribution center
    val requiredCapacity = donation.totalWeight
    findBestDistributionCenter(requiredCapacity) match {
      case Some(center) =>
        // Add items to the center's inventory
        donation.items.foreach { case (food, quantity) =>
          center.addInventory(food, quantity)
        }
        
        // Update donation status
        val processedDonation = donation.copy(status = "Processed")
        donations += processedDonation
        
        println(s"Donation processed successfully at ${center.name}")
        true
        
      case None =>
        println("No suitable distribution center found with sufficient capacity")
        false
    }
  }

  private def findBestDistributionCenter(requiredCapacity: Double): Option[DistributionCenter] = {
    distributionCenters
      .filter(center => center.isActive && center.availableCapacity >= requiredCapacity)
      .sortBy(_.capacityUtilization)
      .headOption
  }

  def createDistributionEvent(
    centerId: String,
    selectedBeneficiaries: List[Beneficiary],
    foodAllocation: Map[Food, Double]
  ): Option[DistributionEvent] = {
    
    distributionCenters.find(_.id == centerId) match {
      case Some(center) =>
        // Check if center has sufficient inventory
        val canFulfill = foodAllocation.forall { case (food, quantity) =>
          center.getInventory.getOrElse(food, 0.0) >= quantity
        }
        
        if (canFulfill) {
          // Remove items from inventory
          foodAllocation.foreach { case (food, quantity) =>
            center.removeInventory(food, quantity)
          }
          
          // Fixed: Use proper constructor syntax
          val event = DistributionEvent(
            id = s"DIST-${System.currentTimeMillis()}",
            center = center,
            beneficiaries = selectedBeneficiaries,
            distributedItems = foodAllocation,
            eventDate = LocalDateTime.now(),
            volunteers = volunteers.take(3).toList
          )
          
          distributionEvents += event
          Some(event)
        } else {
          None
        }
        
      case None => None
    }
  }

  def getSystemStats: SystemStats = {
    SystemStats(
      totalDonors = donors.size,
      totalBeneficiaries = beneficiaries.size,
      totalVolunteers = volunteers.size,
      totalDistributionCenters = distributionCenters.size,
      totalDonationsKg = donations.map(_.totalWeight).sum,
      totalDistributedKg = distributionEvents.map(_.totalDistributed).sum,
      totalEvents = distributionEvents.size
    )
  }

  def getExpiringItemsAcrossAllCenters(days: Int): List[(DistributionCenter, List[(Food, Double)])] = {
    distributionCenters.map { center =>
      val expiringItems = center.getExpiringItems(days)
      (center, expiringItems)
    }.filter(_._2.nonEmpty).toList
  }

  // Getter methods
  def getAllDonors: List[Donor] = donors.toList
  def getAllBeneficiaries: List[Beneficiary] = beneficiaries.toList
  def getAllVolunteers: List[Volunteer] = volunteers.toList
  def getAllDistributionCenters: List[DistributionCenter] = distributionCenters.toList
  def getAllDonations: List[Donation] = donations.toList
  def getAllDistributionEvents: List[DistributionEvent] = distributionEvents.toList
}