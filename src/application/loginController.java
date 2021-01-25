package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;

public class loginController {

    @FXML private Label statusLabel;
    @FXML private  TextField usernameField;
    @FXML private  TextField passwordField;

    private String dbFilename;
    private final LinkedList<String[]> database = new LinkedList<>();

    @FXML  // Handle signup button; new user registration.
    private void handleSignup() {
        boolean flag = false;
        String username = usernameField.getText();
        if (username.isBlank() && passwordField.getText().isBlank()) return;
        if (!statusLabel.isVisible()) statusLabel.setVisible(true);
        // Check if inputted username exists already (taken).
        for (String[] userpass : database) {
            if (userpass[0].toLowerCase().equals(username.toLowerCase())) {
                statusLabel.setText("Username is taken!");
                flag = true;
                usernameField.clear();
                passwordField.clear();
                break;
            }
        }
        // When username is not taken, register new user.
        if (!flag) {
            database.add(new String[]{username, passwordField.getText()});
            updateDatabaseFile();
            statusLabel.setText("Account created successfully!");
            createDirectory(username);  // Create a directory named after the username.
            loadMainScene(username);
        }
    }

    @FXML  // Handle login button; user attempts login to existing account.
    private void handleLogin() {
        boolean flag = false;
        if (usernameField.getText().isBlank() && passwordField.getText().isBlank()) return;
        if (!statusLabel.isVisible()) statusLabel.setVisible(true);
        // Validate inputted username and password.
        for (String[] userpass: database) {
            if (userpass[0].equals(usernameField.getText()) && userpass[1].equals(passwordField.getText())) {
                statusLabel.setText("Login Successful!");
                flag = true;
                break;
            }
        }
        if (!flag) {
            statusLabel.setText("Login Failed. Please try again.");
            usernameField.clear();
            passwordField.clear();
            return;
        }
        loadMainScene(usernameField.getText());
    }

    // Overwrite database text file with updated user-pass entries.
    private void updateDatabaseFile() {
        try {
            FileWriter fw = new FileWriter(new File(dbFilename));
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(database.size() + "");  // Write number of user-pass entry in the file first.
            for (String[] userpass : database) {  // Write user-pass entries.
                bw.write("\r" + userpass[0] + ", " + userpass[1]);
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            System.out.println("An error occured. Couldn't register user to text file.");
        }
    }

    // Read username-password entries from textfile to database LinkedList.
    private void readDatabaseFile() {
        int n, i;
        String temp;
        Scanner scan;
        // Open the file and read the contents
        try {
            scan = new Scanner(new File(dbFilename));
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            return;
        }
        // Read and store the contents (first line is users count)
        n = scan.nextInt();     // Get the number of users in database
        scan.nextLine();
        for (i=0; i<n; i++) {
            temp = scan.nextLine();
            database.add(temp.split(", "));
        }
    }

    // Create directory with the given name inside the "databases" directory.
    private void createDirectory(String dirName) {
        File dir = new File("databases\\" + dirName);  // Create File object
        boolean bool = dir.mkdir();  // Create directory
        if (bool) {
            System.out.println("Directory creates successfully.");
        } else {
            System.out.println("An error occurred. Unable to create directory.");
        }
    }

    // Load main stage UI.
    private void loadMainScene(String username) {
        Rectangle2D screen = Screen.getPrimary().getBounds();
        // get the stage statusLabel belongs to
        Stage primaryStage = (Stage) statusLabel.getScene().getWindow();
        try {
            // load loginUI.fxml
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainUI.fxml"));
            Parent root = fxmlLoader.load();
            // pass username to loginUI.fxml's controller
            mainController controller = fxmlLoader.getController();
            controller.setUsername(username);
            // swap scene
            primaryStage.setScene(new Scene(root, 0.8*screen.getWidth(), 0.8*screen.getHeight()));
        } catch (IOException e) {
            e.getCause();
        }
    }

    @FXML
    private void initialize() {
        statusLabel.setText("");
        statusLabel.setVisible(false);
        dbFilename = "databases\\_general.db";
        readDatabaseFile();
    }


}
