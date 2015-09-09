
package org.generationcp.commons.ruleengine.stockid;

import java.util.List;

import org.generationcp.commons.ruleengine.OrderedRuleExecutionContext;
import org.generationcp.middleware.service.api.InventoryService;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class StockIDGenerationRuleExecutionContext extends OrderedRuleExecutionContext {

	private InventoryService inventoryService;
	private StringBuilder stockIDGenerationBuilder;
	private String breederIdentifier;
	private Integer notationNumber;
	private String separator;
	private Long sequenceNumber;

	public StockIDGenerationRuleExecutionContext(List<String> executionOrder) {
		this(executionOrder, null);
	}

	public StockIDGenerationRuleExecutionContext(List<String> executionOrder, InventoryService inventoryService) {
		super(executionOrder);
		this.inventoryService = inventoryService;
		this.stockIDGenerationBuilder = new StringBuilder();
	}

	@Override
	public Object getRuleExecutionOutput() {
		return this.stockIDGenerationBuilder.toString();
	}

	public StringBuilder getStockIDGenerationBuilder() {
		return this.stockIDGenerationBuilder;
	}

	public void setStockIDGenerationBuilder(StringBuilder stockIDGenerationBuilder) {
		this.stockIDGenerationBuilder = stockIDGenerationBuilder;
	}

	public String getBreederIdentifier() {
		return this.breederIdentifier;
	}

	public void setBreederIdentifier(String breederIdentifier) {
		this.breederIdentifier = breederIdentifier;
	}

	public Integer getNotationNumber() {
		return this.notationNumber;
	}

	public void setNotationNumber(Integer notationNumber) {
		this.notationNumber = notationNumber;
	}

	public String getSeparator() {
		return this.separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public Long getSequenceNumber() {
		return this.sequenceNumber;
	}

	public void setSequenceNumber(Long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public InventoryService getInventoryService() {
		return this.inventoryService;
	}
}
