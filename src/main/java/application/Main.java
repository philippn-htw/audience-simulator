package application;
	
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;




import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
//import javafx.scene.layout.BorderPane; // vertikal
import javafx.scene.layout.HBox;	// horizontal

public class Main extends Application {
	@Override
	public void start(Stage stage) {  // 
		try {
			//jeder Layout Container ist ein Parent 
			//das ist die Superklasse davon
			
			
			Parent root = FXMLLoader.load(getClass().getResource("/fxml/screen1.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
			
			
			
			// Root Node - Layout der Szene
			HBox root1 = new HBox();    // HBox ist ein Layout Container welcher ermöglicht
			root1.setPrefSize(400.0, 70.0);  // 370 in der Breite und 70 in der Höhe
			root1.setSpacing(40.0);   // der Abstand zwischen den Button
			root1.setAlignment(Pos.CENTER);
			
			HBox hbox = new HBox();
			hbox.getStyleClass().add("hbox");
			
			HBox hb = new HBox();
			hb.getStyleClass().add("hbox");
			
			
			
			//Elemente erstellen wie Button etc.
			Button button1 = new Button();		// die 5 Button sollen horizontal angeordnet werden 
			Button button2 = new Button();
			
			//Elemente zum rode Node hinzufügen
			
			root1.getChildren().add(button1);  //hier bekommt man diese buttons in den Layout Container
			root1.getChildren().add(button2);
			
			
			//jedem Button eine Größe geben
			button1.setPrefSize(100.0, 64.01);  // hier wird bestimmt wie hoch und breit der Button sein soll
			button2.setPrefSize(100.0, 64.01);
			
			//
			
			button1.getStyleClass().add("button1");
			button2.getStyleClass().add("button2");
			
			
			
			//!!! EVENT HANDLER 
			
			//Elemente klickbar machen - Eventhandling 
			
			//macht Button klickbar
			button1.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					//getHostServices().showDocument(uri);
					
					System.out.println("gib aus ");
					// TODO Auto-generated method stub
					
				}
			}   );

			//Szene erstellen
			
		
			//BorderPane root = new BorderPane(); // das ist das Layout "root" ist ein Anfangspunkt
			//Scene scene1 = new Scene(root1,400,400);
			
			//scene1.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			//primarystage.setScene(scene1);
			//primarystage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() {
		if(Settings.getSim()!= null) {
			Settings.getSim().stop();
		}
	}
		

	
	public static void main(String[] args) {
		launch(args);
	}	
}
