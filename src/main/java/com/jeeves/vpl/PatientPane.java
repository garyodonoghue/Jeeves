package com.jeeves.vpl;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.prefs.Preferences;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.jeeves.vpl.firebase.FirebaseDB;
import com.jeeves.vpl.firebase.FirebasePatient;
import com.jeeves.vpl.firebase.FirebaseProject;
import com.jeeves.vpl.firebase.FirebaseQuestion;
import com.jeeves.vpl.firebase.FirebaseSurvey;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

public class PatientPane extends Pane {

	private ObservableList<FirebasePatient> allowedPatients = FXCollections.observableArrayList();
	private Map<String, FirebaseSurvey> completedSurveys;
	private FirebaseDB firebase;

	private Main gui;
	private Map<String, FirebaseSurvey> incompleteSurveys;
	@FXML private Label lblPatientCompleted;
	@FXML private Label lblPatientMissed;
	@FXML private ListView<String> lstMessages;
	@FXML private TableView<FirebasePatient> tblPatients;
	@FXML private TextField txtEmail;
	@FXML private TextField txtName;
	@FXML private TextField txtPhone;
	private ChangeListener<FirebasePatient> patientListener;
	private ChangeListener<FirebaseSurvey> surveyListener;
	private int selectedIndex = 0;
	private FirebasePatient selectedPatient;

	@FXML private ListView<String> lstSurveys;
	@FXML private Label lblSent;
	@FXML private Label lblComplete;
	@FXML private Label lblMissed;
	@FXML private Label lblCompliance;
	@FXML private Label lblTimeTriggers;
	@FXML private Label lblSensorTriggers;
	@FXML private Label lblButtonTriggers;
	@FXML private Label lblInitTime;
	@FXML private Label lblCompletionTime;
	
	@FXML private RadioButton rdioCsv;
	@FXML private RadioButton rdioExcel;
	

	private PrivateKey privateKey;

	public PrivateKey getPrivate(String keystr) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(keystr);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	public Cipher cipher;

	public String decryptText(String msg, PrivateKey key)
			throws InvalidKeyException, UnsupportedEncodingException,
			IllegalBlockSizeException, BadPaddingException {
		this.cipher.init(Cipher.DECRYPT_MODE, key);
		return new String(cipher.doFinal(Base64.decodeBase64(msg)), "UTF-8");
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public PatientPane(Main gui, FirebaseDB firebase) {
		this.firebase = firebase;
		this.gui = gui;
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setController(this);
		patientListener = new ChangeListener<FirebasePatient>() {
			@Override
			public void changed(ObservableValue<? extends FirebasePatient> observable, FirebasePatient oldValue,
					FirebasePatient newValue) {
				update();
			}
		};
		surveyListener = new ChangeListener<FirebaseSurvey>() {
			@Override
			public void changed(ObservableValue<? extends FirebaseSurvey> observable, FirebaseSurvey oldValue,
					FirebaseSurvey newValue) {
				update();
			}
		};
		try {
			this.cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		URL location = this.getClass().getResource("/PatientPane.fxml");
		TableColumn nameCol = new TableColumn("Name");
		nameCol.setCellValueFactory(
				new Callback<CellDataFeatures<FirebasePatient, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<FirebasePatient, String> p) {
						return new ReadOnlyObjectWrapper(p.getValue().getName());
					}
				});
		fxmlLoader.setLocation(location);
		try {
			Node root = (Node) fxmlLoader.load();
			getChildren().add(root);
			tblPatients.getColumns().clear();
			tblPatients.getColumns().addAll(nameCol);
			tblPatients.setPlaceholder(new Label("No patients currently assigned to this study"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void downloadSurvey(Event e){
		
	}
	@FXML
	public void downloadPatient(Event e) {
		int answerlength = 0;
		boolean newsheet = false;
		Date date = new Date();
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialFileName("results.xls");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel spreadsheet(*.xls)", "*.xls"));

			fileChooser.setTitle("Save Image");
			Workbook wb = new HSSFWorkbook();
			Row r = null;
			Cell c = null;

			File file = fileChooser.showSaveDialog(getScene().getWindow());

			if (file != null) {
				if (!file.getName().contains(".")) {
					file = new File(file.getAbsolutePath() + ".xls");
				}
				try {
					if (completedSurveys.isEmpty()) {
						wb.close();
						return;
					}
					FileOutputStream fileOut = new FileOutputStream(file);
					HashMap<String, Sheet> sheets = new HashMap<String, Sheet>();
					//		HashMap<String, String> surveyids = new HashMap<String,String>();
					String lastSurveyId = "";
					// Is this horrendously convoluted? Perhaps. Hopefully it
					// won't slow things down
					Collection<FirebaseSurvey> surveys = completedSurveys.values();
					ArrayList<FirebaseSurvey> surveylist = new ArrayList<FirebaseSurvey>(surveys);
					surveylist.sort(new Comparator<FirebaseSurvey>() {

						@Override
						public int compare(FirebaseSurvey o1, FirebaseSurvey o2) {
							return (int) (o1.gettimeFinished() - o2.gettimeFinished());
						}
					});

					Sheet s = null;

					CreationHelper createHelper = wb.getCreationHelper();
					CellStyle cellStyle = wb.createCellStyle();
					CellStyle style = wb.createCellStyle();
					Font font = wb.createFont();
					font.setFontName(HSSFFont.FONT_ARIAL);
					font.setFontHeightInPoints((short)10);
					font.setBold(true);
					style.setFont(font);
					cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));
					cellStyle.setFont(font);
					Preferences prefs = Preferences.userRoot().node("key");
					String privateKeyStr = prefs.get("privateKey", "");
					PrivateKey privateKey = getPrivate(privateKeyStr);
					System.out.println("privkeystr is " + privateKeyStr);
					for (FirebaseSurvey nextsurvey : surveylist) {
						System.out.println("our encoded answers are...");
						System.out.println(nextsurvey.getencodedAnswers());
						System.out.println("decrypted answers are...");
						System.out.println(decryptText(nextsurvey.getencodedAnswers(), privateKey));
						date.setTime(nextsurvey.gettimeFinished());
						String surveyname = nextsurvey.gettitle();
						// Do we have a sheet for this particular survey?
						s = sheets.get(surveyname);
						if (s == null) {
							newsheet = true;
							s = wb.createSheet();
							sheets.put(surveyname, s);
							lastSurveyId =  nextsurvey.getsurveyId();
							wb.setSheetName(wb.getSheetIndex(s), surveyname);
							r = s.createRow(0);
							int count = 1;
							c = r.createCell(0);
							c.setCellStyle(style);
							c.setCellValue("Completed");
							for(FirebaseQuestion q : nextsurvey.getquestions()){
								answerlength++;
								c = r.createCell(count);
								count++;
								c.setCellValue(q.getquestionText());
								c.setCellStyle(style);
							}
						}
						//The Survey ID has changed, that means our questions have changed!
						//Need to make a new line detailing the questions
						int newlength = 0;
						if(!nextsurvey.getsurveyId().equals(lastSurveyId)){
							lastSurveyId = nextsurvey.getsurveyId();
							r = s.createRow(s.getLastRowNum() + 1);
							int count =1;
							c = r.createCell(0);
							c.setCellStyle(style);

							c.setCellValue("Completed");
							for(FirebaseQuestion q : nextsurvey.getquestions()){
								newlength++;
								c = r.createCell(count);
								count++;
								c.setCellValue(q.getquestionText());
								c.setCellStyle(style);

							}
							if(newlength > answerlength)
								answerlength = newlength;
						}

						r = s.createRow(s.getLastRowNum() + 1);
						c = r.createCell(0);
						c.setCellStyle(cellStyle);

						c.setCellValue(date);
						//	s.autoSizeColumn(0);

						List<String> answers = nextsurvey.getanswers();
						int answercounter = 1;
						for (String answer : answers) {

							c = r.createCell(answercounter);
							answercounter++;

							c.setCellValue(answer);
						}
						if(newsheet){
							s.setColumnWidth(0, 20*256);
							for (int i = 1; i < answerlength; i++){
								s.autoSizeColumn(i);
							}
							newsheet = false;
						}
					}
					//	s.autoSizeColumn(0);

					wb.write(fileOut);
					fileOut.close();
					Desktop.getDesktop().open(file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				wb.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public void loadSurveys(){
//		FirebaseProject proj = gui.getCurrentProject();
//		if (proj == null)
//			return;
//		String name = proj.getname();
		gui.registerSurveyListener(new ListChangeListener<FirebaseSurvey>(){

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends FirebaseSurvey> c) {
				c.next();
				if(c.wasAdded()){
					FirebaseSurvey added = c.getAddedSubList().get(0);
					lstSurveys.getItems().add(added.gettitle());
				//	addToTable(added);
				}
				else{
					FirebaseSurvey removed = c.getRemoved().get(0);
					lstSurveys.getItems().remove(removed.gettitle());
				//	removeFromTable(removed);
				}				
			}
			
		});
	}
	public void loadPatients() {
		FirebaseProject proj = gui.getCurrentProject();
		if (proj == null)
			return;
		String name = proj.getname();
		firebase.getpatients().addListener(new ListChangeListener<FirebasePatient>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends FirebasePatient> c) {
				System.out.println("AGAIN!?!?");
				c.next();
				if(c.wasAdded()){
					FirebasePatient added = c.getAddedSubList().get(0);
					addToTable(added);
				}
				else{
					FirebasePatient removed = c.getRemoved().get(0);
					removeFromTable(removed);
				}
				loadPatientTable(name);

			}
		});
		loadPatientTable(name);
		// back to it k?
		update();
	}

	private void addToTable(FirebasePatient patient){
		String personalInfo = "";
		try {
			personalInfo = decryptText(patient.getuserinfo(),privateKey);
		} catch (InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		decryptInfo(patient,personalInfo);
		tblPatients.getItems().add(patient);
	}
	private void removeFromTable(FirebasePatient patient){
		tblPatients.getItems().remove(patient);
	}
	
	private void decryptInfo(FirebasePatient patient, String personalInfo){
		String[] infoBits = personalInfo.split(";");
		String name = infoBits[0];
		String email = infoBits[1];
		String phone = infoBits[2];
		patient.setName(name);
		patient.setEmail(email);
		patient.setPhoneNo(phone);
	}
	private void loadPatientTable(String name) {
		Preferences prefs = Preferences.userRoot().node("key");
		String privateKeyStr = prefs.get("privateKey", "");
		try {
			privateKey = getPrivate(privateKeyStr);
			System.out.println("privkey is " + privateKey);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("HOW MAY TIMES");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				//loadPatientTable();
				allowedPatients.clear();
				firebase.getpatients().forEach(patient -> {
					if (patient.getCurrentStudy() != null && patient.getCurrentStudy().equals(name))
						allowedPatients.add(patient);
					System.out.println("Patient's encrypted info is " + patient.getuserinfo());
					try {
				//		System.out.println("And their decrypted info is " + 
					//String oldinfo = new String(patient.getuserinfo());
						String personalInfo = decryptText(patient.getuserinfo(),privateKey);
						decryptInfo(patient,personalInfo);
					} catch (InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException
							| BadPaddingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}); 

				System.out.println("We have " + allowedPatients.size() + " allowed patientes");
				//	tblPatients.getItems().clear();
				tblPatients.setItems(allowedPatients); // This is hacky but I'll get

				update();

			}
		});
		tblPatients.getSelectionModel().selectedItemProperty().addListener(patientListener);
	}

	private void update() {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				TableViewSelectionModel<FirebasePatient> selectionModel = tblPatients.getSelectionModel();
				if (selectionModel.getSelectedItem() != null) {
					selectedPatient = selectionModel.getSelectedItem();
					selectedIndex = selectionModel.getSelectedIndex();
				} else {
					tblPatients.getSelectionModel().selectedItemProperty().removeListener(patientListener);
					tblPatients.getSelectionModel().clearAndSelect(selectedIndex);
					selectedPatient = selectionModel.getSelectedItem();
					tblPatients.getSelectionModel().selectedItemProperty().addListener(patientListener);
				}
				lstMessages.getItems().clear();
				if (selectedPatient == null)
					return;

				Map<String, Object> feedback = selectedPatient.getfeedback();
				Date date = new Date();
				DateFormat df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z");
				df.setTimeZone(TimeZone.getTimeZone("GMT"));

				if (feedback != null) {
					Iterator<Entry<String, Object>> feeds = feedback.entrySet().iterator();
					ObservableList<String> items = FXCollections.observableArrayList();
					while (feeds.hasNext()) {
						Entry<String, Object> feed = feeds.next();
						date.setTime(Long.parseLong(feed.getKey()));
						String message = df.format(date) + ":    " + feed.getValue();
						items.add(message);
					}
					lstMessages.setItems(items);
				}

				incompleteSurveys = selectedPatient.getincomplete();
				completedSurveys = selectedPatient.getcomplete();
				if (incompleteSurveys == null || incompleteSurveys.isEmpty())
					lblPatientMissed.setText("0");

				else {
					int incomplete = 0;
					Iterator<FirebaseSurvey> incompleteIter = incompleteSurveys.values().iterator();
					while (incompleteIter.hasNext()) {
						incomplete++;
						incompleteIter.next(); //You bloody idiot
					}
					lblPatientMissed.setText(Integer.toString(incomplete));
				}

				if (completedSurveys == null || completedSurveys.isEmpty())
					lblPatientCompleted.setText("0");
				else
					lblPatientCompleted.setText(Integer.toString(completedSurveys.size()));

				txtName.setText(selectedPatient.getName());
				txtEmail.setText(selectedPatient.getEmail());
				txtPhone.setText(selectedPatient.getPhoneNo());
			}
		});
	}


}
