object Main {
  def main(args: Array[String]): Unit = {
    println("=" * 60)
    println("  FOOD DISTRIBUTION MANAGEMENT SYSTEM")
    println("  Supporting UN SDG Goal 2: Zero Hunger")
    println("=" * 60)
    
    // Import the classes we need
    import models._
    import services.FoodDistributionService
    import java.time.LocalDate
    import java.time.LocalDateTime
    
    val service = new FoodDistributionService()
    
    println("\n🏗️  INITIALIZING SYSTEM...")
    
    // Create distribution centers
    val center1 = new DistributionCenter("DC001", "Downtown Food Hub", "123 Main St", 1000.0)
    val center2 = new DistributionCenter("DC002", "Community Center North", "456 Oak Ave", 800.0)
    service.addDistributionCenter(center1)
    service.addDistributionCenter(center2)
    println("✅ Created 2 distribution centers")
    
    // Register donors (demonstrating inheritance)
    val donors = List(
      Donor("DNR001", "Green Grocery Store", "contact@greengrocer.com", "123-456-7890", "1 Market St"),
      Donor("DNR002", "Maria's Restaurant", "maria@restaurant.com", "123-456-7891", "2 Main Ave"),
      Donor("DNR003", "John Smith", "john.smith@email.com", "123-456-7892", "3 Oak Rd")
    )
    
    donors.foreach(service.registerPerson) // Polymorphic method call
    println(s"✅ Registered ${donors.size} donors")
    
    // Register beneficiaries
    val beneficiaries = List(
      Beneficiary("BEN001", "Sarah Johnson", "sarah.j@email.com", "123-456-7893", "4 Elm St", householdSize = 4, needsLevel = "High", dietaryRestrictions = List("Gluten-free")),
      Beneficiary("BEN002", "Michael Rodriguez", "m.rodriguez@email.com", "123-456-7894", "5 Oak St", householdSize = 2, needsLevel = "Medium", dietaryRestrictions = List()),
      Beneficiary("BEN003", "Emily Chen", "emily.chen@email.com", "123-456-7895", "6 Pine St", householdSize = 5, needsLevel = "Critical", dietaryRestrictions = List("Vegetarian", "Nut allergy"))
    )
    
    beneficiaries.foreach(service.registerPerson) // Polymorphic method call
    println(s"✅ Registered ${beneficiaries.size} beneficiaries")
    
    // Register volunteers
    val volunteers = List(
      Volunteer("VOL001", "Tom Martinez", "tom.m@email.com", "123-456-7896", "7 Cedar St", skills = List("Driving", "Lifting"), availability = "Monday, Wednesday, Friday"),
      Volunteer("VOL002", "Jennifer Lee", "jennifer.l@email.com", "123-456-7897", "8 Maple Ave", skills = List("Organization", "Spanish"), availability = "Tuesday, Thursday, Saturday")
    )
    
    volunteers.foreach(service.registerPerson) // Polymorphic method call
    println(s"✅ Registered ${volunteers.size} volunteers")
    
    println("\n🍎 DEMONSTRATING INHERITANCE & POLYMORPHISM...")
    
    // Create different food types (demonstrating inheritance)
    val foods = List(
      Food("FOOD001", "White Rice", category = "Grain", expirationDate = LocalDateTime.now().plusDays(30), nutritionalValue = 130),
      Food("FOOD002", "Carrots", category = "Vegetable", expirationDate = LocalDateTime.now().plusDays(5), nutritionalValue = 41),
      Food("FOOD003", "Chicken Breast", category = "Protein", expirationDate = LocalDateTime.now().plusDays(2), nutritionalValue = 165)
    )
    
    // Demonstrate polymorphism - different food types have different behaviors
    foods.foreach { food =>
      println(s"📦 ${food.name} (${food.category}):")
      println(s"   - Expiration date: ${food.expirationDate}")
      println(s"   - Nutritional value: ${food.nutritionalValue}")
      println(s"   - Allergens: ${food.allergens.mkString(", ")}")
      println(s"   - Is expired: ${food.isExpired}")
    }
    
    println("\n📦 PROCESSING DONATIONS...")
    
    // Create donation (demonstrating composition)
    val donation = Donation(
      id = "DON001",
      donor = donors.head,
      items = Map(
        foods(0) -> 50.0,
        foods(1) -> 30.0,
        foods(2) -> 20.0
      ),
      donationDate = LocalDateTime.now()
    )
    
    if (service.processDonation(donation)) {
      println("✅ Donation processed successfully!")
      println(s"   - Total weight: ${donation.totalWeight} kg")
      println(s"   - Total items: ${donation.totalItems}")
      println(s"   - Donor: ${donation.donor.name} (${donation.donor.donorType})")
    } else {
      println("❌ Donation processing failed!")
    }
    
    println("\n🎯 CREATING DISTRIBUTION EVENT...")
    
    // Create distribution event
    val foodAllocation = Map(
      foods(0) -> 25.0, // Distribute half the rice
      foods(1) -> 15.0, // Distribute half the carrots
      foods(2) -> 10.0  // Distribute half the chicken
    )
    
    service.createDistributionEvent(
      centerId = center1.id,
      selectedBeneficiaries = beneficiaries.take(2),
      foodAllocation = foodAllocation
    ) match {
      case Some(event) =>
        println("✅ Distribution event created!")
        println(s"   - Event ID: ${event.id}")
        println(s"   - Center: ${event.center.name}")
        println(s"   - Beneficiaries: ${event.beneficiariesCount}")
        println(s"   - Food distributed: ${event.totalDistributed} kg")
      
      case None =>
        println("❌ Distribution event creation failed!")
    }
    
    println("\n📊 SYSTEM STATISTICS...")
    
    val stats = service.getSystemStats
    println(s"📈 Total donations: ${stats.totalDonationsKg} kg")
    println(s"📈 Total distributed: ${stats.totalDistributedKg} kg")
    
    val efficiency = if (stats.totalDonationsKg > 0) 
      (stats.totalDistributedKg / stats.totalDonationsKg) * 100 
    else 0.0
    println(f"📈 Distribution efficiency: $efficiency%.1f%%")
    
    // Check for expiring items (demonstrating trait usage)
    println("\n⏰ EXPIRATION CHECK...")
    val expiringItems = service.getExpiringItemsAcrossAllCenters(7)
    if (expiringItems.nonEmpty) {
      println("⚠️  Items expiring within 7 days:")
      expiringItems.foreach { case (center, items) =>
        println(s"   ${center.name}:")
        items.foreach { case (food, quantity) =>
          println(s"     - ${food.name}: ${quantity} kg (Expires: ${food.expirationDate.toLocalDate})")
        }
      }
    } else {
      println("✅ No items expiring soon!")
    }
    
    println("\n🌍 UN SDG GOAL 2 IMPACT...")
    val totalFamilyMembers = beneficiaries.map(_.householdSize).sum
    val foodSecurityDays = if (totalFamilyMembers > 0) {
      (stats.totalDistributedKg / (totalFamilyMembers * 0.5)).toInt
    } else 0
    
    println(s"🍽️  Estimated meals provided: ${(stats.totalDistributedKg * 2000 / 600).toInt} meals")
    println(s"🏠 Family-days of food security: $foodSecurityDays days")
    println(s"🌱 Environmental impact: ${(stats.totalDistributedKg * 2.2).toInt} lbs diverted from landfill")
    
    println("\n" + "=" * 60)
    println("✅ DEMONSTRATION COMPLETE!")
    println("🎯 OOP Concepts Demonstrated:")
    println("   ✅ Inheritance (Food → Grain/Vegetable/Protein)")
    println("   ✅ Polymorphism (Different food behaviors)")
    println("   ✅ Abstract Classes (Food, Person)")
    println("   ✅ Traits/Mixins (Perishable, Trackable)")
    println("   ✅ Composition (Service managing multiple objects)")
    println("🌍 Supporting UN SDG Goal 2: Zero Hunger")
    println("=" * 60)
  }
}