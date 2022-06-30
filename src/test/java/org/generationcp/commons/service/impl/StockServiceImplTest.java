
package org.generationcp.commons.service.impl;

import org.generationcp.commons.ruleengine.stockid.StockIDSeparatorRule;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.service.api.inventory.LotService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

// FIXME!! : please revive ASAP
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class StockServiceImplTest {

	public static final Integer DUMMY_NOTATION_NUMBER = 5;
	public static final String DUMMY_BREEDER_IDENTIFIER = "DV";

	public static final int DUMMY_DETAIL_COUNT = 5;
	public static final String EXPECTED_PREFIX = StockServiceImplTest.DUMMY_BREEDER_IDENTIFIER
			+ (StockServiceImplTest.DUMMY_NOTATION_NUMBER + 1) + StockIDSeparatorRule.DEFAULT_SEPARATOR;

	public static final String COMMA = ",";
	public static final String TEST_INVENTORY_ID = "TR1-123";
	public static final String TEST_INVENTORY_ID_2 = "TR1-124";
	public static final String TEST_INVENTORY_ID_3 = "TR1-125";
	public static final String[] TEST_INVENTORY_ID_ARRAY = {StockServiceImplTest.TEST_INVENTORY_ID,
			StockServiceImplTest.TEST_INVENTORY_ID_2, StockServiceImplTest.TEST_INVENTORY_ID_3, null, null};
	public static final String[] TEST_BULK_WITH_ARRAY = {
			StockServiceImplTest.TEST_INVENTORY_ID_2 + StockServiceImplTest.COMMA + StockServiceImplTest.TEST_INVENTORY_ID_3,
			StockServiceImplTest.TEST_INVENTORY_ID + StockServiceImplTest.COMMA + StockServiceImplTest.TEST_INVENTORY_ID_3,
			StockServiceImplTest.TEST_INVENTORY_ID + StockServiceImplTest.COMMA + StockServiceImplTest.TEST_INVENTORY_ID_2, null, null};
	public static final String PLOT_DUPE_PREFIX = "Plot Dupe: ";
	public static final String[] TEST_DUPLICATE_ARRAY = {
			StockServiceImplTest.PLOT_DUPE_PREFIX + StockServiceImplTest.TEST_INVENTORY_ID_2 + StockServiceImplTest.COMMA
			+ StockServiceImplTest.TEST_INVENTORY_ID_3,
			StockServiceImplTest.PLOT_DUPE_PREFIX + StockServiceImplTest.TEST_INVENTORY_ID + StockServiceImplTest.COMMA
			+ StockServiceImplTest.TEST_INVENTORY_ID_3,
			StockServiceImplTest.PLOT_DUPE_PREFIX + StockServiceImplTest.TEST_INVENTORY_ID + StockServiceImplTest.COMMA
			+ StockServiceImplTest.TEST_INVENTORY_ID_2,
			null, null};

	@Resource
	private LotService lotService;

	@Resource
	private StockServiceImpl inventoryStockService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testCalculateNextStockIDPrefix() throws MiddlewareException {
		Mockito.when(this.lotService.getCurrentNotationNumberForBreederIdentifier(StockServiceImplTest.DUMMY_BREEDER_IDENTIFIER))
		.thenReturn(StockServiceImplTest.DUMMY_NOTATION_NUMBER);

		String prefix = this.inventoryStockService.calculateNextStockIDPrefix(StockServiceImplTest.DUMMY_BREEDER_IDENTIFIER, null);

		Assert.assertEquals(StockServiceImplTest.EXPECTED_PREFIX, prefix);
	}

}
