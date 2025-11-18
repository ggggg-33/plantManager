package org.example.plantmanager.business;

import org.example.plantmanager.service.PlantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlantCareManagerTest {

    private PlantService plantService;
    private PlantCareManager careManager;

    @BeforeEach
    void setUp() {
        plantService = mock(PlantService.class);
        careManager = new PlantCareManager(plantService);
    }

    @Test
    void getCareNotificationsWithPlantsNeedingWater() {
        Plant plant1 = new Plant("Спатифиллум", LocalDate.now().minusDays(8), 5, 2.0, 40.0);
        Plant plant2 = new Plant("Замиокулькас", LocalDate.now().minusDays(20), 14, 1.0, 60.0);

        when(plantService.getPlantsNeedingWater()).thenReturn(List.of(plant1, plant2));
        when(plantService.getPlantsNeedingRepotting()).thenReturn(List.of());

        String notifications = careManager.getCareNotifications();

        assertTrue(notifications.contains("Спатифиллум"));
        assertTrue(notifications.contains("Замиокулькас"));
        assertTrue(notifications.contains("полива"));
    }

    @Test
    void getCareNotificationsWithPlantsNeedingRepotting() {
        Plant plant = new Plant("Монстера", LocalDate.now().minusMonths(15), 7, 3.0, 120.0);
        plant.setLastRepottingDate(LocalDate.now().minusMonths(15));

        when(plantService.getPlantsNeedingWater()).thenReturn(List.of());
        when(plantService.getPlantsNeedingRepotting()).thenReturn(List.of(plant));

        String notifications = careManager.getCareNotifications();

        assertTrue(notifications.contains("Монстера"));
        assertTrue(notifications.contains("пересадки"));
    }


    @Test
    void getCareNotificationsWhenAllPlantsAreHealthy() {
        when(plantService.getPlantsNeedingWater()).thenReturn(List.of());
        when(plantService.getPlantsNeedingRepotting()).thenReturn(List.of());

        String notifications = careManager.getCareNotifications();

        assertEquals("Все растения в порядке!", notifications);
    }
}