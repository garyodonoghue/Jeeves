package com.jeeves.vpl.survey.questions;

import static com.jeeves.vpl.Constants.DATE;

import java.io.IOException;
import java.util.Map;

import com.jeeves.vpl.firebase.FirebaseQuestion;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;


public class QuestionDate extends QuestionView {

	@FXML
	private HBox hboxOptions;
	@FXML
	private RadioButton rdioAny;
	@FXML
	private RadioButton rdioFuture;
	@FXML
	private RadioButton rdioPast;
	private ToggleGroup tgroup;
	@FXML
	private VBox vboxDateOpts;

	public QuestionDate(String label)  throws Exception {
		this(new FirebaseQuestion(label));
	}

	public QuestionDate(FirebaseQuestion data) {
		super(data);
	}

	@Override
	public void addEventHandlers() {
		tgroup.selectedToggleProperty().addListener((x, y, z) -> {
			model.getparams().put("dateConstraint", tgroup.getSelectedToggle().getUserData());
		});

	}

	@Override
	public String getImagePath() {
		return "/img/icons/imgdate.png";
	}

	@Override
	public String getQuestionType() {
		return DATE;
	}

	@Override
	public void loadOptions() {
		tgroup = new ToggleGroup();
		FXMLLoader surveyLoader = new FXMLLoader();
		surveyLoader.setController(this);
		surveyLoader.setLocation(getClass().getResource("/OptionsDateTime.fxml"));
		try {
			optionsPane = (Pane) surveyLoader.load();

			addEventHandlers();
		} catch (IOException e) {
		}
		
		rdioAny.setToggleGroup(tgroup);
		rdioAny.setUserData(new Long(0));
		rdioFuture.setToggleGroup(tgroup);
		rdioFuture.setUserData(new Long(1));
		rdioPast.setToggleGroup(tgroup);
		rdioPast.setUserData(new Long(2));
	}

	@Override
	public void showEditOpts(Map<String, Object> opts) {
		if (model.getparams().containsKey("dateConstraint"))
			switch (((Long)model.getparams().get("dateConstraint")).intValue()) {
			case 0:
				tgroup.selectToggle(rdioAny);
				break;
			case 1:
				tgroup.selectToggle(rdioFuture);
				break;
			case 2:
				tgroup.selectToggle(rdioPast);
				break;
			}
	}

}
