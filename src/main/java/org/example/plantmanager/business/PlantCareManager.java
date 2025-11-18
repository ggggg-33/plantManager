package org.example.plantmanager.business;

import org.example.plantmanager.service.PlantService;

import java.util.List;

public class PlantCareManager {
    private final PlantService plantService;

    public PlantCareManager(PlantService plantService) {
        this.plantService = plantService;
    }

    /**
     * Получить уведомления по уходу за растениями
     */
    public String getCareNotifications() {
        StringBuilder notifications = new StringBuilder();

        List<Plant> plantsNeedingWater = plantService.getPlantsNeedingWater();
        if (!plantsNeedingWater.isEmpty()) {
            notifications.append("Растения, требующие полива:\n");
            for (Plant plant : plantsNeedingWater) {
                long daysOverdue = -plant.getDaysUntilNextWatering();
                String urgency = daysOverdue > 3 ? "СРОЧНО" : "Внимание";
                notifications.append("- ").append(plant.getName())
                        .append(" (").append(urgency).append(" - просрочка ").append(daysOverdue).append(" дн.)\n");
            }
            notifications.append("\n");
        }

        List<Plant> plantsNeedingRepotting = plantService.getPlantsNeedingRepotting();
        if (!plantsNeedingRepotting.isEmpty()) {
            notifications.append("Растения, требующие пересадки:\n");
            for (Plant plant : plantsNeedingRepotting) {
                String status = plant.getRepottingStatus();
                notifications.append("- ").append(plant.getName())
                        .append(" (").append(status).append(")\n");
            }
            notifications.append("\n");
        }

        // Добавим уведомления о растениях, которые скоро потребуют внимания
        List<Plant> allPlants = plantService.getAllPlants();
        int upcomingWaterCount = 0;
        int upcomingRepottingCount = 0;

        for (Plant plant : allPlants) {
            // Растения, которые нужно полить в ближайшие 2 дня
            if (plant.getDaysUntilNextWatering() > 0 && plant.getDaysUntilNextWatering() <= 2) {
                upcomingWaterCount++;
            }

            // Растения, у которых статус пересадки "Скоро"
            if (plant.getRepottingStatus().contains("Скоро")) {
                upcomingRepottingCount++;
            }
        }

        if (upcomingWaterCount > 0) {
            notifications.append("Скоро потребуют полива: ").append(upcomingWaterCount).append(" растений\n");
        }

        if (upcomingRepottingCount > 0) {
            notifications.append("Скоро потребуют пересадки: ").append(upcomingRepottingCount).append(" растений\n");
        }

        return notifications.length() > 0 ? notifications.toString() : "Все растения в порядке!";
    }

    /**
     * Получить статистику по растениям
     */
    public String getPlantStatistics() {
        List<Plant> allPlants = plantService.getAllPlants();

        if (allPlants.isEmpty()) {
            return "Нет растений в каталоге";
        }

        long totalPlants = allPlants.size();
        long needWater = plantService.getPlantsNeedingWater().size();
        long needRepotting = plantService.getPlantsNeedingRepotting().size();
        long healthyPlants = allPlants.stream()
                .filter(p -> p.getHealthStatus().equals("Здоров"))
                .count();

        // Статистика по скорости роста
        double avgGrowthRate = allPlants.stream()
                .mapToDouble(Plant::getGrowthRate)
                .average()
                .orElse(0.0);

        double maxGrowthRate = allPlants.stream()
                .mapToDouble(Plant::getGrowthRate)
                .max()
                .orElse(0.0);

        double minGrowthRate = allPlants.stream()
                .mapToDouble(Plant::getGrowthRate)
                .min()
                .orElse(0.0);

        // Статистика по высоте
        double avgHeight = allPlants.stream()
                .mapToDouble(Plant::getCurrentHeight)
                .average()
                .orElse(0.0);

        double maxHeight = allPlants.stream()
                .mapToDouble(Plant::getCurrentHeight)
                .max()
                .orElse(0.0);

        // Распределение по скорости роста
        long fastGrowing = allPlants.stream()
                .filter(p -> p.getGrowthRate() > 5.0)
                .count();

        long mediumGrowing = allPlants.stream()
                .filter(p -> p.getGrowthRate() > 2.0 && p.getGrowthRate() <= 5.0)
                .count();

        long slowGrowing = allPlants.stream()
                .filter(p -> p.getGrowthRate() <= 2.0)
                .count();

        return String.format(
                "Статистика растений:\n\n" +
                        "Общее количество: %d растений\n\n" +
                        "Требуют внимания:\n" +
                        "   • Полив: %d растений\n" +
                        "   • Пересадка: %d растений\n\n" +
                        "Состояние здоровья:\n" +
                        "   • Здоровых: %d растений\n" +
                        "   • С проблемами: %d растений\n\n" +
                        "Скорость роста (см/мес):\n" +
                        "   • Средняя: %.1f\n" +
                        "   • Максимальная: %.1f\n" +
                        "   • Минимальная: %.1f\n\n" +
                        "Распределение по росту:\n" +
                        "   • Быстрорастущие: %d растений\n" +
                        "   • Средний рост: %d растений\n" +
                        "   • Медленнорастущие: %d растений\n\n" +
                        "Высота растений:\n" +
                        "   • Средняя: %.1f см\n" +
                        "   • Максимальная: %.1f см",
                totalPlants, needWater, needRepotting, healthyPlants,
                totalPlants - healthyPlants, avgGrowthRate, maxGrowthRate, minGrowthRate,
                fastGrowing, mediumGrowing, slowGrowing, avgHeight, maxHeight
        );
    }

    /**
     * Получить расширенную статистику по конкретному растению
     */
    public String getPlantDetailedStatistics(long plantId) {
        Plant plant = plantService.getPlantById(plantId);
        if (plant == null) {
            return "Растение не найдено";
        }

        return String.format(
                "Детальная статистика: %s\n\n" +
                        "Полив:\n" +
                        "   • Частота: каждые %d дней\n" +
                        "   • Следующий полив: через %d дней\n" +
                        "   • Последний полив: %s\n\n" +
                        "Пересадка:\n" +
                        "   • Статус: %s\n" +
                        "   • Прогноз: %s\n" +
                        "   • Последняя пересадка: %s\n\n" +
                        "Параметры роста:\n" +
                        "   • Текущая высота: %.1f см\n" +
                        "   • Скорость роста: %.1f см/мес\n" +
                        "   • Дата посадки: %s\n\n" +
                        "Состояние: %s",
                plant.getName(),
                plant.getWateringFrequency(),
                plant.getDaysUntilNextWatering(),
                plant.getLastWateringDate() != null ? plant.getLastWateringDate().toString() : "не указано",
                plant.getRepottingStatus(),
                plant.getRepottingForecast(),
                plant.getLastRepottingDate() != null ? plant.getLastRepottingDate().toString() : "не указано",
                plant.getCurrentHeight(),
                plant.getGrowthRate(),
                plant.getPlantingDate().toString(),
                plant.getHealthStatus()
        );
    }
}