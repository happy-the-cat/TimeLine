package classes;

import javafx.beans.property.SimpleStringProperty;

public class Task {
    private final SimpleStringProperty time;
    private final SimpleStringProperty name;


    public Task(String name, String time) {
        this.name = new SimpleStringProperty(name);
        this.time = new SimpleStringProperty(time);
    }

    public String getTime() {
        return time.get();
    }
    public void setTime(String time) {
        this.time.set(time);
    }
    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }


}
