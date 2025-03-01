package proiect_mip_client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.InputMismatchException; // Import corect pentru InputMismatchException

public class WeatherClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectat la server.");
            System.out.println("Alege tipul de utilizator:");
            System.out.println("1. Client obișnuit");
            System.out.println("2. Admin");
            System.out.print("Tipul tău: ");
            int userType = scanner.nextInt();
            scanner.nextLine(); // Consumă newline-ul rămas

            if (userType == 1) {
                Boolean isOrdinaryClientRunning = true;
                while (isOrdinaryClientRunning) {
                    // Client obișnuit
                    System.out.println("Conectat ca client obișnuit. Alege o opțiune pentru vizualizarea vremii:");
                    System.out.println("1. Introdu numele locației");
                    System.out.println("2. Introdu latitudinea și longitudinea");
                    System.out.println("3. Afișează datele sortate după temperatură");
                    System.out.println("4. Iesire proces");
                    System.out.print("Opțiunea ta: ");

                    try {
                        int option = scanner.nextInt();
                        scanner.nextLine(); // Consumă newline-ul rămas

                        switch (option) {
                            case 1 -> {
                                System.out.print("Introdu numele locației: ");
                                String location = scanner.nextLine();
                                out.println("GET_WEATHER_BY_NAME " + location);

                                // Primește răspunsul de la server
                                printServerResponse(in);
                            }
                            case 2 -> {
                                try {
                                    System.out.print("Introdu latitudinea: ");
                                    double latitude = scanner.nextDouble();
                                    System.out.print("Introdu longitudinea: ");
                                    double longitude = scanner.nextDouble();
                                    out.println("GET_WEATHER_BY_COORDS " + latitude + " " + longitude);

                                    // Primește răspunsul de la server
                                    printServerResponse(in);
                                } catch (InputMismatchException e) {
                                    System.out.println("Eroare: Latitudinea și longitudinea trebuie să fie numere valide.");
                                    scanner.nextLine(); // Consumă intrarea invalidă
                                }
                            }
                            case 3 -> {
                                System.out.print("Introdu numele locației: ");
                                String location = scanner.nextLine();
                                out.println("GET_SORTED_WEATHER_BY_NAME " + location);

                                // Primește răspunsul de la server
                                printServerResponse(in);
                            }
                            case 4 -> {
                                out.println("EXIT");
                                isOrdinaryClientRunning = false;
                            }
                            default -> System.out.println("Opțiune invalidă.");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Eroare: Opțiunea trebuie să fie un număr valid.");
                        scanner.nextLine(); // Consumă intrarea invalidă
                    }
                }
            } else if (userType == 2) {
                Boolean isAdminRunning = true;
                while (isAdminRunning) {
                    // Admin
                    System.out.println("Conectat ca admin. Alege o opțiune:");
                    System.out.println("1. Adaugă date noi");
                    System.out.println("2. Modifică date existente");
                    System.out.println("3. Încarcă date JSON");
                    System.out.println("4. Vizualizare toate datele");
                    System.out.println("5. Adauga locatie noua");
                    System.out.println("6. Ieșire proces");
                    System.out.println("7. Șterge locație");
                    System.out.print("Opțiunea ta: ");

                    try {
                        int option = scanner.nextInt();
                        scanner.nextLine(); // Consumă newline-ul rămas

                        switch (option) {
                            case 1 -> {
                                try {
                                    System.out.print("Introdu numele locației: ");
                                    String location = scanner.nextLine();
                                    System.out.print("Introdu data (YYYY-MM-DD): ");
                                    String date = scanner.nextLine();
                                    System.out.print("Introdu temperatura: ");
                                    double temperature = scanner.nextDouble();
                                    scanner.nextLine(); // Consumă newline
                                    System.out.print("Introdu condiția: ");
                                    String condition = scanner.nextLine();
                                    System.out.print("Introdu cantitatea de precipitații: ");
                                    double precipitation = scanner.nextDouble();
                                    System.out.print("Introdu viteza vântului: ");
                                    double windSpeed = scanner.nextDouble();

                                    out.println("ADD_NEW_INFO " + location + " " + date + " " + temperature + " " +
                                            condition + " " + precipitation + " " + windSpeed);

                                    // Primește răspunsul de la server
                                    printServerResponse(in);
                                } catch (InputMismatchException e) {
                                    System.out.println("Eroare: Datele introduse sunt invalide. Te rog să încerci din nou.");
                                    scanner.nextLine(); // Consumă intrarea invalidă
                                }
                            }
                            case 2 -> {
                                try {
                                    System.out.print("Introdu ID-ul datelor meteo de modificat: ");
                                    int weatherId = scanner.nextInt();
                                    System.out.print("Introdu noua temperatură: ");
                                    double newTemperature = scanner.nextDouble();
                                    System.out.print("Introdu noua condiție: ");
                                    String newCondition = scanner.next();
                                    System.out.print("Introdu noua cantitate de precipitații: ");
                                    double newPrecipitation = scanner.nextDouble();
                                    System.out.print("Introdu noua viteză a vântului: ");
                                    double newWindSpeed = scanner.nextDouble();
                                    out.println("MODIFY_INFO " + weatherId + " " + newTemperature + " " +
                                            newCondition + " " + newPrecipitation + " " + newWindSpeed);

                                    // Primește răspunsul de la server
                                    printServerResponse(in);
                                } catch (InputMismatchException e) {
                                    System.out.println("Eroare: Datele introduse sunt invalide. Te rog să încerci din nou.");
                                    scanner.nextLine(); // Consumă intrarea invalidă
                                }
                            }
                            case 3 -> {
                                System.out.println("Introdu datele sub forma JSON. Datele trebuie sa fie sub forma:");
                                System.out.println("[{\"date\": \"YYYY-MM-DD\", \"temperature\": valoare_temperatura, " +
                                        "\"condition\": \"ConditieMeteo\", \"precipitation\": valoare_precipitatii, " +
                                        "\"windSpeed\": valoare_viteza_vant, \"location\": \"NumeLocatie\"}]");
                                System.out.print("Introdu datele sub forma JSON: ");
                                String jsonData = scanner.nextLine();
                                out.println("UPLOAD_JSON " + jsonData);

                                // Primește răspunsul de la server
                                printServerResponse(in);
                            }
                            case 4 -> {
                                out.println("GET_ALL_WEATHER_DATA");

                                // Primește răspunsul de la server
                                printServerResponse(in);
                            }
                            case 5 -> {
                                try {
                                    System.out.print("Introdu numele orașului: ");
                                    String cityName = scanner.nextLine();
                                    System.out.print("Introdu latitudinea: ");
                                    double latitude = scanner.nextDouble();
                                    System.out.print("Introdu longitudinea: ");
                                    double longitude = scanner.nextDouble();
                                    out.println("ADD_NEW_LOCATION " + cityName + " " + latitude + " " + longitude);

                                    // Primește răspunsul de la server
                                    printServerResponse(in);
                                } catch (InputMismatchException e) {
                                    System.out.println("Eroare: Latitudinea și longitudinea trebuie să fie numere valide.");
                                    scanner.nextLine(); // Consumă intrarea invalidă
                                }
                            }
                            case 6 -> {
                                out.println("EXIT");
                                isAdminRunning = false;
                            }
                            case 7 -> {
                                try {
                                    System.out.print("Introdu numele locației pe care dorești să o ștergi: ");
                                    String locationName = scanner.nextLine();
                                    out.println("DELETE_LOCATION " + locationName);

                                    // Primește răspunsul de la server
                                    printServerResponse(in);
                                } catch (Exception e) {
                                    System.out.println("Eroare: Nu am putut procesa cererea de ștergere. Încearcă din nou.");
                                }
                            }

                            default -> System.out.println("Opțiune invalidă.");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Eroare: Opțiunea trebuie să fie un număr valid.");
                        scanner.nextLine(); // Consumă intrarea invalidă
                    }
                }
            } else {
                System.out.println("Tip de utilizator invalid.");
                return;
            }
        } catch (IOException e) {
            System.err.println("Eroare la conectarea cu serverul: " + e.getMessage());
        }
    }

    private static void printServerResponse(BufferedReader in) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while (!(line = in.readLine()).equals("END_OF_MESSAGE")) { // Citim până întâlnim END_OF_MESSAGE
            response.append(line).append("\n");
        }
        System.out.println("Răspuns de la server:\n" + response);
    }
}
