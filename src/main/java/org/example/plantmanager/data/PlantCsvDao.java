package org.example.plantmanager.data;

import org.example.plantmanager.business.Plant;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PlantCsvDao implements PlantDao {
    private final String filePath;
    private final AtomicLong idGenerator = new AtomicLong(1);
    private static final String CSV_HEADER = "id,name,plantingDate,wateringFrequency,lastWateringDate,lastRepottingDate,healthStatus,growthRate,currentHeight";

    public PlantCsvDao(String filePath) {
        this.filePath = filePath;
        initializeFile();
    }

    private void initializeFile() {
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (!file.exists()) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(CSV_HEADER + "\n");
                }
            } else if (file.length() == 0) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(CSV_HEADER + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Не удалось инициализировать CSV файл: " + e.getMessage());
        }
    }

    @Override
    public void addPlant(Plant plant) {
        List<Plant> plants = getAllPlants();
        plant.setId(idGenerator.getAndIncrement());
        plants.add(plant);
        saveAll(plants);
    }

    @Override
    public void updatePlant(Plant plant) {
        List<Plant> plants = getAllPlants();
        for (int i = 0; i < plants.size(); i++) {
            if (plants.get(i).getId() == plant.getId()) {
                plants.set(i, plant);
                saveAll(plants);
                return;
            }
        }
        throw new RuntimeException("Растение не найдено по id: " + plant.getId());
    }

    @Override
    public void deletePlant(long id) {
        List<Plant> plants = getAllPlants();
        plants.removeIf(p -> p.getId() == id);
        saveAll(plants);
    }

    @Override
    public List<Plant> getAllPlants() {
        List<Plant> plants = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line == null || !line.equals(CSV_HEADER)) {
                return plants;
            }

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Plant plant = parsePlantFromCsv(line);
                    if (plant != null) {
                        plants.add(plant);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения CSV файла: " + e.getMessage());
            return plants;
        }

        if (!plants.isEmpty()) {
            plants.stream().mapToLong(Plant::getId).max()
                    .ifPresent(maxId -> idGenerator.set(maxId + 1));
        }

        return plants;
    }

    @Override
    public Plant getPlantById(long id) {
        return getAllPlants().stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private Plant parsePlantFromCsv(String csvLine) {
        try {
            String[] fields = csvLine.split(",");
            if (fields.length != 9) return null;

            Plant plant = new Plant();
            plant.setId(Long.parseLong(fields[0]));
            plant.setName(fields[1]);
            plant.setPlantingDate(LocalDate.parse(fields[2]));
            plant.setWateringFrequency(Integer.parseInt(fields[3]));
            plant.setLastWateringDate(fields[4].isEmpty() ? null : LocalDate.parse(fields[4]));
            plant.setLastRepottingDate(fields[5].isEmpty() ? null : LocalDate.parse(fields[5]));
            plant.setHealthStatus(fields[6]);
            plant.setGrowthRate(Double.parseDouble(fields[7]));
            plant.setCurrentHeight(Double.parseDouble(fields[8]));

            return plant;
        } catch (Exception e) {
            System.err.println("Ошибка парсинга строки CSV: " + csvLine + " - " + e.getMessage());
            return null;
        }
    }

    private void saveAll(List<Plant> plants) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(CSV_HEADER + "\n");
            for (Plant plant : plants) {
                writer.write(plantToCsv(plant) + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи в CSV файл: " + e.getMessage(), e);
        }
    }

    private String plantToCsv(Plant plant) {
        return String.join(",",
                String.valueOf(plant.getId()),
                plant.getName() != null ? plant.getName() : "",
                plant.getPlantingDate() != null ? plant.getPlantingDate().toString() : "",
                String.valueOf(plant.getWateringFrequency()),
                plant.getLastWateringDate() != null ? plant.getLastWateringDate().toString() : "",
                plant.getLastRepottingDate() != null ? plant.getLastRepottingDate().toString() : "",
                plant.getHealthStatus() != null ? plant.getHealthStatus() : "",
                String.valueOf(plant.getGrowthRate()),
                String.valueOf(plant.getCurrentHeight())
        );
    }
}