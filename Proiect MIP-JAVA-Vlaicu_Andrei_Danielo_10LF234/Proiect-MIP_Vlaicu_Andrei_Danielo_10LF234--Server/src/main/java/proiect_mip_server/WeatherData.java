package proiect_mip_server;

import java.util.Objects;

public class WeatherData {
    private String location;
    private String date;
    private double temperature;
    private String condition;
    private double precipitation;
    private double windSpeed;

    public WeatherData(String date, double temperature, String condition, double precipitation, double windSpeed) {
        this.date = date;
        this.temperature = temperature;
        this.condition = condition;
        this.precipitation = precipitation;
        this.windSpeed = windSpeed;
    }

    // Getters și Setters
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherData that = (WeatherData) o;
        return Double.compare(that.temperature, temperature) == 0 &&
                Double.compare(that.precipitation, precipitation) == 0 &&
                Double.compare(that.windSpeed, windSpeed) == 0 &&
                Objects.equals(date, that.date) &&
                Objects.equals(condition, that.condition) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, temperature, condition, precipitation, windSpeed, location);
    }
    @Override
    public String toString() {
        return "Data: " + date +
                ", Temperatură: " + temperature + "°C" +
                ", Condiție: " + condition +
                ", Precipitații: " + precipitation + " mm" +
                ", Viteza vântului: " + windSpeed + " km/h";
    }
}
