package scenesNcontrollers;

import classes.TasksPerTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;

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

    private String username;
        // username = directory name where the user's schedules will be saved
    private final ArrayList<LocalTime> times = new ArrayList<>();
    protected TasksPerTime[][] schedule = null;
        // 2D array of TasksPerTime (schedule timeslot) that looks similar to schedPane.
        // row_idx = time (based on times) = schedPane's row index
        // col_idx = day of week (1-7) - 1 = schedPane's col index - 1
    private final LocalTime startTime = LocalTime.of(0, 0);
    private final LocalTime endTime = LocalTime.of(23, 59);
    private final Duration timeGap = Duration.ofMinutes(30);
    private final int schedRows = (int) Math.ceil(Duration.
            between(LocalTime.MIDNIGHT, endTime).toMinutes() / 30.0);

    @FXML
    private void initialize() {
        int row=0;
        LocalTime time = startTime;
        System.out.println("Count:" + schedRows + "Time: " + Duration.between(LocalTime.MIDNIGHT, endTime).toString());
        // Initialize schedule array size
        schedule = new TasksPerTime[schedRows][7];
        System.out.println("Size: " + times.size());

        // Set-up schedPane, schedule timeslots, and combo boxes.
        do {
            // Add time to times ArrayList.
            times.add(time);
            // Add time to schedule as time slot for each day of the week.
            for (DayOfWeek day: DayOfWeek.values()) {
                ObservableList<String> list = FXCollections.observableArrayList();
                schedule[row][day.getValue()-1] = new TasksPerTime(time, day, list);
                ListView<String> listView = new ListView<>();
                listView.setItems(list);
                //listView.setMaxWidth(Double.MAX_VALUE);
                //listView.setMaxHeight(75);
                schedPane.add(listView, day.getValue(), row);
            }
            // Display time to schedPane as row header.
            Label label = new Label(time.toString());
            label.getStyleClass().clear();
            label.getStyleClass().add("gridCellRowHeader");
            schedPane.add(label, 0, row);
            // Add as a choice in to timePicker combo box.
            timePicker.getItems().add(time);
            // Increment for next iteration.
            time = time.plus(timeGap);
            row++;
        } while (!time.equals(startTime));
        System.out.println("Table set-up complete");
        // Set-up day of week combo box.
        weekDayPicker.getItems().addAll(DayOfWeek.values());
        System.out.println("Size: " + times.size() + "Row: " + row);
    }

    @FXML
    private void handleAddTask() {
        if (taskNameField.getText().isBlank() &&
                timePicker.getValue()==null && weekDayPicker.getValue()==null) {
            return;
        }
        // Get the index of the chosen time in times list to serve as row #.
        int row = times.indexOf(timePicker.getValue());
        // Get the chosen day of week int value (1-7; mon-sun) to serve as col #.
        int col = weekDayPicker.getValue().getValue()-1;

        schedule[row][col].addTask(taskNameField.getText());

        /** TODO:
         * grid pane displays tasks base on their starting time
         * edit tasks pulls up a new scene, use global Variable
         * tasks have no end time
         * tasks names are mutable
         *
         * TIMER:
         * - for starting time only??
        **/
    }

    // Set-up schedPane to display new schedule/blank schedule.
    // Assumes that schedPane has been cleared prior to calling this method.
    private void setupSchedPane() {
        for (int row = 1; row <= times.size(); row++) {
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

    // Setter for username; to be called in Main class after loading the mainController.
    public void setUsername(String username) {
        this.username = username;
    }


}
