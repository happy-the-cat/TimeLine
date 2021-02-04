/* TasksPerTime class contains the variables for each timeslot in the schedule,
* namely: time, day of week, and names of the tasks scheduled for that timeslot. */

package classes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class TasksPerTime {
    private LocalTime time;
    private DayOfWeek day;
    private ObservableList<String> tasksList;

    public TasksPerTime(LocalTime time, DayOfWeek day, ObservableList<String> tasksList) {
        this.time = time;
        this.day = day;
        this.tasksList = tasksList;
    }

    public TasksPerTime(LocalTime time, DayOfWeek day, String name) {
        this.time = time;
        this.day = day;
        this.tasksList = FXCollections.observableArrayList(name);
    }

    public LocalTime getTime() {
        return time;
    }
    public void setTime(LocalTime time) {
        this.time = time;
    }
    public DayOfWeek getDay() {
        return day;
    }
    public void setDay(DayOfWeek day) {
        this.day = day;
    }
    public ObservableList<String> getTasksList() {
        return tasksList;
    }
    public void setTasksList(ObservableList<String> names) {
        this.tasksList = names;
    }
    public void addTask(String name) {
        this.tasksList.add(name);
    }


}
