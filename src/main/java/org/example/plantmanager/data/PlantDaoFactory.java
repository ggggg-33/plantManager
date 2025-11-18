package org.example.plantmanager.data;

public class PlantDaoFactory {
    public PlantDao createCsvDao() {
        return new PlantCsvDao("plants.csv");
    }

    public PlantDao createPostgresDao(String url, String user, String password) {
        return new PlantPostgresDao(url, user, password);
    }

    public PlantDao createPostgresDao() {
        // Используем значения по умолчанию или из переменных окружения
        String url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:postgresql://localhost:5432/plants";
        String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
        String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "123456789";

        return new PlantPostgresDao(url, user, password);
    }
}