package classes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class mainController {
    @FXML private TextField schedNameField;
    @FXML private Button createSchedBtn;
    @FXML private Button openSchedBtn;
    @FXML private Button deleteSchedBtn;
    @FXML private TextField taskNameField;
    @FXML private Button addTaskBtn;
    @FXML private Button editTaskBtn;
    @FXML private Button removeTaskBtn;
    @FXML private GridPane schedPane;
    @FXML private ComboBox<LocalTime> timePicker;
    @FXML private ComboBox<DayOfWeek> weekDayPicker;
    @FXML private Label statusLabel;
    @FXML private ScrollPane scrollPane;

    private String userDir;
        // userDir = directory name where the user's schedules will be saved
        // schedNmae = filename of the opened schedule
    private final ArrayList<LocalTime> times = new ArrayList<>();
    private static Timeslot[][] schedule = null;
        // 2D array of TasksPerTime (schedule timeslot) that looks similar to schedPane.
        // row_idx = time (based on times) = schedPane's row index
        // col_idx = day of week (1-7) - 1 = schedPane's col index - 1
    private static Path schedPath = null;
    private final LocalTime startTime = LocalTime.of(0, 0);
    private final LocalTime endTime = LocalTime.of(23, 59);
    private final Duration timeGap = Duration.ofMinutes(30);

    /**** FXML Related Methods ****/

    @FXML
    private void initialize() {
        LocalTime time = startTime;
        // Set-up times ArrayList and combo boxes.
        do {
            // Add time to times ArrayList.
            times.add(time);
            // Add as a choice in to timePicker combo box.
            timePicker.getItems().add(time);
            // Increment for next iteration.
            time = time.plus(timeGap);
        } while (!time.equals(startTime));
        // Set-up day of week combo box.
        weekDayPicker.getItems().addAll(DayOfWeek.values());
        disableTaskControls(true);
        statusLabel.setWrapText(true);
    }

    @FXML
    private void handleCreateSched() {
        if (schedNameField.getText().isEmpty()) {
            statusLabel.setText("Invalid input!");
        }
        else if (Files.exists(Path.of(userDir + schedNameField.getText() + ".txt"))) {
            statusLabel.setText("Schedule already exists!");
        }
        else {
            // Save existing schedule and clear schedPane, if any
            if (schedPath != null) {
                saveSched();
                schedPane.getChildren().clear();
            }
            // Initialize size of schedule array
            schedule = new Timeslot[getTimeIdx(endTime)][7];
            try {
                schedPath = Files.createFile(Path.of(
                        userDir + schedNameField.getText() + ".txt"));
                statusLabel.setText("Schedule ceated successfully!");
            } catch (IOException e) {
                statusLabel.setText("An error occurred.");
                e.printStackTrace();
            }
            initializeSchedule();
            setupSchedPane();
            if (addTaskBtn.isDisable()) {
                disableTaskControls(false);
            }
        }
        schedNameField.clear();
    }

    @FXML
    private void handleOpenSched() {
        if (schedNameField.getText().isEmpty()) {
            statusLabel.setText("Invalid input!");
        }
        else if (!Files.exists(Path.of(userDir + schedNameField.getText() + ".txt"))) {
            statusLabel.setText("Schedule not found!");
        }
        else {
            // Save existing schedule and clear schedPane, if any
            if (schedPath != null) {
                saveSched();
                schedPane.getChildren().clear();
            }
            schedPath = Path.of(userDir + schedNameField.getText() + ".txt");
            // Initialize new schedule array size
            schedule = new Timeslot[getTimeIdx(endTime)][7];
            loadSched();
            setupSchedPane();
            statusLabel.setText("Schedule loaded successfully!");
            if (addTaskBtn.isDisable()) {
                disableTaskControls(false);
            }
        }
        schedNameField.clear();
    }

    @FXML
    private void handleDeleteSched() {
        Path path;
        if (schedNameField.getText().isEmpty()) {
            statusLabel.setText("Invalid input!");
            return;
        }
        path = Path.of(userDir + schedNameField.getText() + ".txt");
        try {
            if (Files.deleteIfExists(path)) {
                statusLabel.setText("Schedule deleted successfully!");
                if (schedPath!=null && schedPath.equals(path)) {
                    schedPath = null;
                    schedPane.getChildren().clear();
                    schedule = null;
                    disableTaskControls(true);
                } else {
                    saveSched();
                }
            } else {
                statusLabel.setText("Schedule not found!");
            }
        } catch (IOException e) {
            statusLabel.setText("An error occurred.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddTask() {
        if (taskNameField.getText().isEmpty() ||
                timePicker.getSelectionModel().isEmpty() ||
                weekDayPicker.getSelectionModel().isEmpty()) {
            statusLabel.setText("Incomplete Input!");
            return;
        }
        LocalTime time = timePicker.getValue();
        // Get the index of the chosen time in times list to serve as row # of schedPaneList.
        int row = times.indexOf(time);
        // Get the chosen day of week int value (1-7) to serve as col # of schedPaneList.
        int col = weekDayPicker.getValue().getValue() -1;
        // Add task.
        schedule[row][col].addTask(taskNameField.getText());
        statusLabel.setText("Task Added!");
        taskNameField.clear();
        // Focus on task
        //ScrollPane scrollPane = (ScrollPane) schedPane.getParent().getParent();
        scrollPane.setVvalue(row*55 / schedPane.getHeight());
    }

    @FXML
    private void handleEditTask() {
        if (taskNameField.getText().isEmpty() ||
                timePicker.getSelectionModel().isEmpty() ||
                weekDayPicker.getSelectionModel().isEmpty()) {
            statusLabel.setText("Incomplete Input!");
            return;
        }
        String task = taskNameField.getText();
        int row = times.indexOf(timePicker.getValue());
        int col = weekDayPicker.getValue().getValue() -1;
        boolean isRemoved = schedule[row][col].removeTask(task);
        if (!isRemoved) {
            statusLabel.setText("Task not found!");
            return;
        }

        // Create edit task scene layout
        Label label = new Label("Edit Task");
        label.setId("header1");
        label.setMaxWidth(Double.MAX_VALUE);
        // Edit task name nodes
        Label label1 = new Label("Name: ");
        label1.setTextFill(Color.WHITE);
        label.setMaxWidth(Double.MAX_VALUE);
        TextField nameInput = new TextField();
        nameInput.setText(task);
        nameInput.getStyleClass().add("textFieldDarkBG");
        nameInput.setMaxWidth(Double.MAX_VALUE);
        // Edit time nodes
        Label label2 = new Label("Time: ");
        label2.setTextFill(Color.WHITE);
        label2.setMaxWidth(Double.MAX_VALUE);
        ComboBox<LocalTime> timeInput = new ComboBox<>();
        timeInput.getItems().addAll(times);
        timeInput.getSelectionModel().select(row);
        timeInput.setMaxWidth(Double.MAX_VALUE);
        timeInput.setStyle("-fx-border-color: transparent");
        // Edit day of week nodes
        Label label3 = new Label("Day of Week: ");
        label3.setTextFill(Color.WHITE);
        ComboBox<DayOfWeek> dayInput = new ComboBox<>();
        dayInput.getItems().addAll(DayOfWeek.values());
        dayInput.setValue(DayOfWeek.of(col+1));
        dayInput.setMaxWidth(Double.MAX_VALUE);
        dayInput.setStyle("-fx-border-color: transparent");
        // GridPane for edits
        GridPane gridPane = new GridPane();
        gridPane.addRow(0, label1, nameInput);
        gridPane.addRow(1, label2, timeInput);
        gridPane.addRow(2, label3, dayInput);
        gridPane.setHgap(10);
        gridPane.setVgap(20);
        gridPane.setAlignment(Pos.CENTER);
        // Save edit button
        Button btn = new Button("Save Edit");
        btn.getStyleClass().add("buttonDarkBG");
        btn.setAlignment(Pos.CENTER);
        // VBox (scene)
        VBox vbox = new VBox(label, gridPane, btn);
        vbox.getStylesheets().add("styles/mainStylesheet.css");
        vbox.getStyleClass().add("vbox");
        vbox.setSpacing(30);
        vbox.setPadding(new Insets(50));
        // Swap scene
        Scene primaryScene = taskNameField.getScene();
        Stage stage = (Stage) taskNameField.getScene().getWindow();
        stage.setScene(new Scene(vbox, 350, 400));
        // Successful edit actions
        btn.setOnAction(e -> {
            schedule[getTimeIdx(timeInput.getValue())][dayInput.getValue().getValue()-1].
                    addTask(nameInput.getText());
            stage.setScene(primaryScene);
        });
        statusLabel.setText("Task edits processed successfully!");
        taskNameField.clear();
    }

    @FXML
    private void handleRemoveTask() {
        if (taskNameField.getText().isEmpty() ||
                timePicker.getSelectionModel().isEmpty() ||
                weekDayPicker.getSelectionModel().isEmpty()) {
            statusLabel.setText("Incomplete Input!");
            return;
        }
        String task = taskNameField.getText();
        int row = times.indexOf(timePicker.getValue());
        int col = weekDayPicker.getValue().getValue() -1;
        boolean isRemoved = schedule[row][col].removeTask(task);
        if (!isRemoved) {
            statusLabel.setText("Task not found!");
        } else {
            statusLabel.setText("Task removed successfully!");
        }
        taskNameField.clear();
    }


    /** Non-FXML methods **/

    // Initialize new schedule with blank timeslots (no tasks).
    private void initializeSchedule() {
        for (int i = 0; i < times.size(); i++) {
            for (DayOfWeek day : DayOfWeek.values()) {
                ObservableList<String> list = FXCollections.observableArrayList();
                schedule[i][day.getValue()-1] = new Timeslot(times.get(i), day, list);
            }
        }
    }

    // Set-up schedule and schedPane to display new/blank schedule.
    // Assumes that schedPane has been cleared prior to calling this method.
    private void setupSchedPane() {
        for (int row = 0; row < times.size(); row++) {
            // Display time to schedPane as row header.
            Label label = new Label(times.get(row).toString());
            label.getStyleClass().clear();
            label.getStyleClass().add("gridCellRowHeader");
            schedPane.add(label, 0, row);
            // Display tasksList for each day of week in schedPane.
            for (DayOfWeek day: DayOfWeek.values()) {
                ListView<String> listView = new ListView<>();
                listView.setItems(schedule[row][day.getValue()-1].getTasksList());
                schedPane.add(listView, day.getValue(), row);
            }
        }
    }

    // Save schedule in an existing text file by writing the contents to schedPath.
    public static void saveSched() {
        StringBuilder content = new StringBuilder();
        String temp;
        if (schedPath == null) return;
        // Format for each timeslot: <time>|<day>|<tasks separated by ::>
        // General delimiter:  "|"     Tasks delimiter: "::"
        for (Timeslot[] time : schedule) {
            for (Timeslot timeslot : time) {
                content.append(timeslot.getTime().toString()).append("|");
                content.append(timeslot.getDayInt()).append("|");
                // Iterate thru each task in timeslot's task lists
                Iterator<String> iterator = timeslot.getTasksList().iterator();
                if (iterator.hasNext()) {
                    temp = iterator.next();
                    content.append(temp);
                    while (iterator.hasNext()) {
                        temp = iterator.next();
                        content.append("::").append(temp);
                    }
                }
                content.append("\r");
            }
        }
        // Write content to schedule's text file whose path is pointed to by schedPath
        try { Files.writeString(schedPath, content); }
        catch (IOException e) { e.printStackTrace(); }
    }

    // Read schedule contents from text file pointed by schedPath to schedule.
    // Main reference: https://www.baeldung.com/java-read-lines-large-file
    private void loadSched() {
        FileInputStream inputStream = null;
        Scanner sc = null;
        boolean flag = false;
        try {
            inputStream = new FileInputStream(String.valueOf(schedPath));
            sc = new Scanner(inputStream, StandardCharsets.UTF_8);
            if (sc.hasNextLine()) flag = true;
            while (sc.hasNextLine()) {
                // Get timeslot attributes (time, day, tasks)
                String[] items = sc.nextLine().split("\\|");
                LocalTime time = LocalTime.parse(items[0]);
                DayOfWeek day = DayOfWeek.of(Integer.parseInt(items[1]));
                // Get tasks
                ObservableList<String> list;
                if (items.length == 2) {  // when there's no task
                    list = FXCollections.observableArrayList();
                } else {
                    String[] tasks = items[2].split("::");
                    list = FXCollections.observableArrayList(tasks);
                }
                // Add timeslot to schedule
                schedule[getTimeIdx(time)][day.getValue()-1] = new Timeslot(time, day, list);
            }
            if (sc.ioException() != null) {
                throw sc.ioException();  // since Scanner suppresses exceptions
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!flag) initializeSchedule();
            if (inputStream != null) {
                try { inputStream.close(); }
                catch (IOException e) { e.printStackTrace(); }
            }
            if (sc != null) {
                sc.close();
            }
        }
    }

    // Computes for the index of a specified time that should be in times list and returns it.
    private int getTimeIdx(LocalTime time) {
        return (int) Math.ceil(Duration.
                between(LocalTime.MIDNIGHT, time).toMinutes() / 30.0);
    }

    // Setter for username; to be called in Main class after loading the mainController.
    // Also checks if a directory of username exists, if not, it creates one.
    public void setUserDirectory(String username) {
        this.userDir = "databases/" + username;
        if (!Files.isDirectory(Path.of(userDir))) {
            try {
                Files.createDirectory(Path.of(userDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        userDir = userDir.concat("/");
    }

    private void disableTaskControls(boolean bool) {
        addTaskBtn.setDisable(bool);
        editTaskBtn.setDisable(bool);
        removeTaskBtn.setDisable(bool);
        taskNameField.setDisable(bool);
        timePicker.setDisable(bool);
        weekDayPicker.setDisable(bool);
    }

}
