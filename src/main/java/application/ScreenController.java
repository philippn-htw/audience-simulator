package application;

import javafx.stage.Stage;
import processing.sound.AudioIn;
import simulation.AudioDevice;
import simulation.MainApplet;
import simulation.MediaInterface;
import simulation.Simulation;
import simulation.VideoDevice;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCaptureException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

/**
 * Manages all events and processes triggered from the GUI
 * @author Magomed Baidaev
 *
 */
public class ScreenController {

	private Stage stage;
	private Scene scene;
	private Parent root1;

	// settings items
	@FXML
	private ChoiceBox<Integer> samplerate;
	@FXML
	private ChoiceBox<String> audioInputDevice;
	@FXML
	private ChoiceBox<String> videoInputDevice;
	@FXML
	private Button startStopSimButton;
	@FXML
	private ChoiceBox<String> sampleRateChoice;
	@FXML
	private ChoiceBox<String> videoResolutionChoice;
	@FXML
	private ChoiceBox<String> qualityChoice;

	private MediaInterface audio = new AudioDevice();
	private MediaInterface video = new VideoDevice();
	
	/**
	 * Switch to simulator configuration 4
	 * @param event
	 * @throws IOException if fxml or css not reachable
	 */
	public void switchToScenesimulator4(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/simulator4.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
		
		Settings.setLocation(1);
	}
	
	/**
	 * Switch to simulator configuration 3
	 * @param event
	 * @throws IOException if fxml or css not reachable
	 */
	public void switchToScenesimulator3(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/simulator3.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
		
		Settings.setLocation(3);
	}
	
	/**
	 * Switch to simulator configuration 2
	 * @param event
	 * @throws IOException if fxml or css not reachable
	 */
	public void switchToScenesimulator2(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/simulator2.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
		
		Settings.setLocation(2);
	}
	
	/**
	 * Switch to simulator configuration 1
	 * @param event
	 * @throws IOException if fxml or css not reachable
	 */
	public void switchToScenesimulator(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/simulator.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
		
		Settings.setLocation(0);

	}
	
	/**
	 * Switch to welcome screen
	 * @param event
	 * @throws IOException if fxml or css not reachable
	 */
	public void switchToScenewelcome(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/welcome.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();

	}
	
	/**
	 * Switch to screen 1
	 * @param event
	 * @throws IOException if fxml or css not reachable
	 */
	public void switchToScene1(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/screen1.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();

	}
	
	/**
	 * Switch to screen 2
	 * @param event
	 * @throws IOException if fxml or css not reachable
	 */
	public void switchToScene2(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/screen2.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();

	}
	
	/**
	 * Switch to screen 3
	 * @param event
	 * @throws IOException if fxml or css not reachable
	 */
	public void switchToScene3(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/screen3.fxml"));
		stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}
	
	
	
	
	/**
	 * Start the simulation when it is not running or stops it if it is running.
	 */
	public void startStopSimulation() {
		if(Settings.getIsRunning()) {
			try {
				Settings.getSim().stop();
				
			}catch(NullPointerException e) {
				System.out.println("No Simulation found");
			}
			Settings.setIsRunning(false);
			startStopSimButton.setText("Start Simulation");
		} else {
			Simulation sim = new Simulation((AudioIn)audio.getStream(Settings.getAudioDeviceIndex()),
					(Device)video.getStream(Settings.getVideoDeviceIndex()),
					Settings.getLocation());
			
			boolean simStarted = sim.start();
			
			if(simStarted) {
				Settings.setSim(sim);
				Settings.setIsRunning(true);
				startStopSimButton.setText("Stop Simulation");
			}
		}
	}
	
	
	/**
	 * initialize audio device choice box and add items
	 */
	private void initializeAudioBox() {
		if (audioInputDevice != null) {
			ObservableList<String> list = FXCollections.observableArrayList();
			List<String> audioDevices = audio.getDeviceList();
			for (int i = 0; i < audioDevices.size(); i++) {
				list.add(audioDevices.get(i));
			}
			audioInputDevice.setItems(list);

			audioInputDevice.getSelectionModel().clearAndSelect(Settings.getAudioDeviceIndex());

			audioInputDevice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					Settings.setAudioDeviceIndex(audioInputDevice.getSelectionModel().getSelectedIndex());

				}
			});
		}
	}
	
	
	/**
	 * initialize audio device choice box and add items
	 */
	private void initializeVideoBox() {
		if (videoInputDevice != null) {
			ObservableList<String> list = FXCollections.observableArrayList();
			List<String> videoDevices = video.getDeviceList();
			for (int i = 0; i < videoDevices.size(); i++) {
				list.add(videoDevices.get(i));
			}
			videoInputDevice.setItems(list);

			videoInputDevice.getSelectionModel().clearAndSelect(Settings.getVideoDeviceIndex());

			videoInputDevice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					Settings.setVideoDeviceIndex(videoInputDevice.getSelectionModel().getSelectedIndex());

				}
			});
		}
	}
	
	
	/**
	 * Set the choicebox initialy to this index
	 * @param choicebox
	 * @param index
	 * @throws IllegalArgumentException if index is negative.
	 */
	private void setInitialIndex(ChoiceBox<?> choicebox, int index) {
		if(index<0) {
			throw new IllegalArgumentException("Invalid Index.");
		}
		choicebox.getSelectionModel().clearAndSelect(index);
	}
	
	/**
	 * initializes Items. Called if a new fxml file is loaded.
	 */
	public void initialize() {
		initializeAudioBox();
		initializeVideoBox();
		
		//INITIALIZE SAMPLERATE CHOICEBOX
		if(sampleRateChoice != null) {
			sampleRateChoice.setItems(FXCollections.observableArrayList(
				    "44100 kHz", "48000 kHz", "96000 kHz"));
			setInitialIndex(sampleRateChoice,Settings.getSamplerateIndex());
			
			sampleRateChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					Settings.setSamplerateIndex(sampleRateChoice.getSelectionModel().getSelectedIndex());

				}
			});
		}
		
		//INITIALIZE VIDEO RESOLUTION CHOICEBOX
		if(videoResolutionChoice != null) {
			videoResolutionChoice.setItems(FXCollections.observableArrayList(
				    "640x480", "1080x720", "1920x1080"));
			setInitialIndex(videoResolutionChoice,Settings.getVideoResolutionIndex());
			
			videoResolutionChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					Settings.setVideoResolutionIndex(videoResolutionChoice.getSelectionModel().getSelectedIndex());

				}
			});
		}
		
		//INITIALIZE QUALITY CHOICEBOX
		if(qualityChoice != null) {
			qualityChoice.setItems(FXCollections.observableArrayList(
				    "High", "Medium", "Low"));
			setInitialIndex(qualityChoice,Settings.getQualityIndex());
			
			qualityChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					Settings.setQualityIndex(qualityChoice.getSelectionModel().getSelectedIndex());

				}
			});
		}

	}

}
