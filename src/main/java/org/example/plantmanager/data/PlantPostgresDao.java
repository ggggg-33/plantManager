package org.example.plantmanager.data;

import org.example.plantmanager.business.Plant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlantPostgresDao implements PlantDao {
    private final Connection connection;

    public PlantPostgresDao(String url, String user, String password) {
        try {
            System.out.println("Попытка подключения к: " + url);
            System.out.println("Пользователь: " + user);
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Подключение успешно!");
            createTableIfNotExists();
        } catch (SQLException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
            throw new RuntimeException("Ошибка подключения к базе данных PostgreSQL", e);
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS plants (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "planting_date DATE NOT NULL, " +
                "watering_frequency INTEGER NOT NULL, " +
                "last_watering_date DATE, " +
                "last_repotting_date DATE, " +
                "health_status VARCHAR(200), " +
                "growth_rate DECIMAL(5,2) DEFAULT 0, " +  // Убрано NOT NULL, добавлен DEFAULT
                "current_height DECIMAL(5,2) NOT NULL" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица plants создана или уже существует");
        }
    }

    @Override
    public void addPlant(Plant plant) {
        String sql = "INSERT INTO plants (name, planting_date, watering_frequency, " +
                "last_watering_date, last_repotting_date, health_status, growth_rate, current_height) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setPlantParameters(stmt, plant);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    plant.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления растения", e);
        }
    }

    @Override
    public void updatePlant(Plant plant) {
        String sql = "UPDATE plants SET name=?, planting_date=?, watering_frequency=?, " +
                "last_watering_date=?, last_repotting_date=?, health_status=?, growth_rate=?, current_height=? " +
                "WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setPlantParameters(stmt, plant);
            stmt.setLong(9, plant.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления растения", e);
        }
    }

    @Override
    public void deletePlant(long id) {
        String sql = "DELETE FROM plants WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления растения", e);
        }
    }

    @Override
    public List<Plant> getAllPlants() {
        List<Plant> plants = new ArrayList<>();
        String sql = "SELECT * FROM plants";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                plants.add(resultSetToPlant(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения растений", e);
        }

        return plants;
    }

    @Override
    public Plant getPlantById(long id) {
        String sql = "SELECT * FROM plants WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToPlant(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения растения по ID", e);
        }
        return null;
    }

    private void setPlantParameters(PreparedStatement stmt, Plant plant) throws SQLException {
        stmt.setString(1, plant.getName());
        stmt.setDate(2, Date.valueOf(plant.getPlantingDate()));
        stmt.setInt(3, plant.getWateringFrequency());
        stmt.setDate(4, plant.getLastWateringDate() != null ? Date.valueOf(plant.getLastWateringDate()) : null);
        stmt.setDate(5, plant.getLastRepottingDate() != null ? Date.valueOf(plant.getLastRepottingDate()) : null);
        stmt.setString(6, plant.getHealthStatus());
        stmt.setDouble(7, plant.getGrowthRate());
        stmt.setDouble(8, plant.getCurrentHeight());
    }

    private Plant resultSetToPlant(ResultSet rs) throws SQLException {
        Plant plant = new Plant();
        plant.setId(rs.getLong("id"));
        plant.setName(rs.getString("name"));
        plant.setPlantingDate(rs.getDate("planting_date").toLocalDate());
        plant.setWateringFrequency(rs.getInt("watering_frequency"));

        Date lastWatering = rs.getDate("last_watering_date");
        if (lastWatering != null) {
            plant.setLastWateringDate(lastWatering.toLocalDate());
        }

        Date lastRepotting = rs.getDate("last_repotting_date");
        if (lastRepotting != null) {
            plant.setLastRepottingDate(lastRepotting.toLocalDate());
        }

        plant.setHealthStatus(rs.getString("health_status"));
        plant.setGrowthRate(rs.getDouble("growth_rate"));
        plant.setCurrentHeight(rs.getDouble("current_height"));

        return plant;
    }
}