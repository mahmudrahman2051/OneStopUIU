# 🎯 OneStopUIU - University Management System

> **OneStopUIU** is a comprehensive JavaFX-based management system designed for **United International University (UIU)**. The system provides a unified platform for managing canteen operations with separate interfaces for customers, sellers, and administrators.

---

## 🚀 **QUICK START FOR PRESENTATION**

### **Option 1: IntelliJ IDEA (Recommended)**
1. Open project in IntelliJ IDEA 2025.1
2. Run configuration: **"OneStopUIU Application"** 
3. Click ▶️ **Run**

### **Option 2: Command Line**
```bash
# Windows PowerShell
.\mvnw.cmd javafx:run

# Or use the presentation batch file
.\run-presentation.bat
```

---

## 🎭 **DEMO ACCOUNTS FOR PRESENTATION**

| **Role** | **Username** | **Password** | **Purpose** |
|----------|--------------|--------------|-------------|
| 👑 **Admin** | `admin` | `admin123` | System administration & management |
| 👤 **Customer** | `customer1` | `pass123` | Food ordering & tracking |
| 👤 **Student** | `student1` | `student123` | Regular student user |
| 🏪 **Seller** | `seller1` | `seller123` | Food item management |

---

## ✨ **KEY FEATURES TO DEMONSTRATE**

### 🍽️ **Canteen Management**
- **Real-time menu browsing** with categories (breakfast, lunch, snacks)
- **Smart ordering system** with stock validation
- **Order tracking** (pending → completed → delivered)
- **Admin dashboard** with sales analytics
- **Inventory management** with low-stock alerts

### 👥 **User Management**  
- **Role-based access control** (Customer/Seller/Admin)
- **Secure authentication** with proper session management
- **User profile management** with role-specific features

### 📊 **Analytics & Reporting**
- **Real-time sales dashboard** with daily/weekly views
- **Stock level monitoring** with automatic alerts
- **Order history tracking** with detailed reports
- **User activity analytics** for business insights

---

## 🏗️ **Technical Architecture**

### **Frontend**
- **JavaFX 17.0.2** - Modern UI framework
- **FXML** - Declarative UI layouts
- **CSS styling** - Clean, minimal design
- **Responsive design** - Adapts to different screen sizes

### **Backend**
- **Java 23** - Latest LTS features
- **Maven** - Dependency management & build
- **JDBC** - Database connectivity
- **MVC Pattern** - Clean code architecture

### **Database**
- **MySQL 8.0** - Relational database
- **Connection pooling** - Optimized performance
- **Transaction management** - Data consistency
- **Schema: `onestopuiu`** - Organized data structure

---

## 📁 **Project Structure**

```
OneStopUIU/
├── 📂 src/main/java/com/example/onestopuiu/
│   ├── 🎯 OneStopUIUApplication.java      # Main application entry
│   ├── 📂 controller/                      # UI Controllers (MVC)
│   │   ├── LoginController.java           # User authentication
│   │   ├── AdminDashboardController.java  # Admin interface
│   │   ├── CanteenViewController.java     # Food ordering
│   │   └── MyOrdersController.java        # Order tracking
│   ├── 📂 model/                          # Data Models
│   │   ├── User.java                      # User entity
│   │   ├── FoodItem.java                  # Food item entity
│   │   └── FoodOrder.java                 # Order entity
│   ├── 📂 dao/                            # Data Access Layer
│   │   ├── UserDAO.java                   # User operations
│   │   ├── FoodItemDAO.java               # Food item operations
│   │   └── FoodOrderDAO.java              # Order operations
│   └── 📂 util/                           # Utilities
│       ├── DatabaseConnection.java        # DB connection
│       └── ImageCache.java                # Image optimization
├── 📂 src/main/resources/                 # Resources
│   └── 📂 com/example/onestopuiu/
│       ├── *.fxml                         # UI layouts
│       ├── styles.css                     # Styling
│       └── 📂 images/                     # Application images
└── 📋 pom.xml                            # Maven configuration
```

---

## 🎪 **PRESENTATION HIGHLIGHTS**

### **🎯 What to Show:**
1. **Login System** - Demonstrate different user roles
2. **Customer Flow** - Browse menu → Add to cart → Place order → Track status
3. **Admin Dashboard** - Real-time analytics, inventory management
4. **Seller Interface** - Product management, order fulfillment
5. **Database Integration** - Live data updates, stock management
6. **Error Handling** - Insufficient stock, invalid inputs
7. **UI/UX Design** - Clean, minimal, responsive interface

### **🎪 Demo Scenario:**
1. **Start as Customer** → Order coffee and sandwich
2. **Switch to Admin** → View dashboard, manage inventory
3. **Show Seller View** → Manage products, view orders
4. **Demonstrate Features** → Real-time updates, stock validation
5. **Show Database** → Live data changes, order history

---

## 🛠️ **TECHNICAL SETUP**

### **Prerequisites**
- ☕ **Java 23** (JDK)
- 🗄️ **MySQL 8.0** 
- 🔧 **Maven 3.8+**
- 💻 **IntelliJ IDEA 2025.1** (recommended)

### **Database Setup**
```sql
-- Create database
CREATE DATABASE onestopuiu;

-- Import demo data (automatic on first run)
-- Run: PresentationSetup.main() for demo data
```

### **Environment Variables**
```bash
JAVA_HOME=C:\Program Files\Java\jdk-23
MAVEN_HOME=C:\Program Files\Apache\maven
```

---

## 🔧 **TROUBLESHOOTING**

### **Common Issues & Solutions**

| **Issue** | **Solution** |
|-----------|--------------|
| JavaFX modules not found | Use provided run configurations |
| Database connection failed | Check MySQL service, credentials |
| Compilation errors | Run `mvnw clean compile` |
| IntelliJ not recognizing project | Reimport Maven project |

### **Quick Fixes**
```bash
# Clean and rebuild
.\mvnw.cmd clean compile

# Reset demo data
.\mvnw.cmd exec:java -Dexec.mainClass="com.example.onestopuiu.util.PresentationSetup"

# Force refresh IntelliJ
File → Reload Gradle/Maven Projects
```

---

## 🎯 **PROJECT HIGHLIGHTS**

### **🏆 Technical Excellence**
- ✅ **Modern Java 23** with latest features
- ✅ **Clean MVC architecture** for maintainability  
- ✅ **Responsive JavaFX UI** with FXML
- ✅ **Robust database design** with proper relationships
- ✅ **Error handling & validation** throughout
- ✅ **Resource management** with automatic cleanup

### **🎨 Design Excellence**
- ✅ **Minimal, clean UI** with consistent styling
- ✅ **Intuitive user experience** for all roles
- ✅ **Professional color scheme** 
- ✅ **Responsive layouts** that adapt to content
- ✅ **Clear navigation** and user feedback

### **🚀 Performance Features**
- ✅ **Efficient database queries** with prepared statements
- ✅ **Image caching** for better performance
- ✅ **Connection pooling** for database optimization
- ✅ **Background processing** for smooth UI
- ✅ **Memory management** with proper resource cleanup

---

## 📞 **SUPPORT & CONTACT**

**Project Team:** UIU Computer Science Students  
**Institution:** United International University (UIU)  
**Presentation Date:** October 8, 2025  

---

## 🏆 **FINAL NOTES FOR PRESENTATION**

> **OneStopUIU** demonstrates proficiency in:
> - **Object-Oriented Programming** with Java
> - **Database Design & Management** with MySQL
> - **User Interface Development** with JavaFX
> - **Software Architecture** with MVC pattern
> - **Project Management** with Maven
> - **Version Control** with Git

**🎯 Ready for demonstration! Good luck with your presentation! 🎉**
---
---


---

## Table of Contents
- [Features](#features)
- [Technical Stack](#technical-stack)
- [System Architecture](#system-architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Database Configuration](#database-configuration)
- [Running & Testing](#running--testing)
- [Project Structure](#project-structure)
- [Module/Class Overview](#moduleclass-overview)
- [User Roles](#user-roles)
- [Troubleshooting & FAQ](#troubleshooting--faq)
- [Contributing](#contributing)
- [License](#license)

---

## Features

### Canteen Section
- View and order from the canteen menu
- Track food order history
- Real-time order notifications
- Daily sales reports
- Menu management (Admin)
- Order status tracking

### General Features
- User authentication (Customer/Admin/Seller roles)
- Clean minimal design interface
- Real-time inventory updates
- Secure data management
- Responsive JavaFX interface with minimal color palette

## Technical Stack

- **Backend**: Java 17+
- **Frontend**: JavaFX
- **Database**: MySQL
- **Build Tool**: Maven
- **Other**: FXML for UI layouts, JDBC for database connectivity

## System Architecture

- **MVC Pattern**: The application follows the Model-View-Controller (MVC) architecture.
- **JavaFX**: Used for building the user interface.
- **DAO Layer**: Handles all database operations.
- **Utility Classes**: For image caching, scheduling, and database connections.
- **FXML**: UI layouts are defined in FXML files for separation of design and logic.

```
[User] <-> [JavaFX UI (View)] <-> [Controller] <-> [Model/DAO] <-> [MySQL Database]
```

## Prerequisites

- JDK 17 or higher
- JavaFX SDK (matching your JDK version)
- MySQL Server
- Maven
- (Optional) IDE: IntelliJ IDEA, Eclipse, or VS Code

## Installation

1. **Clone the repository**
   ```bash
   git clone [repository-url]
   cd OneStopUIU
   ```

2. **Database Setup**
   - Install MySQL Server
   - Create a database named `onestopuiu`
   - Import the schema from `src/main/resources/schema.sql` (or your provided schema)
   - Example (MySQL CLI):
     ```sql
     CREATE DATABASE onestopuiu;
     USE onestopuiu;
     SOURCE path/to/schema.sql;
     ```

3. **Configure Database Connection**
   - Edit the database connection settings in `DatabaseConnection.java` (typically in `src/main/java/com/example/onestopuiu/util/DatabaseConnection.java`).
   - Set your MySQL username, password, and database URL.

4. **Build and Run**
   ```bash
   mvn clean install
   mvn javafx:run
   ```
   Or, run from your IDE using the main class: `com.example.onestopuiu.OneStopUIUApplication`

## Database Configuration

- Ensure MySQL is running and accessible.
- Update credentials in the code if needed.
- The schema file should create all necessary tables for users, orders, items, etc.
- For development, you can use a local MySQL instance; for production, configure remote access as needed.

## Running & Testing

- **Run the Chat Server First:**
  - Move the OneStopUIU-Server folder from the project directory and place it somewhere else 
  - Navigate to the chat server directory (e.g., `src/main/java/com/example/onestopuiu/server/chatserver/`).
  - Compile and run the chat server (a `main` method in a `ChatServer.java` file).
  - Ensure the chat server is running and listening on the correct port (default: 5000).
- **Now back to the Project Directory** 
- **Run via Maven**: `mvn javafx:run`
- **Run via IDE**: Right-click `OneStopUIUApplication.java` and select 'Run'.
- **Testing**: Manual testing via the UI. (Automated tests can be added as needed.)
- **Stopping**: Close the application window or stop the process in your IDE/terminal.

## Project Structure

```


OneStopUIU/
│
├── pom.xml
├── README.md
├── mvnw / mvnw.cmd
├── .gitignore
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── onestopuiu/
│       │               │
│       │               ├── controller/      # JavaFX controllers for all UI screens
│       │               │   ├── AdminBaseController.java
│       │               │   ├── AdminCanteenOrdersController.java
│       │               │   ├── AdminDashboardController.java
│       │               │   ├── AdminGroceryOrdersController.java
│       │               │   ├── AdminSellerRequestsController.java
│       │               │   ├── CanteenViewController.java
│       │               │   ├── ChatController.java
│       │               │   ├── CustomerBaseController.java
│       │               │   ├── CustomerOrdersController.java
│       │               │   ├── EShoppingOrdersController.java
│       │               │   ├── EShoppingProductDetailsController.java
│       │               │   ├── EShoppingProductsManagerController.java
│       │               │   ├── EShoppingViewController.java
│       │               │   ├── FoodItemsManagerController.java
│       │               │   ├── GroceryItemsManagerController.java
│       │               │   ├── GroceryViewController.java
│       │               │   ├── ItemDetailsController.java
│       │               │   ├── LoginController.java
│       │               │   ├── MyOrdersController.java
│       │               │   ├── SectionSelectorController.java
│       │               │   ├── SellerBaseController.java
│       │               │   ├── SellerDashboardController.java
│       │               │   ├── SignupController.java
│       │               │
│       │               ├── dao/             # Data Access Objects for DB operations
│       │               │   ├── DAO.java
│       │               │   ├── EShoppingOrderDAO.java
│       │               │   ├── EShoppingProductDAO.java
│       │               │   ├── FoodItemDAO.java
│       │               │   ├── FoodOrderDAO.java
│       │               │   ├── GroceryItemDAO.java
│       │               │   ├── GroceryOrderDAO.java
│       │               │   ├── SellerRequestDAO.java
│       │               │   ├── UserDAO.java
│       │               │
│       │               ├── model/              # Data models/entities
│       │               │   ├── EShoppingOrder.java
│       │               │   ├── EShoppingProduct.java
│       │               │   ├── FoodItem.java
│       │               │   ├── FoodOrder.java
│       │               │   ├── FoodOrderItem.java
│       │               │   ├── GroceryItem.java
│       │               │   ├── GroceryOrder.java
│       │               │   ├── GroceryOrderItem.java
│       │               │   ├── SellerRequest.java
│       │               │   ├── User.java
│       │               │
│       │               ├── util/               # Utility classes
│       │               │   ├── CartManager.java
│       │               │   ├── ChatLauncher.java
│       │               │   ├── DatabaseConnection.java
│       │               │   ├── ImageCache.java
│       │               │   ├── ImageUtils.java
│       │               │   ├── ImgBBUploader.java
│       │               │   ├── OrderSchedulerService.java
│       │               │
│       │               ├── OneStopUIUApplication.java   # Main entry point
│       │               ├── AI_instructions.md
│       │               ├── OneStopUIU_project_requirements.md
│       │
│       ├── resources/
│       │   └── com/
│       │       └── example/
│       │           └── onestopuiu/
│       │               │
│       │               ├── FXML files for all screens:
│       │               │   ├── admin-canteen-orders.fxml
│       │               │   ├── admin-dashboard.fxml
│       │               │   ├── admin-grocery-orders.fxml
│       │               │   ├── admin-seller-requests.fxml
│       │               │   ├── canteen-view.fxml
│       │               │   ├── chat-view.fxml
│       │               │   ├── customer-orders.fxml
│       │               │   ├── eshopping-orders.fxml
│       │               │   ├── eshopping-product-details.fxml
│       │               │   ├── eshopping-products-manager.fxml
│       │               │   ├── eshopping-view.fxml
│       │               │   ├── food-items-manager.fxml
│       │               │   ├── grocery-items-manager.fxml
│       │               │   ├── grocery-view.fxml
│       │               │   ├── hello-view.fxml
│       │               │   ├── item-details.fxml
│       │               │   ├── login.fxml
│       │               │   ├── my-orders.fxml
│       │               │   ├── section-selector.fxml
│       │               │   ├── seller-dashboard.fxml
│       │               │   ├── signup.fxml
│       │               │   ├── styles.css
│       │               │
│       │               ├── images/             # UI and default images
│       │               │   ├── default-item.png
│       │               │   ├── loginView.jpg
│       │               │   ├── loginView.png
│       │               │
│       │               ├── uploads/            # Uploaded product images
│       │               │   ├── 35e1f170-02d5-4a1a-82e4-d483db7f3f41.jpg
│       │               │   ├── 884c5ce4-b3cd-4615-a408-343490ecf275.jpg
│       │               │   ├── 9444ff50-eeb2-48b8-9ae8-4ddb154a1ed0.jpeg
│       │               │   ├── a0cbf96b-aa2c-4fc7-b2fc-7e640b0bb9fc.jpg
│       │               │   ├── f38eddfd-8fe3-464e-a9b0-3533345b622a.jpeg
│       │               │
│       │
│       ├── module-info.java
│
├── target/                                   # Build output (generated) 
```

## Module/Class Overview

- **controller/**: Handles UI logic for each screen (e.g., login, orders, admin dashboard, eShopping views).
- **model/**: Contains data classes representing users, items, orders, and eShopping products/orders.
- **dao/**: Data Access Objects for CRUD operations with the database, including eShopping products and orders.
- **util/**: Utility classes for image caching, database connections, scheduling, etc.
- **resources/**: FXML files for UI layouts, images, and stylesheets, including eShopping views.
- **OneStopUIUApplication.java**: Main entry point for the JavaFX application.
- **controller/ChatController.java**: Handles the chat UI and communication logic for real-time messaging between users.
- **util/ChatLauncher.java**: Utility to launch the chat window and initialize chat sessions.
- **resources/chat-view.fxml**: FXML layout for the chat window.

## User Roles

### Customer
- Place food orders
- Purchase grocery items
- Place eShopping orders and chat with the seller
- View order history (food, grocery, eShopping)
- Track order status

### Seller
- Add and manage eShopping products
- View and process eShopping orders
- Monitor product stock and receive low stock alerts
- View sales reports

### Admin
- Manage food menu
- Handle grocery inventory
- View sales reports
- Process orders
- Monitor stock levels
- Manages Seller Requests

## Troubleshooting & FAQ

**Q: JavaFX not found or not launching?**
- Ensure JavaFX SDK is installed and configured in your IDE or build tool.
- Check your `pom.xml` for JavaFX dependencies.

**Q: Database connection errors?**
- Verify MySQL is running and credentials in `DatabaseConnection.java` are correct.
- This project uses the MySql in "4306" port instead of regular "3306" port.
- Ensure the schema is imported and tables exist.

**Q: UI not displaying correctly?**
- Make sure FXML files are in the correct resource path.
- Check for missing images or resources.

**Q: How to reset the database?**
- Drop and recreate the `onestopuiu` database, then re-import the schema.

**Q: How to add new features or sections?**
- Follow the MVC pattern: add new models, controllers, DAOs, and FXML views as needed.

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License. 
