
package org.generationcp.commons.service.impl;

import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.commons.ruleengine.stockid.StockIDGenerationRuleExecutionContext;
import org.generationcp.commons.service.StockService;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used for providing stock-related functionalities such as assigning stock IDs, processing bulk settings and bulking
 * duplicates and reciprocals
 */
@Transactional
public class StockServiceImpl implements StockService {

	private static final Logger LOG = LoggerFactory.getLogger(StockServiceImpl.class);

	@Resource
	private RulesService rulesService;

	@Resource
	private RuleFactory ruleFactory;

	@Resource
	private LotService lotService;

	/**
	 * Returns the stock ID prefix (consisting of breeder identifier plus current notation number) and the configured separator e.g., 'DV1-'
	 *
	 * @param breederIdentifier
	 * @param separator
	 * @return
	 */
	@Override
	public String calculateNextStockIDPrefix(final String breederIdentifier, final String separator) {
		List<String> sequenceList = Arrays.asList(this.ruleFactory.getRuleSequenceForNamespace("stockid"));
		StockIDGenerationRuleExecutionContext context = new StockIDGenerationRuleExecutionContext(sequenceList, this.lotService);
		context.setBreederIdentifier(breederIdentifier);
		context.setSeparator(separator);

		try {
			this.rulesService.runRules(context);
			return (String) context.getRuleExecutionOutput();

		} catch (RuleException e) {
			throw new MiddlewareException(e.getMessage(), e);
		}
	}

}
