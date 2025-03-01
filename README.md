# SimpleWeatherApplication  

## 📌 Overview  
SimpleWeatherApplication is a **Java-based client-server application** that interacts with a **PostgreSQL database** to provide weather-related data. The system supports two types of users: **Admin** and **Regular Client**.  

## 🛠️ Features  

### **🔹 Admin Functionalities**  
The **Admin** has full control over the database, allowing them to:  
- **Manage city records**: Add new locations or update existing ones (name, coordinates).  
- **Manage weather conditions**:  
  - Store **weather data** for different cities, including:  
    - **Date**  
    - **Temperature**  
    - **Weather state** (e.g., rainy, sunny, windy, etc.)  
    - **Precipitation amount**  
    - **Wind speed**  
  - **Update weather conditions** for a specific date.  
  - **Add new weather data** for any city.  
  - **View the entire database**, including all recorded cities and weather conditions.  

### **🔹 Regular Client Functionalities**  
A **regular client** can:  
- **View precipitation levels** in selected locations.  
- **Sort locations based on temperature** to see the warmest/coldest places.  
- **Retrieve weather forecasts** for **the current day and the next three days**.  

### **🔹 Database Connection Management**  
- The application establishes a connection to the **PostgreSQL database** at runtime.  
- **The connection is closed when exiting the application** to prevent resource leaks.  

## 🔧 Technologies Used  
- **Java** for the client-server architecture.  
- **PostgreSQL** for database storage and management.  
- **JDBC** for database connectivity.  

## 🚀 Future Improvements  
- Implement a graphical user interface (GUI) for easier interaction.  
- Add authentication and role-based access control for enhanced security.  
- Include historical weather data analysis.  
