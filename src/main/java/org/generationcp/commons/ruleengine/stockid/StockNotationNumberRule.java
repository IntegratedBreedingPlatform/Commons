
package org.generationcp.commons.ruleengine.stockid;

import org.generationcp.commons.ruleengine.OrderedRule;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Component;

/**
 * A rule implementation that defines the logic for processing stock notation within the context of generation of stock IDs
 */
@Component
public class StockNotationNumberRule extends OrderedRule<StockIDGenerationRuleExecutionContext> {

	static final String KEY = "NOTATION";

	@Override
	public Object runRule(final StockIDGenerationRuleExecutionContext context) throws RuleException {

		try {
			final Integer notationNumber =
					context.getLotService().getCurrentNotationNumberForBreederIdentifier(context.getBreederIdentifier()) + 1;
			context.setNotationNumber(notationNumber);
		} catch (MiddlewareQueryException e) {
			throw new RuleException(e.getMessage(), e);
		}

		final Integer currentNotationNumber = context.getNotationNumber();
		context.getStockIDGenerationBuilder().append(currentNotationNumber);

		return currentNotationNumber;
	}

	@Override
	public String getKey() {
		return StockNotationNumberRule.KEY;
	}
}
