package io.github.vaqxai;
import javafx.application.*;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Hello world!
 *
 */
public class DictServ extends Application
{
    public void start( Stage stg ) {
        stg.setTitle( "Select program mode" );

        var radioButton1 = new RadioButton("Client" );
        var radioButton2 = new RadioButton("Server");

        var toggleGroup1 = new ToggleGroup();

        radioButton1.setToggleGroup( toggleGroup1 );
        radioButton2.setToggleGroup( toggleGroup1 );

        var label = new Label("Please select program mode");

        var pushButton1 = new Button("Start");

        pushButton1.setOnAction( e -> {
            if ( toggleGroup1.getSelectedToggle() == radioButton1 ) {
                stg.setTitle( "Client" );
                var client = new Client();
                stg.hide();
                client.start( stg );
            }
            else if ( toggleGroup1.getSelectedToggle() == radioButton2 ) {
                stg.setTitle( "Server" );
                var server = new MasterServer();
                stg.hide();
                server.start( stg );
            }
        });

        var vbox = new VBox();

        vbox.getChildren().add( label );
        vbox.getChildren().addAll( radioButton1, radioButton2 );
        vbox.getChildren().add(pushButton1);

        HBox.setMargin(vbox, new Insets(10,10,10,10));

        var root = new HBox();

        root.getChildren().add(vbox);

        var scene = new Scene( root );

        stg.setResizable(false);

        stg.setScene( scene );

        stg.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        stg.show();
    }

    public void runServer(Stage stg) {

    }

    public void runClient(Stage stg, boolean isAdmin) {

    }
}
