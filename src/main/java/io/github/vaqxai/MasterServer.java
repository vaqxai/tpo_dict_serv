package io.github.vaqxai;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MasterServer {

    HashMap<Lang,Integer> dictServers = new HashMap<>(); // language, port (ip is our own)
    VBox vbox;
    Stage stg;

    public void start(Stage stg) {
        this.stg = stg;
        stg.setTitle("Server");

        System.out.println("Starting language master server");
        
        var root = new HBox();
        vbox = new VBox();
        var label = new Label("Language master server running on 8080.\nLanguage servers currently deployed: ");

        vbox.getChildren().add(label);

        root.getChildren().add(vbox);

        var scene = new Scene(root);
        stg.setScene(scene);
        stg.show();

        startDictionaries();
        System.out.println("Dictionary servers started");

        new Thread(new Runnable() {
            public void run(){
                try (var listen = new ServerSocket(8080)) {
        
                    while (true) {
                        System.out.println("Waiting for connection");
                        Socket client = listen.accept();
                        ClientHandler clientHandler = new ClientHandler(client, dictServers);
                        new Thread(clientHandler).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        
            }
        }).start();
    }

    public void startDictionary(Lang l, int port){
        final int _port;
        if(port == 0){
            _port = 8081 + dictServers.size();
        } else {
            _port = port;
        }

        var label = new Label(l + " Starting...");
        vbox.getChildren().add(label);
        dictServers.put(l, _port);
        CompletableFuture<String> serverStatus = new CompletableFuture<>();
        Thread dictThread = new Thread(new DictionaryServer(l, dictServers.get(l), serverStatus));
        dictThread.start();
        while(!serverStatus.isDone()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            if(serverStatus.get().equals("success")){
                label.setText("\n" + l + " on " + Inet4Address.getLocalHost().getHostAddress() + ":" + dictServers.get(l));
            } else {
                label.setText("\n" + l + " ERROR: " + serverStatus.get());
                var retryBtn = new Button("Retry");
                vbox.getChildren().add(retryBtn);
                retryBtn.setOnAction(e -> {
                    vbox.getChildren().remove(retryBtn);
                    vbox.getChildren().remove(label);
                    dictServers.remove(l);
                    System.out.println(dictServers.toString());
                    dictThread.interrupt();
                    startDictionary(l, _port);
                    return;
                });
            }
        } catch (UnknownHostException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        stg.sizeToScene();
    }

    public void startDictionaries(){
        var langs = Lang.values();
        for (int i = 0; i < langs.length; i++) {
            startDictionary(langs[i], 0);
        }
    }
    
}
