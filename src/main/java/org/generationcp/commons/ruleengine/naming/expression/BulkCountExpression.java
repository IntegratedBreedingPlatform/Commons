
package org.generationcp.commons.ruleengine.naming.expression;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.util.ExpressionHelper;
import org.generationcp.commons.util.ExpressionHelperCallback;
import org.springframework.stereotype.Component;

import org.generationcp.commons.pojo.AdvancingSource;

@Component
public class BulkCountExpression extends BaseExpression {

	public static final String KEY = "[BCOUNT]";

	public BulkCountExpression() {
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source, final String capturedText) {
		for (StringBuilder container : values) {
            String computedValue;
			if (source.getRootName() != null) {
				BulkExpressionHelperCallback callback = new BulkExpressionHelperCallback();
				ExpressionHelper.evaluateExpression(source.getRootName(), "-([0-9]*)B", callback);

				StringBuilder lastBulkCount = callback.getLastBulkCount();

				if (lastBulkCount.length() > 0) {
					computedValue = (Integer.valueOf(lastBulkCount.toString()) + 1) + "B";
				} else {
					computedValue = "-B";
				}
			} else {
                computedValue = "-B";
			}

            this.replaceExpressionWithValue(container, computedValue);
		}
	}

	private class BulkExpressionHelperCallback implements ExpressionHelperCallback {

		final StringBuilder lastBulkCount = new StringBuilder();

		@Override
		public void evaluateCapturedExpression(String capturedText, String originalInput, int start, int end) {
			if ("-B".equals(capturedText)) {
				this.lastBulkCount.replace(0, this.lastBulkCount.length(), "1");
			} else {
				String newCapturedText = capturedText.replaceAll("[-B]*", "");
				if (newCapturedText != null && NumberUtils.isNumber(newCapturedText)) {
					this.lastBulkCount.replace(0, this.lastBulkCount.length(), newCapturedText);
				}
			}
		}

		public StringBuilder getLastBulkCount() {
			return this.lastBulkCount;
		}
	}

	@Override
	public String getExpressionKey() {
		return BulkCountExpression.KEY;
	}
}
