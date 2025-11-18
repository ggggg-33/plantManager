package org.example.plantmanager.service;

import org.example.plantmanager.data.PlantDao;
import org.example.plantmanager.business.Plant;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PlantService {
    private final PlantDao plantDao;

    public PlantService(PlantDao plantDao) {
        this.plantDao = plantDao;
    }

    public List<Plant> getAllPlants() {
        return plantDao.getAllPlants();
    }

    public void addPlant(Plant plant) {
        plant.adjustWateringForSeason();
        plantDao.addPlant(plant);
    }

    public void updatePlant(Plant plant) {
        plant.adjustWateringForSeason();
        plantDao.updatePlant(plant);
    }

    public void deletePlant(long id) {
        plantDao.deletePlant(id);
    }

    public Plant getPlantById(long id) {
        return plantDao.getPlantById(id);
    }

    /**
     * Получить растения, которые нужно полить сегодня или скоро
     */
    public List<Plant> getPlantsNeedingWater() {
        return plantDao.getAllPlants().stream()
                .filter(plant -> plant.getDaysUntilNextWatering() <= 0)
                .collect(Collectors.toList());
    }

    /**
     * Получить растения, которые нуждаются в пересадке
     */
    public List<Plant> getPlantsNeedingRepotting() {
        return plantDao.getAllPlants().stream()
                .filter(Plant::needsRepotting)
                .collect(Collectors.toList());
    }

    /**
     * Полить растение
     */
    public void waterPlant(long plantId) {
        Plant plant = plantDao.getPlantById(plantId);
        if (plant != null) {
            plant.setLastWateringDate(LocalDate.now());
            plantDao.updatePlant(plant);
        }
    }

    /**
     * Пересадить растение
     */
    public void repotPlant(long plantId) {
        Plant plant = plantDao.getPlantById(plantId);
        if (plant != null) {
            plant.setLastRepottingDate(LocalDate.now());
            plantDao.updatePlant(plant);
        }
    }

    /**
     * Обновить здоровье растения
     */
    public void updatePlantHealth(long plantId, String symptoms) {
        Plant plant = plantDao.getPlantById(plantId);
        if (plant != null) {
            plant.setHealthStatus(symptoms);
            plantDao.updatePlant(plant);
        }
    }

    /**
     * Получить диагностику для растения
     */
    public String diagnosePlant(long plantId) {
        Plant plant = plantDao.getPlantById(plantId);
        return plant != null ? plant.diagnoseHealth() : "Растение не найдено";
    }
}
