package org.example.plantmanager.business;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;

public class Plant {
    private long id;
    private String name;
    private LocalDate plantingDate;
    private int wateringFrequency;
    private LocalDate lastWateringDate;
    private LocalDate lastRepottingDate;
    private String healthStatus;
    private double growthRate; // см в месяц
    private double currentHeight;

    public Plant() {
        // для сериализации
    }

    public Plant(String name, LocalDate plantingDate, int wateringFrequency,
                 double growthRate, double currentHeight) {
        this.name = name;
        this.plantingDate = plantingDate;
        this.wateringFrequency = wateringFrequency;
        this.growthRate = growthRate;
        this.currentHeight = currentHeight;
        this.lastWateringDate = plantingDate;
        this.lastRepottingDate = plantingDate;
        this.healthStatus = "Здоров";
        adjustWateringForSeason();
    }

    /**
     * Расчет максимальной высоты растения по его названию
     */
    private double calculateMaxHeight(String plantName) {
        String nameLower = plantName.toLowerCase();

        if (nameLower.contains("фикус") || nameLower.contains("монстера") ||
                nameLower.contains("пальма") || nameLower.contains("драцена")) {
            return 200.0; // Крупные растения
        } else if (nameLower.contains("фиалка") || nameLower.contains("кактус") ||
                nameLower.contains("суккулент") || nameLower.contains("алоэ")) {
            return 30.0; // Маленькие растения
        } else if (nameLower.contains("орхидея") || nameLower.contains("антуриум") ||
                nameLower.contains("спатифиллум")) {
            return 60.0; // Средние растения
        } else if (nameLower.contains("плющ") || nameLower.contains("традесканция") ||
                nameLower.contains("хлорофитум")) {
            return 100.0; // Вьющиеся растения
        } else {
            return 50.0; // Среднее значение по умолчанию
        }
    }

    /**
     * Прогноз полного роста
     */
    public String getFullGrowthForecast() {
        if (growthRate <= 0) {
            return "Растение не растет";
        }

        double maxHeight = calculateMaxHeight(name);

        if (currentHeight >= maxHeight) {
            return "Достигло максимума";
        }

        double remainingHeight = maxHeight - currentHeight;
        long monthsToFullGrowth = (long) Math.ceil(remainingHeight / growthRate);

        if (monthsToFullGrowth <= 0) {
            return "Скоро достигнет максимума";
        } else if (monthsToFullGrowth == 1) {
            return "Через 1 месяц";
        } else if (monthsToFullGrowth < 12) {
            return "Через " + monthsToFullGrowth + " мес.";
        } else {
            long years = monthsToFullGrowth / 12;
            long months = monthsToFullGrowth % 12;
            if (months == 0) {
                return "Через " + years + " " + getYearWord(years);
            } else {
                return "Через " + years + " " + getYearWord(years) + " " + months + " мес.";
            }
        }
    }

    /**
     * Автокоррекция частоты полива по сезону
     */
    public void adjustWateringForSeason() {
        Month currentMonth = LocalDate.now().getMonth();

        if (currentMonth == Month.DECEMBER || currentMonth == Month.JANUARY ||
                currentMonth == Month.FEBRUARY) {
            // Зима - реже полив
            this.wateringFrequency += 2;
        } else if (currentMonth == Month.JUNE || currentMonth == Month.JULY ||
                currentMonth == Month.AUGUST) {
            // Лето - чаще полив
            this.wateringFrequency = Math.max(1, this.wateringFrequency - 1);
        }
    }

    /**
     * Диагностика болезней на основе симптомов
     */
    public String diagnoseHealth() {
        StringBuilder diagnosis = new StringBuilder();

        if (this.healthStatus.contains("желтые листья")) {
            diagnosis.append("Возможные причины желтых листьев:\n");
            diagnosis.append("- Избыточный полив\n");
            diagnosis.append("- Недостаток питательных веществ\n");
            diagnosis.append("- Недостаток света\n");
        }

        if (this.healthStatus.contains("вялые листья")) {
            if (diagnosis.length() > 0) diagnosis.append("\n");
            diagnosis.append("Возможные причины вялых листьев:\n");
            diagnosis.append("- Недостаточный полив\n");
            diagnosis.append("- Высокая температура\n");
            diagnosis.append("- Болезни корней\n");
        }

        if (this.healthStatus.contains("пятна")) {
            if (diagnosis.length() > 0) diagnosis.append("\n");
            diagnosis.append("Возможные причины пятен на листьях:\n");
            diagnosis.append("- Грибковые заболевания\n");
            diagnosis.append("- Солнечные ожоги\n");
            diagnosis.append("- Вредители\n");
        }

        return diagnosis.length() > 0 ? diagnosis.toString() : "Растение здорово";
    }

    /**
     * Нужна ли пересадка на основе скорости роста и времени с последней пересадки
     */
    public boolean needsRepotting() {
        if (lastRepottingDate == null) return false;

        long monthsSinceRepotting = ChronoUnit.MONTHS.between(lastRepottingDate, LocalDate.now());

        // Быстрорастущие растения требуют пересадки чаще
        int recommendedMonths;
        if (growthRate > 5.0) {
            recommendedMonths = 6; // Очень быстрорастущие - каждые 6 месяцев
        } else if (growthRate > 2.0) {
            recommendedMonths = 9; // Средний рост - каждые 9 месяцев
        } else {
            recommendedMonths = 12; // Медленнорастущие - каждые 12 месяцев
        }

        return monthsSinceRepotting > recommendedMonths;
    }

    /**
     * Статус пересадки с учетом срочности
     */
    public String getRepottingStatus() {
        if (lastRepottingDate == null) return "Нет данных";

        long monthsSinceRepotting = ChronoUnit.MONTHS.between(lastRepottingDate, LocalDate.now());
        int recommendedMonths = getRecommendedRepottingMonths();

        if (monthsSinceRepotting > recommendedMonths + 3) {
            return "СРОЧНО";
        } else if (monthsSinceRepotting > recommendedMonths) {
            return "Скоро";
        } else if (monthsSinceRepotting > recommendedMonths - 2) {
            return "Норма";
        } else {
            return "Норма";
        }
    }

    /**
     * Прогноз когда нужна следующая пересадка
     */
    public String getRepottingForecast() {
        if (lastRepottingDate == null) return "Нет данных";

        int recommendedMonths = getRecommendedRepottingMonths();
        LocalDate nextRepottingDate = lastRepottingDate.plusMonths(recommendedMonths);
        long monthsUntilRepotting = ChronoUnit.MONTHS.between(LocalDate.now(), nextRepottingDate);

        if (monthsUntilRepotting <= 0) {
            return "Требуется сейчас";
        } else if (monthsUntilRepotting == 1) {
            return "Через 1 месяц";
        } else if (monthsUntilRepotting < 12) {
            return "Через " + monthsUntilRepotting + " мес.";
        } else {
            long years = monthsUntilRepotting / 12;
            long months = monthsUntilRepotting % 12;
            if (months == 0) {
                return "Через " + years + " " + getYearWord(years);
            } else {
                return "Через " + years + " " + getYearWord(years) + " " + months + " мес.";
            }
        }
    }

    private String getYearWord(long years) {
        if (years == 1) return "год";
        if (years >= 2 && years <= 4) return "года";
        return "лет";
    }

    private int getRecommendedRepottingMonths() {
        if (growthRate > 5.0) return 6;
        if (growthRate > 2.0) return 9;
        return 12;
    }

    public long getDaysUntilNextWatering() {
        if (lastWateringDate == null) return 0;
        LocalDate nextWateringDate = lastWateringDate.plusDays(wateringFrequency);
        return ChronoUnit.DAYS.between(LocalDate.now(), nextWateringDate);
    }

    // Геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getPlantingDate() { return plantingDate; }
    public void setPlantingDate(LocalDate plantingDate) { this.plantingDate = plantingDate; }

    public int getWateringFrequency() { return wateringFrequency; }
    public void setWateringFrequency(int wateringFrequency) {
        this.wateringFrequency = wateringFrequency;
    }

    public LocalDate getLastWateringDate() { return lastWateringDate; }
    public void setLastWateringDate(LocalDate lastWateringDate) {
        this.lastWateringDate = lastWateringDate;
    }

    public LocalDate getLastRepottingDate() { return lastRepottingDate; }
    public void setLastRepottingDate(LocalDate lastRepottingDate) {
        this.lastRepottingDate = lastRepottingDate;
    }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public double getGrowthRate() { return growthRate; }
    public void setGrowthRate(double growthRate) { this.growthRate = growthRate; }

    public double getCurrentHeight() { return currentHeight; }
    public void setCurrentHeight(double currentHeight) { this.currentHeight = currentHeight; }

    @Override
    public String toString() {
        return "Plant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", wateringFrequency=" + wateringFrequency +
                ", growthRate=" + growthRate +
                ", currentHeight=" + currentHeight +
                '}';
    }
}