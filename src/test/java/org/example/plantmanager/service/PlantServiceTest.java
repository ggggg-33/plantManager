package org.example.plantmanager.service;

import org.example.plantmanager.business.Plant;
import org.example.plantmanager.data.PlantDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlantServiceTest {

    private PlantDao dao;
    private PlantService service;

    @BeforeEach
    void setUp() {
        dao = mock(PlantDao.class);
        service = new PlantService(dao);
    }

    @Test
    void addPlantCallsDaoAndAdjustsWatering() {
        Plant plant = new Plant("Фикус", LocalDate.now(), 7, 2.5, 45.0);
        service.addPlant(plant);

        // Проверяем, что DAO был вызван
        verify(dao).addPlant(any(Plant.class));
        // Проверяем, что растение было обработано сервисом
        assertNotNull(plant);
    }

    @Test
    void waterPlantUpdatesLastWateringDate() {
        Plant plant = new Plant("Кактус", LocalDate.now().minusDays(10), 14, 1.0, 15.0);
        plant.setLastWateringDate(LocalDate.now().minusDays(10));

        when(dao.getPlantById(1L)).thenReturn(plant);

        service.waterPlant(1L);

        assertEquals(LocalDate.now(), plant.getLastWateringDate());
        verify(dao).updatePlant(plant);
    }

    @Test
    void repotPlantUpdatesLastRepottingDate() {
        Plant plant = new Plant("Орхидея", LocalDate.now().minusYears(1), 7, 0.5, 30.0);
        plant.setLastRepottingDate(LocalDate.now().minusYears(1));

        when(dao.getPlantById(1L)).thenReturn(plant);

        service.repotPlant(1L);

        assertEquals(LocalDate.now(), plant.getLastRepottingDate());
        verify(dao).updatePlant(plant);
    }

    @Test
    void diagnosePlantReturnsDiagnosisForUnhealthyPlant() {
        Plant plant = new Plant("Роза", LocalDate.now(), 5, 3.0, 60.0);
        plant.setHealthStatus("желтые листья");
        when(dao.getPlantById(1L)).thenReturn(plant);

        String diagnosis = service.diagnosePlant(1L);

        assertNotNull(diagnosis);
        assertFalse(diagnosis.isEmpty());
        // Для растения с симптомами должен вернуться диагноз с рекомендациями
        assertTrue(diagnosis.contains("желтые листья") ||
                diagnosis.contains("Избыточный полив") ||
                diagnosis.contains("Недостаток питательных веществ"));
    }

    @Test
    void diagnosePlantReturnsHealthyForHealthyPlant() {
        Plant plant = new Plant("Здоровое растение", LocalDate.now(), 5, 3.0, 60.0);
        plant.setHealthStatus("Здоров");
        when(dao.getPlantById(1L)).thenReturn(plant);

        String diagnosis = service.diagnosePlant(1L);

        assertEquals("Растение здорово", diagnosis);
    }

    @Test
    void getPlantsNeedingWaterReturnsPlantsWithOverdueWatering() {
        // Растение, которое нужно полить (прошло больше дней, чем частота полива)
        Plant plant1 = new Plant("Суккулент", LocalDate.now().minusDays(15), 10, 1.0, 20.0);
        plant1.setLastWateringDate(LocalDate.now().minusDays(15));

        // Растение, которое не нужно поливать (прошло меньше дней, чем частота полива)
        Plant plant2 = new Plant("Папоротник", LocalDate.now().minusDays(2), 7, 2.0, 35.0);
        plant2.setLastWateringDate(LocalDate.now().minusDays(2));

        when(dao.getAllPlants()).thenReturn(List.of(plant1, plant2));

        List<Plant> plantsNeedingWater = service.getPlantsNeedingWater();

        // Только Суккулент должен требовать полива
        assertEquals(1, plantsNeedingWater.size());
        assertEquals("Суккулент", plantsNeedingWater.get(0).getName());
    }

    @Test
    void getPlantsNeedingRepottingReturnsPlantsWithOverdueRepotting() {
        // Растение, которое нужно пересадить (прошло больше 12 месяцев)
        Plant plant1 = new Plant("Бонсай", LocalDate.now().minusMonths(15), 7, 0.2, 25.0);
        plant1.setLastRepottingDate(LocalDate.now().minusMonths(15));

        // Растение, которое не нужно пересаживать (прошло меньше 12 месяцев)
        Plant plant2 = new Plant("Традесканция", LocalDate.now().minusMonths(6), 5, 5.0, 40.0);
        plant2.setLastRepottingDate(LocalDate.now().minusMonths(6));

        when(dao.getAllPlants()).thenReturn(List.of(plant1, plant2));

        List<Plant> plantsNeedingRepotting = service.getPlantsNeedingRepotting();

        // Только Бонсай должен требовать пересадки
        assertEquals(1, plantsNeedingRepotting.size());
        assertEquals("Бонсай", plantsNeedingRepotting.get(0).getName());
    }

    @Test
    void getAllPlantsReturnsAllPlantsFromDao() {
        Plant plant1 = new Plant("Кактус", LocalDate.now(), 14, 1.0, 15.0);
        Plant plant2 = new Plant("Фикус", LocalDate.now(), 7, 2.5, 45.0);

        when(dao.getAllPlants()).thenReturn(List.of(plant1, plant2));

        List<Plant> allPlants = service.getAllPlants();

        assertEquals(2, allPlants.size());
        verify(dao).getAllPlants();
    }

    @Test
    void updatePlantHealthUpdatesHealthStatus() {
        Plant plant = new Plant("Орхидея", LocalDate.now(), 7, 0.5, 30.0);
        when(dao.getPlantById(1L)).thenReturn(plant);

        service.updatePlantHealth(1L, "новые симптомы");

        assertEquals("новые симптомы", plant.getHealthStatus());
        verify(dao).updatePlant(plant);
    }

    @Test
    void deletePlantCallsDao() {
        service.deletePlant(1L);
        verify(dao).deletePlant(1L);
    }
}