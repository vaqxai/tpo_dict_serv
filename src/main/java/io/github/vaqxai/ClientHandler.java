package io.github.vaqxai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

public class ClientHandler implements Runnable{

    private Socket client;
    private HashMap<Lang, Integer> dictServers;

    public ClientHandler(Socket clientSocket, HashMap<Lang, Integer> dictServers) {
        this.client = clientSocket;
        this.dictServers = dictServers;
    }
    
    public void run () {
        System.out.println("Client connected");

        // forward to appropriate dictionary server

        try (var reader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
			System.out.println("Reader established");

			var lang = reader.readLine();
			var textToTranslate = reader.readLine();

			reader.close();

			System.out.println("Reading language: " + lang);
			System.out.println("Reading text: " + textToTranslate);

			Socket proxySender = new Socket();

			proxySender.connect(new InetSocketAddress("localhost", dictServers.get(Lang.valueOf(lang))));
			
			var writer = new PrintStream(proxySender.getOutputStream());

			writer.println(client.getInetAddress().getHostAddress());
			writer.println(client.getPort());
			writer.println(textToTranslate);

			writer.close();
			proxySender.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}
