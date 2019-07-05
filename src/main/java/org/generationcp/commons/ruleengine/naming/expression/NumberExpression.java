
package org.generationcp.commons.ruleengine.naming.expression;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import org.generationcp.commons.pojo.AdvancingSource;

@Component
public class NumberExpression extends BaseExpression implements Expression {

	public static final String KEY = "[NUMBER]";

	public NumberExpression() {

	}

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source, final String capturedText) {
		if (source.isForceUniqueNameGeneration()) {
			for (StringBuilder container : values) {
				this.replaceExpressionWithValue(container, "(" + (source.getCurrentMaxSequence() + 1) + ")");

			}

			return;
		}

		if (source.isBulk()) {
			for (StringBuilder container : values) {
				if (source.getPlantsSelected() != null && source.getPlantsSelected() > 1) {
					Integer newValue = source.getPlantsSelected();
					this.replaceExpressionWithValue(container, newValue != null ? newValue.toString() : "");
				} else {
					this.replaceExpressionWithValue(container, "");
				}
			}
		} else {
			List<StringBuilder> newNames = new ArrayList<StringBuilder>();
			int startCount = 1;

			if (source.getCurrentMaxSequence() > -1) {
				startCount = source.getCurrentMaxSequence() + 1;
			}

			for (StringBuilder value : values) {
				if (source.getPlantsSelected() != null && source.getPlantsSelected() > 0) {

					for (int i = startCount; i < startCount + source.getPlantsSelected(); i++) {
						StringBuilder newName = new StringBuilder(value);
						this.replaceExpressionWithValue(newName, String.valueOf(i));
						newNames.add(newName);
					}
				} else {
					this.replaceExpressionWithValue(value, "");
					newNames.add(value);
				}
			}

			values.clear();
			values.addAll(newNames);
		}
	}

	@Override
	public String getExpressionKey() {
		return NumberExpression.KEY;
	}

}
