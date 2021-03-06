package org.generationcp.commons.ruleengine.coding;

import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.coding.expression.CodingExpressionResolver;
import org.generationcp.middleware.pojos.naming.NamingConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SuffixRuleTest {

	@Mock
	private CodingExpressionResolver codingExpressionResolver;

	@InjectMocks
	private SuffixRule suffixRule;

	@Test
	public void testRunRule() throws RuleException {

		final String suffix = "XYZ";

		final List<String> sequenceOrder = new ArrayList<>();
		final NamingConfiguration namingConfiguration = new NamingConfiguration();

		namingConfiguration.setSuffix(suffix);
		final CodingRuleExecutionContext context = new CodingRuleExecutionContext(sequenceOrder, namingConfiguration);
		context.setCurrentData("");

		Mockito.when(codingExpressionResolver.resolve(context.getCurrentData(), namingConfiguration.getSuffix(), namingConfiguration))
				.thenReturn(Arrays.asList(suffix));

		assertEquals(suffix, this.suffixRule.runRule(context));
	}

}
