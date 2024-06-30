package com.example.demo_proba;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class HelloController {
    @FXML
    private ChoiceBox<String> typeColumn;
    @FXML
    private DatePicker dataColumn;
    @FXML
    private TextField timeColumn;
    @FXML
    private TextField durationColumn;
    @FXML
    private TextField descriptionColumn;
    @FXML
    private Button addB;
    @FXML
    private Button saveB;
    @FXML
    private Button editB;
    @FXML
    private Button deleteB;
    @FXML
    private TableView<Training> trainingTable;
    @FXML
    private TableColumn<Training, String> typeCol;
    @FXML
    private TableColumn<Training, LocalDate> dateCol;
    @FXML
    private TableColumn<Training, LocalTime> timeCol;
    @FXML
    private TableColumn<Training, String> durationCol;
    @FXML
    private TableColumn<Training, String> descriptionCol;

    @FXML// Инициализация колонок для TableView
    public void initialize() {
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("durationAsString"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        // Заполнение ChoiceBox значениями
        ObservableList<String> typesOfTraining = FXCollections.observableArrayList(
                "Кардио", "Силовая", "Растяжка", "Йога", "Кроссфит"
        );
        typeColumn.setItems(typesOfTraining);
        // Загрузка данных из файла
        loadTrainingsFromFile();
        // Добавление обработчика событий для кнопки добавления
        addB.setOnAction(this::addTraining);
        // Добавление обработчика событий для кнопки сохранения
        saveB.setOnAction(this::saveTrainingToFile);
        // Добавление обработчика событий для кнопки редактирования
        editB.setOnAction(event -> editTraining());
        // Добавление обработчика событий для кнопки удаления
        deleteB.setOnAction(this::deleteTraining);
        // Добавление обработчика события закрытия окна
        Platform.runLater(() -> {
            trainingTable.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::onCloseWindow);
        });
    }

    @FXML// Метод для загрузки данных из файла
    private void loadTrainingsFromFile() {
        String homeDirectory = System.getProperty("user.home");
        String filePath = homeDirectory + "/trainings.txt"; // Путь к файлу с сохраненными тренировками
        try (FileReader fr = new FileReader(filePath);
             BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] trainingData = line.split(",");
                // Проверяем, что строка содержит все необходимые данные
                if (trainingData.length < 5) {
                    System.out.println("Неполные данные в строке: " + line);
                    continue; // Пропускаем эту строку и переходим к следующей
                }
                // Создание объекта Training из прочитанных данных
                Training training = new Training(
                        trainingData[0], // тип
                        LocalDate.parse(trainingData[1]), // дата
                        LocalTime.parse(trainingData[2]), // время
                        trainingData[3], // продолжительность
                        trainingData[4]  // описание
                );
                trainingTable.getItems().add(training); // Добавление объекта в таблицу
            }
        } catch (IOException | DateTimeParseException e) {
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    @FXML //Метод для добавления данных
    private void addTraining(ActionEvent event) {
        try {
            // Получение данных из полей ввода
            String typeInput = typeColumn.getValue() != null ? typeColumn.getValue().toString() : "";
            String dateInput = dataColumn.getValue() != null ? dataColumn.getValue().toString() : "";
            String timeInput = timeColumn.getText();
            String durationInput = durationColumn.getText();
            String descriptionInput = descriptionColumn.getText();
            // Проверка, что все поля были заполнены
            if (typeInput.trim().isEmpty() || dateInput.trim().isEmpty() ||
                    timeInput.trim().isEmpty() || durationInput.trim().isEmpty() ||
                    descriptionInput.trim().isEmpty()) {
                showAlert("Ошибка", "Все поля должны быть заполнены.");
                return; // Прерываем выполнение метода, если какое-либо поле не заполнено
            }
            // Создание нового объекта Training с данными из полей ввода
            Training newTraining = new Training(
                    typeInput,
                    LocalDate.parse(dateInput),
                    LocalTime.parse(timeInput),
                    durationInput,
                    descriptionInput
            );
            trainingTable.getItems().add(newTraining); // Добавление нового объекта в таблицу
            clearInputFields(); // Очистка полей ввода
        } catch (DateTimeParseException e) {
            // Обработка ошибки парсинга времени или даты
            showAlert("Ошибка", "Неправильный формат времени.");
        }
    }

    @FXML // Метод для сохранения всех тренировок в файл
    private void saveTrainingToFile(ActionEvent actionEvent) {
        String homeDirectory = System.getProperty("user.home");
        String filePath = homeDirectory + "/trainings.txt"; // Используйте этот путь для сохранения файла в домашней директории
        try (FileWriter fw = new FileWriter(filePath, false); // false для перезаписи файла
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (Training training : trainingTable.getItems()) {
                // Проверка на null для даты и времени
                if (training.getDate() != null && training.getTime() != null) {
                    out.println(training.getType() + "," +
                            training.getDate().toString() + "," +
                            training.getTime().toString() + "," +
                            training.getDurationAsString() + "," +
                            training.getDescription());
                } else {
                    System.out.println("Одна из тренировок не содержит даты или времени и не будет сохранена.");
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    @FXML// Метод для редактирования выбранной тренировки
    private void editTraining() {
        // Получаем выбранную тренировку
        Training selectedTraining = trainingTable.getSelectionModel().getSelectedItem();
        if (selectedTraining != null) {
            // Отображаем данные выбранной тренировки в полях ввода
            typeColumn.setValue(selectedTraining.getType());
            dataColumn.setValue(selectedTraining.getDate());
            timeColumn.setText(selectedTraining.getTime().toString());
            durationColumn.setText(selectedTraining.getDuration());
            descriptionColumn.setText(selectedTraining.getDescription());
            // Делаем кнопку "Добавить" недоступной, а кнопку "Сохранить изменения" доступной
            addB.setDisable(true);
            saveB.setDisable(false);
            // Устанавливаем действие для кнопки "Сохранить изменения"
            saveB.setOnAction(event -> saveEditedTraining(selectedTraining));
        } else {
            // Если тренировка не выбрана, выводим сообщение об ошибке
            showAlert("Ошибка", "Необходимо выбрать тренировку для редактирования.");
        }
    }
    @FXML// Метод для сохранения отредактированных данных тренировки
    private void saveEditedTraining(Training trainingToEdit) {
        // Проверяем, что тип тренировки выбран
        if (typeColumn.getValue() != null) {
            trainingToEdit.setType(typeColumn.getValue().toString());
        } else {
            // Если тип не выбран, показываем предупреждение и прерываем выполнение метода
            showAlert("Ошибка", "Тип тренировки не выбран.");
            return;
        }
        // Обновляем данные тренировки новыми значениями из полей ввода
        trainingToEdit.setType(typeColumn.getValue().toString());
        trainingToEdit.setDate(dataColumn.getValue());
        trainingToEdit.setTime(LocalTime.parse(timeColumn.getText()));
        trainingToEdit.setDuration(durationColumn.getText());
        trainingToEdit.setDescription(descriptionColumn.getText());
        // Обновляем таблицу с новыми данными
        trainingTable.refresh();
        // Очищаем поля ввода и делаем кнопку "Добавить" снова доступной
        clearInputFields();
        addB.setDisable(false);
        saveB.setDisable(true);
    }
    @FXML// Метод для очистки полей ввода
    private void clearInputFields() {
        typeColumn.setValue(null); // или typeColumn.getSelectionModel().clearSelection();
        dataColumn.setValue(null);
        timeColumn.clear();
        durationColumn.clear();
        descriptionColumn.clear();
    }

    @FXML //Метод для удаления данных из таблицы
    private void deleteTraining(ActionEvent event) {
        Training selectedTraining = trainingTable.getSelectionModel().getSelectedItem();
        if (selectedTraining != null) {
            trainingTable.getItems().remove(selectedTraining);
        } else {
            showAlert("Ошибка", "Необходимо выбрать тренировку для удаления.");
        }
    }

    @FXML// Метод для сортировки по дате
    private void sortByDate(ActionEvent event) {
        SortedList<Training> sortedData = new SortedList<>(trainingTable.getItems(),
                (Training t1, Training t2) -> t1.getDate().compareTo(t2.getDate()));
        trainingTable.setItems(sortedData);
    }
    @FXML // Метод для сортировки по типу тренировки
    private void sortByType(ActionEvent event) {
        SortedList<Training> sortedData = new SortedList<>(trainingTable.getItems(),
                (Training t1, Training t2) -> t1.getType().compareTo(t2.getType()));
        trainingTable.setItems(sortedData);
    }

    @FXML //Обработчик события закрытия окна
    private void onCloseWindow(WindowEvent event) {
        saveTrainingToFile(new ActionEvent());
    }

    @FXML// Метод для отображения всплывающих уведомлений
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void switchTheme(ActionEvent actionEvent) {
    }

    // Внутренний класс для представления данных о тренировке
    public static class Training {
        private final StringProperty type;
        private final ObjectProperty<LocalDate> date;
        private final ObjectProperty<LocalTime> time;
        private final StringProperty duration;
        private final StringProperty description;

        public Training(String type, LocalDate date, LocalTime time, String duration, String description) {
            this.type = new SimpleStringProperty(type);
            this.date = new SimpleObjectProperty<>(date);
            this.time = new SimpleObjectProperty<>(time);
            this.duration = new SimpleStringProperty(duration);
            this.description = new SimpleStringProperty(description);
        }
        // Геттеры и сеттеры
        public String getType() {
            return type.get();
        }
        public void setType(String type) {
            this.type.set(type);
        }
        public StringProperty typeProperty() {
            return type;
        }
        public LocalDate getDate() {
            return date.get();
        }
        public void setDate(LocalDate date) {
            this.date.set(date);
        }
        public ObjectProperty<LocalDate> dateProperty() {
            return date;
        }
        public LocalTime getTime() {
            return time.get();
        }
        public void setTime(LocalTime time) {
            this.time.set(time);
        }
        public ObjectProperty<LocalTime> timeProperty() {
            return time;
        }
        public String getDurationAsString() { // Добавлен новый геттер
            return duration.get();
        }
        public void setDurationAsString(String duration) { // Добавлен новый сеттер
            this.duration.set(duration);
        }
        public StringProperty durationProperty() { // Изменено на StringProperty
            return duration;
        }
        public String getDescription() {
            return description.get();
        }
        public void setDescription(String description) {
            this.description.set(description);
        }
        public StringProperty descriptionProperty() {
            return description;
        }
        public String getDuration() {
            return duration.get();
        }
        public void setDuration(String duration) {
            this.duration.set(duration);
        }
    }
}
