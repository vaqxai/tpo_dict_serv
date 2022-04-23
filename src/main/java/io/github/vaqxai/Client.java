package io.github.vaqxai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Client {

    public Client(){
    }

    public void start(Stage stg) {

        var root = new HBox();
        var scene = new Scene(root);

        stg.setScene(scene);

        stg.setTitle("Client");

        VBox vbox = new VBox();
        root.getChildren().add(vbox);

        var label = new Label("Choose a language to translate to");
        vbox.getChildren().add(label);

        var langList = Arrays.stream(Lang.values()).map(Lang::toString).collect(Collectors.toList());
        var dropdown = new ComboBox<String>(FXCollections.observableList(langList));
        vbox.getChildren().add(dropdown);

        var label2 = new Label("Enter text to be translated:");
        vbox.getChildren().add(label2);

        var textEntry = new TextField();
        vbox.getChildren().add(textEntry);

        var translatebutton = new Button("Translate");
        vbox.getChildren().add(translatebutton);

        var translationLabel = new Label("Translation:");
        vbox.getChildren().add(translationLabel);

        translatebutton.setOnAction(e -> {
            var lang = dropdown.getValue();
            var text = textEntry.getText();
        
            Socket requester = new Socket();
            var translated = "";
            try {
            requester.connect(new InetSocketAddress("localhost", 8080)); // hardcoded port

            var writer = new PrintStream(requester.getOutputStream());

            writer.println(lang);
            writer.println(text);

            System.out.println("Written! " + lang + " " + text);

            writer.close();
            requester.close();

            ServerSocket responseSocket = new ServerSocket(requester.getLocalPort());
            System.out.println("Waiting for response at port " + requester.getLocalPort());
            Socket response = responseSocket.accept(); // wait for response

            var reader = new BufferedReader(new InputStreamReader (response.getInputStream()));
            
            translated = reader.readLine();

            System.out.println("Got response");

            reader.close();
            response.close();
            responseSocket.close();

            System.out.println(translated);
            translationLabel.setText("Translation:\n" + translated);
            textEntry.setText("");
            stg.sizeToScene();

            } catch (IOException e1) {
                var alert = new Alert(AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText("Could not connect to the translation server");
                alert.setContentText(e1.getMessage());
                alert.show();
            }
        });

        stg.show();
    }
}
