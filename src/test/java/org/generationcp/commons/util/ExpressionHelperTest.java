
package org.generationcp.commons.util;

import org.generationcp.middleware.ruleengine.util.ExpressionHelperCallback;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 6/18/2014 Time: 10:28 AM
 */
public class ExpressionHelperTest {

	@Test
	public void testExpressionHelper() {
		String input = "[Season]*-[AnotherClass]";

		ExpressionHelper.evaluateExpression(input, "\\[([^\\]]*)]", new ExpressionHelperCallback() {

			@Override
			public void evaluateCapturedExpression(String capturedText, String originalInput, int start, int end) {
				System.out.println(capturedText);
			}
		});
	}
}
