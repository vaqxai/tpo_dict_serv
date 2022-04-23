package io.github.vaqxai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import org.json.*;

public class DictionaryServer implements Runnable {
    
    private final Lang lang;
    ServerSocket listen;
    private final int listenPort;
    HashMap<String, String> translations; // key = polish, value = translation in <<Lang>>
    public CompletableFuture<String> status;

    public DictionaryServer(Lang lang, int listenPort, CompletableFuture<String> status){
        this.lang = lang;
        this.listenPort = listenPort;
        this.status = status;
    }

    public void run(){
        System.out.println("Starting DictionaryServer for " + lang + " on port " + listenPort);

        translations = new HashMap<>();

        String jsonString = "";

        try {
    
            var f = new File(lang.toString() + ".json");
            if (!f.exists()) throw new IOException("File does not exist");
            if (!f.isFile()) throw new IOException("Not a file");
            if (!f.canRead()) throw new IOException("Cannot read file");

            BufferedReader fr = new BufferedReader(new FileReader(lang.toString() + ".json"));
            for(String line; (line = fr.readLine()) != null; ) {
                jsonString += line;
            }
            fr.close();
		} catch (IOException e1) {
            status.complete("Error: dictionary file not found (" + new File(lang.toString() + ".json").getAbsolutePath() + ")");
            return;
		}
        System.out.println(jsonString);
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            translations.put(jsonObject.getString("polish"), jsonObject.getString("translation"));
        }
        

        System.out.println("DictionaryServer for " + lang + " now listening on port " + listenPort);
        try {
            listen = new ServerSocket(listenPort);
            status.complete("success");
            while (true) {
                var clientSocket = listen.accept(); // connection comes from master server

                var reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
               
                var ip = reader.readLine();
                var port = Integer.parseInt(reader.readLine());
                var translation = reader.readLine();
                
                System.out.println("Translating: " + translation);

                if(translations.containsKey(translation))
                    translation = translations.get(translation);
                else
                    translation = "Word not in dictionary";

                reader.close();
                Socket responder = new Socket();
                responder.connect(new InetSocketAddress(ip, port));

                System.out.println("Responding to " + ip + ":" + port);

                var writer = new PrintWriter(responder.getOutputStream());
                writer.println(translation);

                writer.close();
                responder.close();
            }
        } catch (Exception e) {
            System.out.println("Error while starting dictionary server for " + lang + ": " + e);
            status.complete("Error: " + e.getMessage() + " on port " + listenPort);
        }
    }


}
