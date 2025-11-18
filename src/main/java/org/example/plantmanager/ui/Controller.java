package org.example.plantmanager.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.plantmanager.data.PlantDao;
import org.example.plantmanager.data.PlantDaoFactory;
import org.example.plantmanager.service.PlantService;
import org.example.plantmanager.business.Plant;
import org.example.plantmanager.business.PlantCareManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML private TableView<Plant> plantTable;
    @FXML private TextField nameField;
    @FXML private TextField wateringFrequencyField;
    @FXML private TextField currentHeightField;
    @FXML private TextField growthRateField;
    @FXML private DatePicker plantingDatePicker;
    @FXML private DatePicker lastWateringDatePicker;
    @FXML private DatePicker lastRepottingDatePicker;
    @FXML private ChoiceBox<String> dataSourceChoiceBox;
    @FXML private Button selectDataSourceButton;
    @FXML private TextField filterField;
    @FXML private ChoiceBox<String> sortChoiceBox;
    @FXML private TextArea notificationsArea;
    @FXML private TextArea diagnosisArea;
    @FXML private TextField symptomsField;

    private PlantDao plantDao;
    private PlantDaoFactory daoFactory;
    private PlantService plantService;
    private PlantCareManager careManager;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    public void initialize() {
        try {
            daoFactory = new PlantDaoFactory();
            plantDao = daoFactory.createCsvDao();
            plantService = new PlantService(plantDao);
            careManager = new PlantCareManager(plantService);

            dataSourceChoiceBox.getItems().addAll("CSV", "PostgreSQL");
            dataSourceChoiceBox.setValue("CSV");

            sortChoiceBox.getItems().addAll("По имени", "По высоте", "По дате посадки", "По скорости роста");
            sortChoiceBox.setValue("По имени");

            initializeDatePickers();
            setupTableColumns();
            setupEventHandlers();
            updateTableView();
            updateNotifications();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка инициализации",
                    "Не удалось инициализировать приложение: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDatePickers() {
        plantingDatePicker.setValue(LocalDate.now());
        lastWateringDatePicker.setValue(LocalDate.now());

        plantingDatePicker.setPromptText("Дата посадки");
        lastWateringDatePicker.setPromptText("Последний полив");
        lastRepottingDatePicker.setPromptText("Последняя пересадка");

        setupDatePickerValidation();
    }

    private void setupDatePickerValidation() {
        plantingDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(LocalDate.now()));
                if (item.isAfter(LocalDate.now())) {
                    setTooltip(new Tooltip("Нельзя выбрать будущую дату"));
                }
            }
        });

        lastWateringDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(LocalDate.now()));
                if (item.isAfter(LocalDate.now())) {
                    setTooltip(new Tooltip("Нельзя выбрать будущую дату"));
                }
            }
        });

        lastRepottingDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(LocalDate.now()));
                if (item.isAfter(LocalDate.now())) {
                    setTooltip(new Tooltip("Нельзя выбрать будущую дату"));
                }
            }
        });
    }

    private void setupTableColumns() {
        plantTable.getColumns().clear();

        TableColumn<Plant, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Plant, Double> heightCol = new TableColumn<>("Высота (см)");
        heightCol.setCellValueFactory(new PropertyValueFactory<>("currentHeight"));
        heightCol.setCellFactory(col -> new TableCell<Plant, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f", item));
            }
        });

        TableColumn<Plant, Double> growthCol = new TableColumn<>("Рост (см/мес)");
        growthCol.setCellValueFactory(new PropertyValueFactory<>("growthRate"));
        growthCol.setCellFactory(col -> new TableCell<Plant, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%.1f", item));
            }
        });

        TableColumn<Plant, String> healthCol = new TableColumn<>("Здоровье");
        healthCol.setCellValueFactory(new PropertyValueFactory<>("healthStatus"));

        TableColumn<Plant, String> wateringCol = new TableColumn<>("Полив через");
        wateringCol.setCellValueFactory(cellData -> {
            long days = cellData.getValue().getDaysUntilNextWatering();
            String status;
            if (days <= 0) {
                status = "Сейчас";
            } else if (days <= 2) {
                status = days + " дн.";
            } else {
                status = days + " дн.";
            }
            return new SimpleStringProperty(status);
        });

        TableColumn<Plant, String> repottingCol = new TableColumn<>("Пересадка");
        repottingCol.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getRepottingStatus();
            return new SimpleStringProperty(status);
        });

        TableColumn<Plant, String> repottingForecastCol = new TableColumn<>("Прогноз пересадки");
        repottingForecastCol.setCellValueFactory(cellData -> {
            String forecast = cellData.getValue().getRepottingForecast();
            return new SimpleStringProperty(forecast);
        });

        TableColumn<Plant, String> lastWaterCol = new TableColumn<>("Послед. полив");
        lastWaterCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getLastWateringDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "—");
        });

        TableColumn<Plant, String> lastRepottingCol = new TableColumn<>("Послед. пересадка");
        lastRepottingCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getLastRepottingDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "—");
        });

        TableColumn<Plant, String> plantingDateCol = new TableColumn<>("Дата посадки");
        plantingDateCol.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getPlantingDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "—");
        });

        plantTable.getColumns().addAll(nameCol, heightCol, growthCol, healthCol,
                wateringCol, repottingCol, repottingForecastCol, lastWaterCol, lastRepottingCol, plantingDateCol);
        plantTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupEventHandlers() {
        selectDataSourceButton.setOnAction(event -> {
            try {
                String selectedSource = dataSourceChoiceBox.getValue();
                switch (selectedSource) {
                    case "CSV":
                        plantDao = daoFactory.createCsvDao();
                        showAlert(Alert.AlertType.INFORMATION, "Источник данных",
                                "Выбран режим хранения в CSV файле.");
                        break;

                    case "PostgreSQL":
                        try {
                            plantDao = daoFactory.createPostgresDao();
                            showAlert(Alert.AlertType.INFORMATION, "Источник данных",
                                    "Успешное подключение к PostgreSQL");
                        } catch (RuntimeException e) {
                            showAlert(Alert.AlertType.ERROR, "Ошибка PostgreSQL",
                                    "Не удалось подключиться к БД: " + e.getMessage() +
                                            "\nАвтоматически переключено на CSV.");
                            plantDao = daoFactory.createCsvDao();
                            dataSourceChoiceBox.setValue("CSV");
                        }
                        break;
                }

                plantService = new PlantService(plantDao);
                careManager = new PlantCareManager(plantService);
                updateTableView();
                updateNotifications();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Критическая ошибка",
                        "Не удалось инициализировать источник данных: " + e.getMessage());
            }
        });

        plantTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillFormWithSelectedPlant(newVal);
            }
        });

        sortChoiceBox.setOnAction(e -> updateTableView());

        filterField.textProperty().addListener((obs, oldText, newText) -> updateTableView());

        wateringFrequencyField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                wateringFrequencyField.setText(oldVal);
            }
        });

        currentHeightField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                currentHeightField.setText(oldVal);
            }
        });

        growthRateField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                growthRateField.setText(oldVal);
            }
        });
    }

    @FXML
    private void handleAddPlant() {
        try {
            if (!validateFields()) return;

            String name = nameField.getText();
            int wateringFrequency = Integer.parseInt(wateringFrequencyField.getText());
            double currentHeight = Double.parseDouble(currentHeightField.getText());
            double growthRate = Double.parseDouble(growthRateField.getText());
            LocalDate plantingDate = plantingDatePicker.getValue();
            LocalDate lastWateringDate = lastWateringDatePicker.getValue();
            LocalDate lastRepottingDate = lastRepottingDatePicker.getValue();

            Plant plant = new Plant(name, plantingDate, wateringFrequency, growthRate, currentHeight);

            if (lastWateringDate != null) {
                plant.setLastWateringDate(lastWateringDate);
            }
            if (lastRepottingDate != null) {
                plant.setLastRepottingDate(lastRepottingDate);
            }

            plantService.addPlant(plant);
            updateTableView();
            updateNotifications();
            clearFields();

            showAlert(Alert.AlertType.INFORMATION, "Успех",
                    "Растение '" + name + "' успешно добавлено!");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить растение: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdatePlant() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Нет выбора", "Выберите растение для редактирования");
            return;
        }

        try {
            if (!validateFields()) return;

            selected.setName(nameField.getText());
            selected.setWateringFrequency(Integer.parseInt(wateringFrequencyField.getText()));
            selected.setCurrentHeight(Double.parseDouble(currentHeightField.getText()));
            selected.setGrowthRate(Double.parseDouble(growthRateField.getText()));

            if (plantingDatePicker.getValue() != null) {
                selected.setPlantingDate(plantingDatePicker.getValue());
            }
            if (lastWateringDatePicker.getValue() != null) {
                selected.setLastWateringDate(lastWateringDatePicker.getValue());
            }
            if (lastRepottingDatePicker.getValue() != null) {
                selected.setLastRepottingDate(lastRepottingDatePicker.getValue());
            }

            plantService.updatePlant(selected);
            updateTableView();
            updateNotifications();
            clearFields();

            showAlert(Alert.AlertType.INFORMATION, "Успех",
                    "Растение '" + selected.getName() + "' успешно обновлено!");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить растение: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeletePlant() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Нет выбора", "Выберите растение для удаления");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Подтверждение удаления");
        confirmation.setHeaderText("Удаление растения");
        confirmation.setContentText("Вы уверены, что хотите удалить растение '" + selected.getName() + "'?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                plantService.deletePlant(selected.getId());
                updateTableView();
                updateNotifications();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Успех",
                        "Растение '" + selected.getName() + "' успешно удалено!");
            }
        });
    }

    @FXML
    private void handleWaterPlant() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Нет выбора", "Выберите растение для полива");
            return;
        }

        plantService.waterPlant(selected.getId());
        updateTableView();
        updateNotifications();

        lastWateringDatePicker.setValue(LocalDate.now());

        showAlert(Alert.AlertType.INFORMATION, "Полив",
                "Растение '" + selected.getName() + "' полито!\nСледующий полив через " +
                        selected.getWateringFrequency() + " дней.");
    }

    @FXML
    private void handleRepotPlant() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Нет выбора", "Выберите растение для пересадки");
            return;
        }

        plantService.repotPlant(selected.getId());
        updateTableView();
        updateNotifications();

        lastRepottingDatePicker.setValue(LocalDate.now());

        showAlert(Alert.AlertType.INFORMATION, "Пересадка",
                "Растение '" + selected.getName() + "' пересажено!\n" + selected.getRepottingForecast());
    }

    @FXML
    private void handleDiagnosePlant() {
        Plant selected = plantTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Нет выбора", "Выберите растение для диагностики");
            return;
        }

        String symptoms = symptomsField.getText();
        if (!symptoms.isEmpty()) {
            plantService.updatePlantHealth(selected.getId(), symptoms);
            updateTableView();
        }

        String diagnosis = plantService.diagnosePlant(selected.getId());
        diagnosisArea.setText(diagnosis);

        if (!diagnosis.equals("Растение здорово")) {
            showAlert(Alert.AlertType.WARNING, "Диагностика",
                    "Обнаружены проблемы с растением '" + selected.getName() + "'.\nПроверьте результат диагностики.");
        }
    }

    @FXML
    private void handleShowStatistics() {
        String statistics = careManager.getPlantStatistics();
        showAlert(Alert.AlertType.INFORMATION, "Статистика растений", statistics);
    }

    private void updateTableView() {
        try {
            String filter = filterField.getText().toLowerCase();
            String sort = sortChoiceBox.getValue();
            List<Plant> all = plantService.getAllPlants();

            List<Plant> filtered = new ArrayList<>();
            for (Plant p : all) {
                if (filter.isEmpty() ||
                        p.getName().toLowerCase().contains(filter) ||
                        p.getHealthStatus().toLowerCase().contains(filter)) {
                    filtered.add(p);
                }
            }

            switch (sort) {
                case "По имени":
                    filtered.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
                    break;
                case "По высоте":
                    filtered.sort((p1, p2) -> Double.compare(p1.getCurrentHeight(), p2.getCurrentHeight()));
                    break;
                case "По дате посадки":
                    filtered.sort((p1, p2) -> p1.getPlantingDate().compareTo(p2.getPlantingDate()));
                    break;
                case "По скорости роста":
                    filtered.sort((p1, p2) -> Double.compare(p2.getGrowthRate(), p1.getGrowthRate()));
                    break;
            }

            plantTable.setItems(FXCollections.observableArrayList(filtered));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка обновления таблицы", e.getMessage());
        }
    }

    private void updateNotifications() {
        String notifications = careManager.getCareNotifications();
        notificationsArea.setText(notifications);

        if (notifications.contains("СРОЧНО") || notifications.contains("СЕЙЧАС")) {
            notificationsArea.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2; -fx-background-color: #ffebee;");
        } else {
            notificationsArea.setStyle("-fx-border-color: #FFA000; -fx-border-width: 2; -fx-background-color: #FFF8E1;");
        }
    }

    private void fillFormWithSelectedPlant(Plant p) {
        nameField.setText(p.getName());
        wateringFrequencyField.setText(String.valueOf(p.getWateringFrequency()));
        currentHeightField.setText(String.valueOf(p.getCurrentHeight()));
        growthRateField.setText(String.valueOf(p.getGrowthRate()));
        symptomsField.setText(p.getHealthStatus());

        plantingDatePicker.setValue(p.getPlantingDate());
        lastWateringDatePicker.setValue(p.getLastWateringDate());
        lastRepottingDatePicker.setValue(p.getLastRepottingDate());
    }

    private void clearFields() {
        nameField.clear();
        wateringFrequencyField.clear();
        currentHeightField.clear();
        growthRateField.clear();
        symptomsField.clear();
        diagnosisArea.clear();

        plantingDatePicker.setValue(LocalDate.now());
        lastWateringDatePicker.setValue(LocalDate.now());
        lastRepottingDatePicker.setValue(null);

        plantTable.getSelectionModel().clearSelection();
    }

    private boolean validateFields() {
        if (nameField.getText().isEmpty() || wateringFrequencyField.getText().isEmpty() ||
                currentHeightField.getText().isEmpty() || growthRateField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Проверка", "Заполните все обязательные поля");
            return false;
        }

        if (plantingDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Проверка", "Укажите дату посадки");
            return false;
        }

        try {
            int wateringFreq = Integer.parseInt(wateringFrequencyField.getText());
            if (wateringFreq <= 0) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Частота полива должна быть положительным числом");
                return false;
            }

            double currentHeight = Double.parseDouble(currentHeightField.getText());
            if (currentHeight <= 0) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Высота растения должна быть положительным числом");
                return false;
            }

            double growthRate = Double.parseDouble(growthRateField.getText());
            if (growthRate < 0) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Скорость роста не может быть отрицательной");
                return false;
            }

            if (plantingDatePicker.getValue().isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Дата посадки не может быть в будущем");
                return false;
            }
            if (lastWateringDatePicker.getValue() != null &&
                    lastWateringDatePicker.getValue().isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Дата последнего полива не может быть в будущем");
                return false;
            }
            if (lastRepottingDatePicker.getValue() != null &&
                    lastRepottingDatePicker.getValue().isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Дата последней пересадки не может быть в будущем");
                return false;
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректные числовые значения");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}