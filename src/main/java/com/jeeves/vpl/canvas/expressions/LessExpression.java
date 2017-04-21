package com.jeeves.vpl.canvas.expressions;

import com.jeeves.vpl.canvas.receivers.ExpressionReceiver;

import static com.jeeves.vpl.Constants.*;

public class LessExpression extends Expression  { // NO_UCD (unused code)
	public static final String NAME = "is this less than that?";
	public static final String DESC = "returns true if the left number is less than the right number";
	@Override
	public void setup() {
		name = NAME;
		description = DESC;
		this.varType = VAR_BOOLEAN;
		operand.setText("is less than");
		receivers.add(new ExpressionReceiver(VAR_NUMERIC));
		receivers.add(new ExpressionReceiver(VAR_NUMERIC));

	}
}