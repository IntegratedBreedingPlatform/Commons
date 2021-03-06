
package org.generationcp.commons.ruleengine.impl;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.commons.ruleengine.Rule;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleExecutionContext;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class RulesServiceImpl implements RulesService {

	@Resource
	private RuleFactory ruleFactory;
	
	public static final Logger LOG = LoggerFactory.getLogger(RulesServiceImpl.class);

	public RulesServiceImpl() {
	}

	// FIXME : catch RuleExceptions here?
	@Override
	public Object runRules(RuleExecutionContext context) throws RuleException {
		
		Monitor monitor = MonitorFactory.start("AdvanceNursery:org.generationcp.commons.ruleengine.impl.RulesServiceImpl.runRules");
		
		try{
		
  		List<String> sequenceOrder = context.getExecutionOrder();
  
  		assert !sequenceOrder.isEmpty();
  		Rule rule = this.ruleFactory.getRule(sequenceOrder.get(0));
  
  		while (rule != null) {
  			rule.runRule(context);
  			rule = this.ruleFactory.getRule(rule.getNextRuleStepKey(context));
  		}
		} finally {
		  monitor.stop();
		}

		return context.getRuleExecutionOutput();

	}

	public void setRuleFactory(RuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}
}
