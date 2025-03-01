package proiect_mip_server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.sql.Date;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.*;

public class WeatherServer {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serverul rulează pe portul " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Eroare la pornirea serverului: " + e.getMessage());
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Gson gson = new Gson();

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }


@Override
public void run() {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

        String request;
        while ((request = in.readLine()) != null) {
            System.out.println("Cerere primită: " + request);

            if ("EXIT".equalsIgnoreCase(request)) {
                System.out.println("Clientul a închis conexiunea.");
                break;
            }

            String response;
            try {
                // Tratează cererea și răspunsul
                if (request.startsWith("GET_WEATHER_BY_NAME")) {
                    String location = request.split(" ", 2)[1];
                    response = getWeatherForLocation(location);
                } else if (request.startsWith("GET_WEATHER_BY_COORDS")) {
                    String[] parts = request.split(" ");
                    double latitude = Double.parseDouble(parts[1]);
                    double longitude = Double.parseDouble(parts[2]);
                    response = getWeatherForCoords(latitude, longitude);
                } else if (request.startsWith("GET_SORTED_WEATHER_BY_NAME")) {
                    String location = request.split(" ", 2)[1];
                    response = getSortedWeatherForLocation(location);
                } else if (request.startsWith("ADD_NEW_INFO")) {
                    String[] parts = request.split(" ", 7);
                    String location = parts[1];
                    String date = parts[2];
                    double temperature = Double.parseDouble(parts[3]);
                    String condition = parts[4];
                    double precipitation = Double.parseDouble(parts[5]);
                    double windSpeed = Double.parseDouble(parts[6]);
                    response = addWeatherData(location, date, temperature, condition, precipitation, windSpeed);
                } else if (request.startsWith("MODIFY_INFO")) {
                    String[] parts = request.split(" ", 6);
                    int weatherId = Integer.parseInt(parts[1]);
                    double temperature = Double.parseDouble(parts[2]);
                    String condition = parts[3];
                    double precipitation = Double.parseDouble(parts[4]);
                    double windSpeed = Double.parseDouble(parts[5]);
                    response = updateWeatherData(weatherId, temperature, condition, precipitation, windSpeed);
                } else if (request.startsWith("UPLOAD_JSON")) {
                    String jsonData = request.split(" ", 2)[1];
                    response = uploadJsonData(jsonData);
                } else if (request.startsWith("GET_ALL_WEATHER_DATA")) {
                    response = getAllWeatherData();
                } else if (request.startsWith("ADD_NEW_LOCATION")) {
                    String[] parts = request.split(" ", 4);
                    String cityName = parts[1];
                    double latitude = Double.parseDouble(parts[2]);
                    double longitude = Double.parseDouble(parts[3]);
                    response = addNewLocation(cityName, latitude, longitude);
                }  else if (request.startsWith("DELETE_LOCATION")) {
                String locationName = request.split(" ", 2)[1];
                response = deleteLocation(locationName);
                } else {
                    response = "Comandă necunoscută!";
                }
            } finally {
                // Închide conexiunea cu baza de date după fiecare cerere
                DatabaseConnection.closeConnection();
            }

            out.println(response);
            out.println("END_OF_MESSAGE"); // Marchează sfârșitul răspunsului
        }
    } catch (IOException e) {
        System.err.println("Eroare la comunicarea cu clientul: " + e.getMessage());
    } finally {
        // Închide conexiunea cu baza de date când clientul termină sesiunea
        DatabaseConnection.closeConnection();
    }
}

    private String getWeatherForLocation(String location) {
        StringBuilder result = new StringBuilder();

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Verificăm dacă locația există în baza de date
            String checkLocationQuery = "SELECT COUNT(*) FROM locations WHERE name = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkLocationQuery)) {
                checkStmt.setString(1, location);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    return "Locația '" + location + "' nu există în baza de date.";
                }
            }
            // Iterează pentru ziua curentă și următoarele 3 zile
            for (int i = 0; i <= 3; i++) {
                String query =
                        "SELECT wd.date, wd.temperature, wd.condition, wd.cantitate_precipitatii, wd.viteza_vant " +
                                "FROM weather_data wd " +
                                "JOIN locations l ON wd.location_id = l.id " +
                                "WHERE l.name = ? AND wd.date = CURRENT_DATE + ?";

                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, location); // Setează numele locației
                    stmt.setInt(2, i);           // Setează ziua curentă + i
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        // Dacă există date pentru ziua respectivă
                        Date date = rs.getDate("date");
                        double temp = rs.getDouble("temperature");
                        String condition = rs.getString("condition");
                        double precip = rs.getDouble("cantitate_precipitatii");
                        double wind = rs.getDouble("viteza_vant");

                        result.append("Data: ").append(date.toString())
                                .append(", Temperatura: ").append(temp).append("°C")
                                .append(", Condiție: ").append(condition)
                                .append(", Precipitații: ").append(precip).append(" mm")
                                .append(", Vânt: ").append(wind).append(" km/h\n");
                    } else {
                        // Dacă nu există date pentru ziua respectivă
                        result.append("Nu există date pentru ziua curentă + ").append(i).append("\n");
                    }
                } catch (SQLException e) {
                    result.append("Eroare la interogarea bazei de date pentru ziua curentă + ")
                            .append(i).append(": ").append(e.getMessage()).append("\n");
                }
            }
        } catch (SQLException e) {
            result.append("Eroare la conexiunea cu baza de date: ").append(e.getMessage());
        }

        return result.toString();
    }
    private String getWeatherForCoords(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Găsirea celei mai apropiate locații
            String locationQuery =
                    "SELECT id, name, SQRT(POWER(latitude - ?, 2) + POWER(longitude - ?, 2)) AS distance " +
                            "FROM locations " +
                            "ORDER BY distance LIMIT 1";

            try (PreparedStatement locationStmt = connection.prepareStatement(locationQuery)) {
                locationStmt.setDouble(1, latitude);
                locationStmt.setDouble(2, longitude);

                ResultSet locationRs = locationStmt.executeQuery();

                if (locationRs.next()) {
                    int locationId = locationRs.getInt("id");
                    String locationName = locationRs.getString("name");
                    double distance = locationRs.getDouble("distance");

                    if (distance > 10) {
                        return "Nu există nicio locație la o distanță mai mică de 10 unități.";
                    }

                    result.append("Cea mai apropiată locație: ").append(locationName)
                            .append(" (Distanță: ").append(String.format("%.3f", distance)).append(" unități)\n\n");

                    // Iterăm pentru ziua curentă și următoarele 3 zile
                    for (int i = 0; i <= 3; i++) {
                        String weatherQuery =
                                "SELECT date, temperature, condition, cantitate_precipitatii, viteza_vant " +
                                        "FROM weather_data " +
                                        "WHERE location_id = ? AND date = CURRENT_DATE + ?";

                        try (PreparedStatement weatherStmt = connection.prepareStatement(weatherQuery)) {
                            weatherStmt.setInt(1, locationId);
                            weatherStmt.setInt(2, i);

                            ResultSet weatherRs = weatherStmt.executeQuery();

                            if (weatherRs.next()) {
                                Date date = weatherRs.getDate("date");
                                double temp = weatherRs.getDouble("temperature");
                                String condition = weatherRs.getString("condition");
                                double precip = weatherRs.getDouble("cantitate_precipitatii");
                                double wind = weatherRs.getDouble("viteza_vant");

                                result.append("Data: ").append(date.toString())
                                        .append(", Temperatura: ").append(temp).append("°C")
                                        .append(", Condiție: ").append(condition)
                                        .append(", Precipitații: ").append(precip).append(" mm")
                                        .append(", Vânt: ").append(wind).append(" km/h\n");
                            } else {
                                result.append("Nu există date pentru ziua curentă + ").append(i)
                                        .append(" în locația: ").append(locationName).append("\n");
                            }
                        }
                    }
                } else {
                    return "Nu a fost găsită nicio locație în apropiere.";
                }
            }
        } catch (SQLException e) {
            result.append("Eroare la interogarea bazei de date: ").append(e.getMessage());
        }

        return result.toString();
    }

    private String getSortedWeatherForLocation(String location) {
        List<WeatherData> dataList = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Verificăm dacă locația există în baza de date
            String checkLocationQuery = "SELECT COUNT(*) FROM locations WHERE name = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkLocationQuery)) {
                checkStmt.setString(1, location);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    return "Locația '" + location + "' nu există în baza de date.";
                }
            }
            // Iterăm pentru ziua curentă și următoarele 3 zile
            for (int i = 0; i <= 3; i++) {
                String query =
                        "SELECT wd.date, wd.temperature, wd.condition, wd.cantitate_precipitatii, wd.viteza_vant " +
                                "FROM weather_data wd " +
                                "JOIN locations l ON wd.location_id = l.id " +
                                "WHERE l.name = ? AND wd.date = CURRENT_DATE + ?";

                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, location);
                    stmt.setInt(2, i); // Ziua curentă + i
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        // Creăm obiecte `WeatherData` și le adăugăm în listă
                        WeatherData data = new WeatherData(
                                rs.getDate("date").toString(),
                                rs.getDouble("temperature"),
                                rs.getString("condition"),
                                rs.getDouble("cantitate_precipitatii"),
                                rs.getDouble("viteza_vant")
                        );
                        dataList.add(data);
                    }
                }
            }

            // Apelează funcția de sortare și formatare
            return sortAndFormatWeatherData(dataList);

        } catch (SQLException e) {
            return "Eroare la interogarea bazei de date: " + e.getMessage();
        }
    }

    private String sortAndFormatWeatherData(List<WeatherData> dataList) {
        // Utilizăm stream-uri pentru sortare descrescătoare după temperatură
        List<WeatherData> sortedList = dataList.stream()
                .sorted(Comparator.comparingDouble(WeatherData::getTemperature).reversed())
                .collect(Collectors.toList());

        // Formatare pentru afișare
        StringBuilder result = new StringBuilder("Datele meteo sortate:\n");
        sortedList.forEach(data -> result.append(data).append("\n"));

        return result.toString();
    }

    private String addWeatherData(String location, String date, double temperature, String condition, double precipitation, double windSpeed) {
        Set<WeatherData> existingDataSet = new HashSet<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Obține toate datele existente pentru locația specificată
            String fetchQuery = "SELECT wd.date, wd.temperature, wd.condition, wd.cantitate_precipitatii, wd.viteza_vant " +
                    "FROM weather_data wd " +
                    "JOIN locations l ON wd.location_id = l.id " +
                    "WHERE l.name = ?";

            try (PreparedStatement fetchStmt = connection.prepareStatement(fetchQuery)) {
                fetchStmt.setString(1, location);
                ResultSet rs = fetchStmt.executeQuery();

                while (rs.next()) {
                    WeatherData existingData = new WeatherData(
                            rs.getString("date"),
                            rs.getDouble("temperature"),
                            rs.getString("condition"),
                            rs.getDouble("cantitate_precipitatii"),
                            rs.getDouble("viteza_vant")
                    );
                    existingDataSet.add(existingData);
                }
            }

            // Creează un nou obiect WeatherData pentru datele primite
            WeatherData newData = new WeatherData(date, temperature, condition, precipitation, windSpeed);

            // Verifică dacă datele sunt deja în Set
            for (WeatherData existingData : existingDataSet) {
                if (existingData.getDate().equals(date)) {
                    // Dacă doar data este la fel, actualizează restul datelor
                    if (!existingData.equals(newData)) {
                        // Actualizează datele
                        String updateQuery = "UPDATE weather_data " +
                                "SET temperature = ?, condition = ?::weather_condition, cantitate_precipitatii = ?, viteza_vant = ? " +
                                "WHERE date = TO_DATE(?, 'YYYY-MM-DD') AND location_id = (SELECT id FROM locations WHERE name = ?)";

                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setDouble(1, temperature);
                            updateStmt.setString(2, condition);
                            updateStmt.setDouble(3, precipitation);
                            updateStmt.setDouble(4, windSpeed);
                            updateStmt.setString(5, date);
                            updateStmt.setString(6, location);

                            int rows = updateStmt.executeUpdate();
                            if (rows > 0) {
                                return "Data există deja, dar informațiile au fost actualizate!";
                            } else {
                                return "Eroare la actualizarea datelor.";
                            }
                        }
                    }
                    // Dacă toate datele sunt identice
                    return "Datele sunt deja în baza de date!";
                }
            }

            // Dacă data nu există deloc, inserează rândul nou
            String insertQuery = "INSERT INTO weather_data (location_id, date, temperature, condition, cantitate_precipitatii, viteza_vant) " +
                    "SELECT id, TO_DATE(?, 'YYYY-MM-DD'), ?, ?::weather_condition, ?, ? FROM locations WHERE name = ?";

            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, date);
                insertStmt.setDouble(2, temperature);
                insertStmt.setString(3, condition);
                insertStmt.setDouble(4, precipitation);
                insertStmt.setDouble(5, windSpeed);
                insertStmt.setString(6, location);

                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    return "Datele au fost adăugate cu succes!";
                } else {
                    return "Locația nu a fost găsită!";
                }
            }

        } catch (SQLException e) {
            return "Eroare la adăugarea datelor: " + e.getMessage();
        }
    }
    private String updateWeatherData(int weatherId, double temperature, String condition, double precipitation, double windSpeed) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "UPDATE weather_data SET temperature = ?, condition = ?::weather_condition, " +
                             "cantitate_precipitatii = ?, viteza_vant = ? WHERE id = ?")) {

            stmt.setDouble(1, temperature);
            stmt.setString(2, condition);
            stmt.setDouble(3, precipitation);
            stmt.setDouble(4, windSpeed);
            stmt.setInt(5, weatherId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                return "Datele au fost actualizate cu succes!";
            } else {
                return "ID-ul datelor meteo nu a fost găsit!";
            }

        } catch (SQLException e) {
            return "Eroare la actualizarea datelor: " + e.getMessage();
        }
    }

    private String uploadJsonData(String jsonData) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            Type listType = new TypeToken<List<WeatherData>>() {}.getType();
            List<WeatherData> weatherDataList = gson.fromJson(jsonData, listType);

            String query = "INSERT INTO weather_data (location_id, date, temperature, condition, cantitate_precipitatii, viteza_vant) " +
                    "SELECT id, TO_DATE(?, 'YYYY-MM-DD'), ?, ?::weather_condition, ?, ? FROM locations WHERE name = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                for (WeatherData data : weatherDataList) {
                    stmt.setString(1, data.getDate());
                    stmt.setDouble(2, data.getTemperature());
                    stmt.setString(3, data.getCondition());
                    stmt.setDouble(4, data.getPrecipitation());
                    stmt.setDouble(5, data.getWindSpeed());
                    stmt.setString(6, data.getLocation());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            return "Datele din JSON au fost încărcate cu succes!";
        } catch (Exception e) {
            return "Eroare la încărcarea datelor din JSON: " + e.getMessage();
        }
    }
    private String getAllWeatherData() {
        StringBuilder result = new StringBuilder("Toate datele meteo:\n");

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT l.name AS location, wd.date, wd.temperature, wd.condition, wd.cantitate_precipitatii, wd.viteza_vant " +
                             "FROM weather_data wd " +
                             "JOIN locations l ON wd.location_id = l.id")) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String location = rs.getString("location");
                String date = rs.getDate("date").toString();
                double temperature = rs.getDouble("temperature");
                String condition = rs.getString("condition");
                double precipitation = rs.getDouble("cantitate_precipitatii");
                double windSpeed = rs.getDouble("viteza_vant");

                result.append(String.format("Locație: %s, Data: %s, Temperatură: %.2f°C, Condiție: %s, Precipitații: %.2f mm, Viteza vântului: %.2f km/h\n",
                        location, date, temperature, condition, precipitation, windSpeed));
            }

        } catch (SQLException e) {
            return "Eroare la interogarea datelor meteo: " + e.getMessage();
        }

        return result.toString();
    }
    private String addNewLocation(String cityName, double latitude, double longitude) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Verificăm dacă orașul există deja
            String checkQuery = "SELECT COUNT(*) FROM locations WHERE name = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, cityName);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Orașul '" + cityName + "' există deja în baza de date.";
                }
            }

            // Adăugăm orașul în baza de date
            String insertQuery = "INSERT INTO locations (name, latitude, longitude) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, cityName);
                insertStmt.setDouble(2, latitude);
                insertStmt.setDouble(3, longitude);

                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    return "Orașul '" + cityName + "' a fost adăugat cu succes!";
                } else {
                    return "Eroare la adăugarea orașului în baza de date.";
                }
            }
        } catch (SQLException e) {
            return "Eroare la interacțiunea cu baza de date: " + e.getMessage();
        }
    }
    private String deleteLocation(String locationName) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "DELETE FROM locations WHERE name = ?")) {

            stmt.setString(1, locationName);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                return "Locația '" + locationName + "' a fost ștearsă cu succes din baza de date!";
            } else {
                return "Eroare: Locația '" + locationName + "' nu există în baza de date.";
            }
        } catch (SQLException e) {
            return "Eroare la ștergerea locației: " + e.getMessage();
        }
    }

}
