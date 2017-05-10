package com.jeeves.vpl.survey.questions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jeeves.vpl.firebase.FirebaseQuestion;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import static com.jeeves.vpl.survey.QuestionEditor.*;
import static com.jeeves.vpl.Constants.*;

public class QuestionMultMany extends QuestionView{

//	public QuestionView clone(){
//		return new QuestionMultMany(super.getModel());
//	}
//	
	public void loadOptions(){
		FXMLLoader surveyLoader = new FXMLLoader();
		surveyLoader.setController(this);
		surveyLoader.setLocation(getClass().getResource("/OptionsMultiChoiceMany.fxml"));
		 try {
			 optionsPane = (Pane) surveyLoader.load();
			 addEventHandlers();
		 }
		 catch(IOException e){
			 System.out.println("Hello?");
		 }
	}
	public String getImagePath(){
		return "/img/icons/imgmany.png";
	}
//	public QuestionMultMany(FirebaseQuestion model) {
//		super(model);
//		setImage("/img/icons/imgmany.png");
//	//	setQuestionText("Select Many");
//		//this.description = "User can select multiple answers from a list of options";
//		}
	public String getLabel(){
		return "Select multiple options from a list";
	}
	@FXML private Pane paneMultChoiceM;
	@FXML private VBox paneChoiceOptsM;
	@FXML private Button btnAddOptM;
	@FXML private ScrollPane paneOptionsM;
	
	
	/**
	 * Add an option to a multiple choice question
	 * @param s The option text
	 */
	public void handleAddOpt(VBox choices, String s) { // NO_UCD (unused code)
		HBox optionBox = new HBox();
		optionBox.setSpacing(2);
		TextField choice = new TextField();
		choice.setText(s);
		Button remove = new Button();
		remove.setText("X");
		remove.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				choices.getChildren().remove(optionBox);
				Map<String,Object> qOptions = new HashMap<String,Object>();
				int optcount = 1;
				for(Node opt : choices.getChildren()){
					HBox optbox = (HBox)opt;
					TextField opttext = (TextField)optbox.getChildren().get(0);
						qOptions.put("option"+Integer.toString(optcount++),opttext.getText());
						model.setOptions(qOptions);
				}
			}
		});		

		optionBox.getChildren().addAll(choice,remove);
		choices.getChildren().add(optionBox);
		choice.setOnKeyReleased((event)->{
			Map<String,Object> qOptions = new HashMap<String,Object>();
			int optcount = 1;
			for(Node opt : choices.getChildren()){
				HBox optbox = (HBox)opt;
				TextField opttext = (TextField)optbox.getChildren().get(0);
					qOptions.put("option"+Integer.toString(optcount++),opttext.getText());
					model.setOptions(qOptions);
			}
		});
	}

	public void addEventHandlers() {
		// TODO Auto-generated method stub
		btnAddOptM.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				handleAddOpt(paneChoiceOptsM,"");//Add a blank options
			}
		});
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public void showCheckQOpts() {
//		cboMultiChoice.setVisible(true);
//		Map<String,Object> opts = (Map<String,Object>)model.getparams().get("options");
//		cboMultiChoice.getItems().clear();
//		if(opts != null)
//			opts.values().forEach(mval ->{cboMultiChoice.getItems().add(mval.toString());});	
//		cboMultiChoice.getSelectionModel().clearSelection();
//
//		
//	}
//	@Override
//	public void handleCheckQ(String scon) {
//		// TODO Auto-generated method stub
//
//			
//		if(!scon.isEmpty())
//			cboMultiChoice.getSelectionModel().select(scon);
//		else
//			cboMultiChoice.getSelectionModel().clearSelection();
//
//
//	}

	@Override
	public void showEditOpts(Map<String,Object> opts) {
		paneChoiceOptsM.getChildren().clear();
		if(opts == null)return;
		for (Object opt : opts.values()){
			handleAddOpt(paneChoiceOptsM,opt.toString());
		}
	}
	@Override
	public int getQuestionType() {
		return MULT_MANY;
	}

}