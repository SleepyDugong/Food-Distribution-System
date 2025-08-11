package ui

import scalafx.application.JFXApp
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.collections.ObservableBuffer
import models._
import services.FoodDistributionService
import java.time.LocalDateTime

object FoodDistributionApp extends JFXApp {
  // Show a debug popup when the GUI starts
  scalafx.application.Platform.runLater {
    new Alert(Alert.AlertType.Information) {
      title = "Debug"
      headerText = Some("ScalaFX GUI Started")
      contentText = "If you see this popup, the GUI is running."
    }.showAndWait()
  }
  override def main(args: Array[String]): Unit = {
    // Required for SBT main class detection
  }
  
  private val service = new FoodDistributionService
  
  // Initialize with some sample data
  initializeSampleData()

  stage = new JFXApp.PrimaryStage {
    title = "Food Distribution Management System"
    scene = createMainScene()
  }

  private def createMainScene(): Scene = {
    val tabPane = new TabPane()
    
    tabPane.tabs = List(
      createDashboardTab(),
      createSimpleTab("Donors", "Donors management coming soon..."),
      createSimpleTab("Beneficiaries", "Beneficiaries management coming soon..."),
      createSimpleTab("Inventory", "Inventory management coming soon...")
    )
    
    new Scene(tabPane, 1000, 700)
  }

  private def createDashboardTab(): Tab = {
    val tab = new Tab {
      text = "Dashboard"
      closable = false
    }
    val stats = service.getSystemStats
    val vbox = new VBox(20) {
      padding = Insets(20)
      children = List(
        new Label("Food Distribution Management System") {
          style = "-fx-font-size: 24px; -fx-font-weight: bold;"
        },
        new Label(s"System Statistics:") {
          style = "-fx-font-size: 16px; -fx-font-weight: bold;"
        },
        new Label(s"• Total Donors: ${stats.totalDonors}"),
        new Label(s"• Total Beneficiaries: ${stats.totalBeneficiaries}"),
        new Label(s"• Total Volunteers: ${stats.totalVolunteers}"),
        new Label(s"• Distribution Centers: ${stats.totalDistributionCenters}"),
        new Label(s"• Total Donations: ${stats.totalDonationsKg} kg"),
        new Label(s"• Total Distributed: ${stats.totalDistributedKg} kg"),
        new Label(f"• Distribution Efficiency: ${stats.distributionEfficiency}%.1f%%"),
        new Button("Show System Report") {
          style = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px 20px;"
          onAction = _ => showSystemReport()
        }
      )
    }
    tab.content = vbox
    tab
  }

  private def createSimpleTab(name: String, message: String): Tab = {
    val tab = new Tab {
      text = name
      closable = false
    }
    val vbox = new VBox(20) {
      padding = Insets(20)
      children = List(
        new Label(name) {
          style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        },
        new Label(message),
        new Button(s"Add New ${name.dropRight(1)}") {
          style = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 8px 16px;"
          onAction = _ => showMessage(s"Add ${name.dropRight(1)}", s"${name.dropRight(1)} functionality coming soon!")
        }
      )
    }
    tab.content = vbox
    tab
  }

  private def showMessage(titleArg: String, message: String): Unit = {
    val alert = new Alert(Alert.AlertType.Information) {
      title = titleArg
      headerText = None
      contentText = message
    }
    alert.showAndWait()
  }

  private def showSystemReport(): Unit = {
    try {
      val report = utils.ReportGenerator.generateSystemSummaryReport(service)
      val dialog = new Dialog[String]() {
        title = "System Report"
        headerText = "Food Distribution System Report"
      }
      val textArea = new TextArea() {
        text = report
        editable = false
        prefRowCount = 20
        prefColumnCount = 80
        wrapText = true
      }
      dialog.dialogPane().setContent(textArea)
      dialog.dialogPane().getButtonTypes.setAll(ButtonType.OK)
      dialog.showAndWait()
    } catch {
      case e: Exception =>
        showMessage("Error", s"Could not generate report: ${e.getMessage}")
    }
  }

  private def initializeSampleData(): Unit = {
    try {
      // Add sample donors
      val donor1 = Donor("D001", "John's Restaurant", "john@restaurant.com", "123-456-7890", "123 Main St", donorType = "Restaurant")
      val donor2 = Donor("D002", "City Grocery Store", "manager@citygroc.com", "123-456-7891", "456 Oak Ave", donorType = "Grocery Store")
      val donor3 = Donor("D003", "Mary Smith", "mary@email.com", "123-456-7892", "789 Pine St", donorType = "Individual")
      
      service.registerPerson(donor1)
      service.registerPerson(donor2)
      service.registerPerson(donor3)
      
      // Add sample beneficiaries
      val ben1 = Beneficiary("B001", "Alice Johnson", "alice@email.com", "123-456-7893", "321 Elm St", householdSize = 4, needsLevel = "High")
      val ben2 = Beneficiary("B002", "Bob Wilson", "bob@email.com", "123-456-7894", "654 Maple Ave", householdSize = 2, needsLevel = "Medium")
      val ben3 = Beneficiary("B003", "Carol Davis", "carol@email.com", "123-456-7895", "987 Cedar St", householdSize = 6, needsLevel = "Critical")
      
      service.registerPerson(ben1)
      service.registerPerson(ben2)
      service.registerPerson(ben3)
      
      // Add sample volunteers
      val vol1 = Volunteer("V001", "Mike Brown", "mike@email.com", "123-456-7896", "111 Oak St", skills = List("Driving", "Organizing"))
      val vol2 = Volunteer("V002", "Sarah Lee", "sarah@email.com", "123-456-7897", "222 Pine Ave", skills = List("Cooking", "Customer Service"))
      
      service.registerPerson(vol1)
      service.registerPerson(vol2)
      
      // Add sample distribution centers
      val center1 = DistributionCenter("DC001", "Downtown Distribution Center", "100 Center St", 1000.0)
      val center2 = DistributionCenter("DC002", "North Side Community Center", "200 North Ave", 750.0)
      val center3 = DistributionCenter("DC003", "South District Hub", "300 South Blvd", 500.0)
      
      service.addDistributionCenter(center1)
      service.addDistributionCenter(center2)
      service.addDistributionCenter(center3)
      
      // Add sample food items and donations
      val bread = Food("F001", "White Bread", "Grains", LocalDateTime.now().plusDays(3))
      val rice = Food("F002", "Rice", "Grains", LocalDateTime.now().plusDays(30))
      val apples = Food("F003", "Apples", "Fruits", LocalDateTime.now().plusDays(7))
      val milk = Food("F004", "Milk", "Dairy", LocalDateTime.now().plusDays(2))
      val chicken = Food("F005", "Chicken Breast", "Protein", LocalDateTime.now().plusDays(1))
      
      // Create sample donations
      val donation1 = Donation("DON001", donor1, Map(bread -> 10.0, apples -> 15.0))
      val donation2 = Donation("DON002", donor2, Map(rice -> 25.0, milk -> 12.0))
      val donation3 = Donation("DON003", donor3, Map(chicken -> 8.0))
      
      // Process donations
      service.processDonation(donation1)
      service.processDonation(donation2)
      service.processDonation(donation3)
      
      // Create a sample distribution event
      val eventResult = service.createDistributionEvent(
        "DC001",
        List(ben1, ben2),
        Map(bread -> 5.0, apples -> 8.0)
      )
      
      println("Sample data initialized successfully!")
      eventResult match {
        case Some(event) => println(s"Created sample distribution event: ${event.id}")
        case None => println("Could not create sample distribution event")
      }
      
    } catch {
      case e: Exception =>
        println(s"Error initializing sample data: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}