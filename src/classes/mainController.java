package classes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

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

    protected String userDir;
        // userDir = directory name where the user's schedules will be saved
        // schedNmae = filename of the opened schedule
    protected final ArrayList<LocalTime> times = new ArrayList<>();
    protected Timeslot[][] schedule = null;
        // 2D array of TasksPerTime (schedule timeslot) that looks similar to schedPane.
        // row_idx = time (based on times) = schedPane's row index
        // col_idx = day of week (1-7) - 1 = schedPane's col index - 1
    private Path schedPath = null;
    private final LocalTime startTime = LocalTime.of(0, 0);
    private final LocalTime endTime = LocalTime.of(23, 59);
    private final Duration timeGap = Duration.ofMinutes(30);


    /** TODO
     * store only the timeslot with task
     * time|day|tasks separated by ||
     * use formula for finding schedrows to find index of a timeslot (idx of time)
     *
     * SCHED FILE
     * -open sched opens a schedfile for editing
     * -create sched creates a new txtfild for editing
     * -delete sched deletes a txtfile from directory
     * -all three functions checks if schedfile is already open
     *    - if it is, overwrite textfile to save, then close it n proceed its main operation
     *    - if not, proceed main operation
     *
     * ADD TASK
     * check if timeslot exists in schedule (search)
     * if it does, get the timeslot obj and add task
     * if not, add new timeslot
     *
     * EDIT TASK
     * new scene (sth like view cart, make sched protected first (global))
     * check if task exists (search schedule)
     * if yes, remove task in the timeslot obj, then add new task
     * if not, update status bout it then do nothing
     *
     *
     */

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
        boolean isInSched = false;
        // Get the index of the chosen time in times list to serve as row # of schedPaneList.
        int row = times.indexOf(time);
        // Get the chosen day of week int value (1-7) to serve as col # of schedPaneList.
        int col = weekDayPicker.getValue().getValue() -1;
        // Add task.
        schedule[row][col].addTask(taskNameField.getText());
        statusLabel.setText("Task Added!");
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
    private void saveSched() {
        StringBuilder content = new StringBuilder();
        String temp;
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
        try {
            inputStream = new FileInputStream(String.valueOf(schedPath));
            sc = new Scanner(inputStream, StandardCharsets.UTF_8);
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
        this.userDir = "databases/" + username + "/";
        if (!Files.isDirectory(Path.of(username))) {
            try {
                Files.createDirectory(Path.of(username));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
