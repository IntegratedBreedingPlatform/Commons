
package org.generationcp.commons.ruleengine.naming.rules;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.generationcp.commons.ruleengine.Rule;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleExecutionContext;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Method;
import org.junit.Before;
import org.junit.Test;

import org.generationcp.commons.pojo.AdvancingSource;

public class CountRuleTest extends BaseNamingRuleTest {

	private Rule rule;
	private Method breedingMethod;
	private Integer breedingMethodSnameType;
	private String name;

	@Before
	public void setUp() {
		this.breedingMethodSnameType = 5;
		this.breedingMethod = new Method();
		this.breedingMethod.setSnametype(this.breedingMethodSnameType);
		this.breedingMethod.setSeparator("-");
		this.breedingMethod.setCount("");
		this.row = new AdvancingSource();
		this.row.setBreedingMethod(this.breedingMethod);
		this.rule = new CountRule();

		this.name = "TestGP";
	}

	@Test
	public void testNoCountMethodInRule() {
		this.breedingMethod.setCount(null);

		List<String> testCurrentInput = new ArrayList<>();
		testCurrentInput.add(this.name);

		RuleExecutionContext context = this.createExecutionContext(testCurrentInput);

		try {

			this.rule.runRule(context);
			List<String> output = (List<String>) context.getRuleExecutionOutput();
			Assert.assertEquals(testCurrentInput.size(), output.size());

			Assert.assertEquals("No changes should be made to current input if no count method is available", this.name, output.get(0));

		} catch (RuleException re) {
			Assert.fail("Rule failed to run for Count" + this.row.getBreedingMethod().getCount());
		}
	}

	@Test
	public void testNumberCount() {
		this.row.setPlantsSelected(3);
		this.setBulking(this.breedingMethod, true);

		List<String> input = new ArrayList<String>();

		input.add(this.name);
		RuleExecutionContext context = this.createExecutionContext(input);

		try {
			this.rule.runRule(context);
			input = (List<String>) context.getRuleExecutionOutput();
			Assert.assertEquals(1, input.size());
			Assert.assertEquals("Should return the correct countr", "TestGP", input.get(0));

		} catch (RuleException re) {
			Assert.fail("Rule failed to run for Count" + this.row.getBreedingMethod().getCount());
		}
	}

	@Test
	public void testNumberCountNoPlantsSelected() {
		this.row.setPlantsSelected(0);
		this.setBulking(this.breedingMethod, true);

		List<String> input = new ArrayList<String>();
		input.add(this.name);

		RuleExecutionContext context = this.createExecutionContext(input);

		try {
			this.rule.runRule(context);

			input = (List<String>) context.getRuleExecutionOutput();

			Assert.assertEquals(1, input.size());
			Assert.assertEquals("Should return the correct countr", "TestGP", input.get(0));
		} catch (RuleException re) {
			Assert.fail("Rule failed to run for Count" + this.row.getBreedingMethod().getCount());
		}

	}

	@Test
	public void testSequenceCountNoPlantsSelected() {
		this.row.setPlantsSelected(0);
		this.setBulking(this.breedingMethod, false);
		List<String> input = new ArrayList<String>();
		input.add(this.name);

		RuleExecutionContext context = this.createExecutionContext(input);
		try {
			input = (List<String>) this.rule.runRule(context);
			Assert.assertEquals(1, input.size());
			Assert.assertEquals("Should return the correct countr", "TestGP", input.get(0));
		} catch (RuleException re) {
			Assert.fail("Rule failed to run for Count" + this.row.getBreedingMethod().getCount());
		}

	}

	@Test
	public void testSequenceCount() {
		this.row.setPlantsSelected(3);
		this.row.setBreedingMethod(this.setBulking(this.breedingMethod, false));
		List<String> input = new ArrayList<String>();
		input.add(this.name);

		RuleExecutionContext context = this.createExecutionContext(input);
		try {
			input = (List<String>) this.rule.runRule(context);

			Assert.assertEquals(1, input.size());
			Assert.assertEquals("Should return the correct countr", "TestGP", input.get(0));
		} catch (RuleException re) {
			Assert.fail("Rule failed to run for Count" + this.row.getBreedingMethod().getCount());
		}

	}

	private Method setBulking(Method method, boolean isBulking) {

		if (isBulking) {
			method.setGeneq(TermId.BULKING_BREEDING_METHOD_CLASS.getId());
		} else {
			method.setGeneq(TermId.NON_BULKING_BREEDING_METHOD_CLASS.getId());
		}
		return method;

	}
}
