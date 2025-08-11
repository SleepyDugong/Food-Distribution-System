package utils

import java.time.LocalDateTime
import java.time.ZoneOffset
import models._
import services.FoodDistributionService
import java.time.format.DateTimeFormatter

object ReportGenerator {
  implicit val localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.by(_.toEpochSecond(ZoneOffset.UTC))
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  
  def generateSystemSummaryReport(service: FoodDistributionService): String = {
    val stats = service.getSystemStats
    val timestamp = LocalDateTime.now().format(dateFormatter)
    
    s"""
      |========================================
      |    FOOD DISTRIBUTION SYSTEM REPORT
      |========================================
      |Generated: $timestamp
      |
      |SYSTEM OVERVIEW:
      |• Total Donors: ${stats.totalDonors}
      |• Total Beneficiaries: ${stats.totalBeneficiaries}
      |• Total Volunteers: ${stats.totalVolunteers}
      |• Distribution Centers: ${stats.totalDistributionCenters}
      |
      |DISTRIBUTION METRICS:
      |• Total Donations Received: ${stats.totalDonationsKg} kg
      |• Total Food Distributed: ${stats.totalDistributedKg} kg
      |• Distribution Efficiency: ${stats.distributionEfficiency.formatted("%.1f")}%
      |• Total Distribution Events: ${stats.totalEvents}
      |• Average per Event: ${stats.averageDistributionPerEvent.formatted("%.1f")} kg
      |
      |PERFORMANCE:
      |• System Status: ${if (stats.distributionEfficiency > 80) "Excellent" else if (stats.distributionEfficiency > 60) "Good" else "Needs Improvement"}
      |• Food Waste: ${(100 - stats.distributionEfficiency).formatted("%.1f")}%
      |
      |========================================
    """.stripMargin
  }
  
  def generateDonorReport(service: FoodDistributionService): String = {
    val donors = service.getAllDonors
    val donations = service.getAllDonations
    val timestamp = LocalDateTime.now().format(dateFormatter)
    
    val donorStats = donors.map { donor =>
      val donorDonations = donations.filter(_.donor.id == donor.id)
      val totalWeight = donorDonations.map(_.totalWeight).sum
      val totalItems = donorDonations.map(_.totalItems).sum
      (donor, donorDonations.size, totalWeight, totalItems)
    }.sortBy(-_._3) // Sort by total weight descending
    
    val reportBuilder = new StringBuilder()
    reportBuilder.append(s"""
      |========================================
      |         DONOR ACTIVITY REPORT
      |========================================
      |Generated: $timestamp
      |
      |DONOR SUMMARY:
      |• Total Active Donors: ${donors.count(_.isActive)}
      |• Total Inactive Donors: ${donors.count(!_.isActive)}
      |
      |TOP DONORS BY CONTRIBUTION:
      |""".stripMargin)
    
    donorStats.take(10).foreach { case (donor, donationCount, totalWeight, totalItems) =>
      reportBuilder.append(s"""
        |• ${donor.name} (${donor.donorType})
        |  - Donations: $donationCount
        |  - Total Weight: ${totalWeight.formatted("%.1f")} kg
        |  - Total Items: $totalItems
        |  - Status: ${if (donor.isActive) "Active" else "Inactive"}
        |""".stripMargin)
    }
    
    reportBuilder.append("\n========================================\n")
    reportBuilder.toString()
  }
  
  def generateBeneficiaryReport(service: FoodDistributionService): String = {
    val beneficiaries = service.getAllBeneficiaries
    val events = service.getAllDistributionEvents
    val timestamp = LocalDateTime.now().format(dateFormatter)
    
    val needsDistribution = beneficiaries.groupBy(_.needsLevel).map { case (level, list) =>
      level -> list.size
    }
    
    val householdSizeStats = beneficiaries.map(_.householdSize)
    val avgHouseholdSize = if (householdSizeStats.nonEmpty) householdSizeStats.sum.toDouble / householdSizeStats.size else 0.0
    
    val servedBeneficiaries = events.flatMap(_.beneficiaries).map(_.id).distinct.size
    
    s"""
      |========================================
      |       BENEFICIARY ANALYSIS REPORT
      |========================================
      |Generated: $timestamp
      |
      |BENEFICIARY OVERVIEW:
      |• Total Registered: ${beneficiaries.size}
      |• Active Beneficiaries: ${beneficiaries.count(_.isActive)}
      |• Served This Period: $servedBeneficiaries
      |• Coverage Rate: ${if (beneficiaries.nonEmpty) (servedBeneficiaries.toDouble / beneficiaries.size * 100).formatted("%.1f") else "0.0"}%
      |
      |HOUSEHOLD DEMOGRAPHICS:
      |• Average Household Size: ${avgHouseholdSize.formatted("%.1f")} people
      |• Total People Served: ${beneficiaries.map(_.householdSize).sum}
      |
      |NEEDS DISTRIBUTION:
      |${needsDistribution.map { case (level, count) => s"• $level: $count beneficiaries" }.mkString("\n")}
      |
      |DIETARY RESTRICTIONS:
      |• With Restrictions: ${beneficiaries.count(_.dietaryRestrictions.nonEmpty)}
      |• Without Restrictions: ${beneficiaries.count(_.dietaryRestrictions.isEmpty)}
      |
      |========================================
    """.stripMargin
  }
  
  def generateInventoryReport(service: FoodDistributionService): String = {
    val centers = service.getAllDistributionCenters
    val timestamp = LocalDateTime.now().format(dateFormatter)
    
    val reportBuilder = new StringBuilder()
    reportBuilder.append(s"""
      |========================================
      |        INVENTORY STATUS REPORT
      |========================================
      |Generated: $timestamp
      |
      |DISTRIBUTION CENTERS OVERVIEW:
      |""".stripMargin)
    
    centers.foreach { center =>
      val inventory = center.getInventory
      val expiringItems = center.getExpiringItems(7)
      val expiredItems = inventory.filter { case (food, _) => food.isExpired }
      
      reportBuilder.append(s"""
        |
        |CENTER: ${center.name}
        |• Location: ${center.address}
        |• Capacity Utilization: ${center.capacityUtilization.formatted("%.1f")}% (${center.currentCapacityUsed}/${center.capacity} kg)
        |• Total Items in Stock: ${inventory.size}
        |• Items Expiring Soon (7 days): ${expiringItems.size}
        |• Expired Items: ${expiredItems.size}
        |• Status: ${if (center.isActive) "Active" else "Inactive"}
        |""".stripMargin)
      
      if (inventory.nonEmpty) {
        reportBuilder.append(s"""
          |  Current Inventory:
          |${inventory.take(5).map { case (food, quantity) => s"  - ${food.name}: ${quantity}kg (${food.category})" }.mkString("\n")}
          |${if (inventory.size > 5) s"  ... and ${inventory.size - 5} more items" else ""}
          |""".stripMargin)
      }
    }
    
    // System-wide inventory summary
    val totalCapacity = centers.map(_.capacity).sum
    val totalUsed = centers.map(_.currentCapacityUsed).sum
    val totalItems = centers.map(_.getInventory.size).sum
    val totalExpiringItems = centers.map(_.getExpiringItems(7).size).sum
    
    reportBuilder.append(s"""
      |
      |SYSTEM-WIDE SUMMARY:
      |• Total System Capacity: ${totalCapacity} kg
      |• Total Capacity Used: ${totalUsed.formatted("%.1f")} kg
      |• Overall Utilization: ${if (totalCapacity > 0) (totalUsed / totalCapacity * 100).formatted("%.1f") else "0.0"}%
      |• Total Unique Items: $totalItems
      |• Items Expiring Soon: $totalExpiringItems
      |
      |========================================
    """.stripMargin)
    
    reportBuilder.toString()
  }
  
  def generateDistributionEventsReport(service: FoodDistributionService): String = {
    val events = service.getAllDistributionEvents
    val timestamp = LocalDateTime.now().format(dateFormatter)
    
    if (events.isEmpty) {
      return s"""
        |========================================
        |     DISTRIBUTION EVENTS REPORT
        |========================================
        |Generated: $timestamp
        |
        |No distribution events found.
        |========================================
      """.stripMargin
    }
    
    val totalDistributed = events.map(_.totalDistributed).sum
    val totalBeneficiaries = events.flatMap(_.beneficiaries).map(_.id).distinct.size
    val avgEventSize = totalDistributed / events.size
    
    val reportBuilder = new StringBuilder()
    reportBuilder.append(s"""
      |========================================
      |     DISTRIBUTION EVENTS REPORT
      |========================================
      |Generated: $timestamp
      |
      |EVENT SUMMARY:
      |• Total Events: ${events.size}
      |• Total Food Distributed: ${totalDistributed.formatted("%.1f")} kg
      |• Unique Beneficiaries Served: $totalBeneficiaries
      |• Average Event Size: ${avgEventSize.formatted("%.1f")} kg
      |
      |RECENT EVENTS:
      |""".stripMargin)
    
    events.sortBy(_.eventDate)(localDateTimeOrdering.reverse).take(10).foreach { event =>
      reportBuilder.append(s"""
        |• Event ${event.id.take(8)}
        |  - Date: ${event.eventDate.format(dateFormatter)}
        |  - Center: ${event.center.name}
        |  - Distributed: ${event.totalDistributed.formatted("%.1f")} kg
        |  - Beneficiaries: ${event.beneficiariesCount}
        |  - Status: ${event.status}
        |""".stripMargin)
    }
    
    reportBuilder.append("\n========================================\n")
    reportBuilder.toString()
  }
  
  def generateFullSystemReport(service: FoodDistributionService): String = {
    val timestamp = LocalDateTime.now().format(dateFormatter)
    
    s"""
      |========================================
      |      COMPREHENSIVE SYSTEM REPORT
      |========================================
      |Generated: $timestamp
      |
      |${generateSystemSummaryReport(service)}
      |
      |${generateDonorReport(service)}
      |
      |${generateBeneficiaryReport(service)}
      |
      |${generateInventoryReport(service)}
      |
      |${generateDistributionEventsReport(service)}
      |
      |========================================
      |           END OF REPORT
      |========================================
    """.stripMargin
  }
}