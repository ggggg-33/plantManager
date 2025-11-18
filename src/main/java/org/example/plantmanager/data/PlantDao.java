package org.example.plantmanager.data;

import org.example.plantmanager.business.Plant;

import java.util.List;

public interface PlantDao {
    void addPlant(Plant plant);
    void updatePlant(Plant plant);
    void deletePlant(long id);
    List<Plant> getAllPlants();
    Plant getPlantById(long id);
}
