package com.jeeves.vpl.canvas.triggers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import com.jeeves.vpl.firebase.FirebaseSurvey;
import com.jeeves.vpl.firebase.FirebaseTrigger;

/**
 * This class represents a clock trigger that can be dragged around on the
 * canvas and have its fields edited
 *
 * @author Daniel
 */
public class SurveyTrigger extends Trigger { // NO_UCD (use default)
	public static final String NAME = "On survey completed/ignored";
	public static final String DESC = "Schedule actions to take place when a user has completed or ignored a survey a certain number of times";
	@FXML private ComboBox<String> cboSurvey;
	@FXML private TextField txtMissedTimes;
	@FXML private ComboBox<String> cboCompMissed;
	private ChangeListener<String> listener;
	public Node[] getWidgets(){
		return new Node[]{cboSurvey,cboCompMissed,txtMissedTimes};
	}

//	public SurveyTrigger() {
//		this(new FirebaseTrigger());
//	}
//	
	public void fxmlInit(){
		super.fxmlInit();
		name = NAME;
		description = DESC;
		cboCompMissed.getItems().clear();
		cboCompMissed.getItems().addAll("completed","missed");
		cboCompMissed.setValue("completed");
	}
	
	@Override
	public void addListeners(){
		super.addListeners();
		ObservableList<FirebaseSurvey> surveys = gui.getSurveys();
		cboSurvey.getItems().clear();
		surveys.forEach(survey->{cboSurvey.getItems().add(survey.getname());});
		//changeSurveys();
		gui.registerSurveyListener(new ListChangeListener<FirebaseSurvey>(){
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends FirebaseSurvey> change) {  
				String value = cboSurvey.getValue();
				cboSurvey.getItems().clear();
				surveys.forEach(survey->{
					if(getInstance().isReadOnly)return;
				cboSurvey.getItems().add(survey.getname());
				if(survey.getname().equals(value))cboSurvey.setValue(value);
				survey.name.addListener(new ChangeListener<String>(){

					@Override
					public void changed(ObservableValue<? extends String> arg0,
							String arg1, String arg2) {
						int index = cboSurvey.getSelectionModel().getSelectedIndex();
						cboSurvey.getItems().clear();
						surveys.forEach(survey2->{cboSurvey.getItems().add(survey2.getname());});
						
						if(index >=0)
							cboSurvey.setValue(surveys.get(index).getname());
						
					}
					
				});});
			}

		});

		listener = new ChangeListener<String>(){

			@Override
			public void changed(ObservableValue<? extends String> arg0,
					String arg1, String arg2) {
				if(arg2 != null) //aaaaaaaargh
					params.put("selectedSurvey", cboSurvey.getValue());
				
			}
			
		};
		cboSurvey.valueProperty().addListener(listener);
		cboCompMissed.valueProperty().addListener(selected->params.put("result",cboCompMissed.getValue()));
		txtMissedTimes.addEventHandler(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent arg0) {
				try{
					Long.parseLong(arg0.getCharacter());
				}
				catch(NumberFormatException e){
					arg0.consume();
					return;
				}	
			}
		});
		txtMissedTimes.addEventHandler(KeyEvent.KEY_RELEASED,new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent arg0) {
				params.put("missed", txtMissedTimes.getText());				
			}
		});
	}

//	public SurveyTrigger(FirebaseTrigger model) {
//		super(model);
//		if(cboSurvey.getItems()!= null && cboSurvey.getItems().size()>0)
//			cboSurvey.setValue(cboSurvey.getItems().get(0));
//		addListeners();
//		
//	}

	@Override
	public void setData(FirebaseTrigger model){
		super.setData(model);
	//	Map<String,Object> params = model.getparams();
		if(params.containsKey("selectedSurvey") && params.get("selectedSurvey") != null){ //Bah this is dodgy
			cboSurvey.setValue(params.get("selectedSurvey").toString());
		}
		
		String completed = params.containsKey("result") ? params.get("result").toString() : "completed";
		
		cboCompMissed.setValue(completed);
		txtMissedTimes.setText(params.containsKey("missed") ? params.get("missed").toString() : "");
		
	}
	@Override
	public String getViewPath() {
		return String.format("/TriggerSurvey.fxml", this.getClass().getSimpleName());
	}
}