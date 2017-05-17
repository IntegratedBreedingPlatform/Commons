
package org.generationcp.commons.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Methods;
import org.generationcp.middleware.pojos.Name;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class CrossingUtilTest {

	private final Integer methodId = 99;
	private final Integer defaultTypeId = 5;
	private GermplasmDataManager germplasmDataManager;
	private List<Pair<Germplasm, Name>> germplasmPairs;
	private Name name;

	@Before
	public void setUp() {
		this.germplasmDataManager = Mockito.mock(GermplasmDataManager.class);
		this.germplasmPairs = new ArrayList<>();
		this.name = new Name();
		this.name.setTypeId(this.defaultTypeId);
		final Germplasm germplasm = new Germplasm();
		germplasm.setMethodId(this.methodId);
		this.germplasmPairs.add(new ImmutablePair<>(germplasm, this.name));
	}

	/**
	 * Current rules expect a Single Cross breeding method when one (or both)
	 * parents have a GNPGS of -1 (signifying they do not have available
	 * parents) and the other parent has a GNPGS of 1 (indicating it has a
	 * single parent)
	 */
	@Test
	public void testDetermineBreedingMethodBasedOnParentalLineParentSingleCross() {
		final Germplasm maleParent = new Germplasm();
		maleParent.setGnpgs(1);
		final Germplasm femaleParent = new Germplasm();
		femaleParent.setGnpgs(-1);

		Integer methodId = CrossingUtil.determineBreedingMethodBasedOnParentalLine(femaleParent, maleParent,
				Mockito.mock(Germplasm.class), Mockito.mock(Germplasm.class), Mockito.mock(Germplasm.class),
				Mockito.mock(Germplasm.class));

		Assert.assertEquals("Invalid method id computed using parental line", Methods.SINGLE_CROSS.getMethodID(),
				methodId);

		maleParent.setGnpgs(-1);

		// aside from the male and female parent, the other parameters here are
		// mocked to help indicate that we are not interested in the values of
		// these parameters for this particular scenario
		methodId = CrossingUtil.determineBreedingMethodBasedOnParentalLine(femaleParent, maleParent,
				Mockito.mock(Germplasm.class), Mockito.mock(Germplasm.class), Mockito.mock(Germplasm.class),
				Mockito.mock(Germplasm.class));

		Assert.assertEquals("Invalid method id computed using parental line", Methods.SINGLE_CROSS.getMethodID(),
				methodId);

	}

	/**
	 * Current rules expect a Double Cross breeding method when both parents
	 * have a GNPGS > 0 (signifying they both have parents) and that each parent
	 * has Single Cross as a breeding method
	 */
	@Test
	public void testDetermineBreedingMethodBasedOnParentalLineParentDoubleCross() {
		final Germplasm maleParent = new Germplasm();
		maleParent.setGnpgs(1);
		maleParent.setMethodId(Methods.SINGLE_CROSS.getMethodID());
		final Germplasm femaleParent = new Germplasm();
		femaleParent.setGnpgs(1);
		femaleParent.setMethodId(Methods.SINGLE_CROSS.getMethodID());

		final Integer methodId = CrossingUtil.determineBreedingMethodBasedOnParentalLine(femaleParent, maleParent,
				Mockito.mock(Germplasm.class), Mockito.mock(Germplasm.class), Mockito.mock(Germplasm.class),
				Mockito.mock(Germplasm.class));

		Assert.assertEquals("Invalid method id computed using parental line", Methods.DOUBLE_CROSS.getMethodID(),
				methodId);
	}

	/**
	 * Current rules expect a back cross when one parent does not have parents
	 * (gnpgs < 0) which we'll call parent A, and the other parent has 2 (gnpgs
	 * == 2) which we'll call parent B, The GID of either the father or the
	 * mother of parent B must also be the same as the GID of parent A.
	 */
	@Test
	public void testDetermineBreedingMethodBasedOnParentalLineBackCross() {
		final Integer maleParentGID = 1;
		final Germplasm maleParent = new Germplasm();
		maleParent.setGid(maleParentGID);
		maleParent.setGnpgs(-1);
		final Germplasm femaleParent = new Germplasm();
		femaleParent.setGnpgs(2);
		final Germplasm fatherOfFemale = new Germplasm();
		fatherOfFemale.setGid(maleParentGID);

		final Integer methodId = CrossingUtil.determineBreedingMethodBasedOnParentalLine(femaleParent, maleParent,
				Mockito.mock(Germplasm.class), fatherOfFemale, Mockito.mock(Germplasm.class),
				Mockito.mock(Germplasm.class));

		Assert.assertEquals("Invalid method id computed using parental line", Methods.BACKCROSS.getMethodID(),
				methodId);
	}

	/**
	 * Current rules expect a three way cross when one parent does not have
	 * parents (gnpgs < 0) which we'll call parent A, and the other parent has 2
	 * (gnpgs == 2) which we'll call parent B. Also, the GID of parent A must be
	 * different from either parent of parent B and the method of parent A must be single cross 
	 */
	@Test
	public void testDetermineBreedingMethodBasedOnParentalLineThreeWayCross() {
		final Integer maleParentGID = 1;
		final Germplasm maleParent = new Germplasm();
		maleParent.setGid(maleParentGID);
		maleParent.setGnpgs(-1);
		final Germplasm femaleParent = new Germplasm();
		femaleParent.setGnpgs(2);
		femaleParent.setMethodId(Methods.SINGLE_CROSS.getMethodID());
		final Germplasm fatherOfFemale = new Germplasm();

		// here we are just trying to emphasize that the gid of the father of
		// the female germplasm is different from the germplasm of the male
		fatherOfFemale.setGid(maleParentGID + 1);

		final Integer methodId = CrossingUtil.determineBreedingMethodBasedOnParentalLine(femaleParent, maleParent,
				Mockito.mock(Germplasm.class), fatherOfFemale, Mockito.mock(Germplasm.class),
				Mockito.mock(Germplasm.class));

		Assert.assertEquals("Invalid method id computed using parental line", Methods.THREE_WAY_CROSS.getMethodID(),
				methodId);
	}

	@Test
	public void testDetermineBreedingMethodBasedOnParentalLineComplexCross() {
		final Integer maleParentGID = 1;
		final Germplasm maleParent = new Germplasm();
		maleParent.setGid(maleParentGID);
		maleParent.setGnpgs(-1);
		final Germplasm femaleParent = new Germplasm();
		femaleParent.setGnpgs(2);
		final Germplasm fatherOfFemale = new Germplasm();

		// here we are just trying to emphasize that the gid of the father of
		// the female germplasm is different from the germplasm of the male
		fatherOfFemale.setGid(maleParentGID + 1);

		final Integer methodId = CrossingUtil.determineBreedingMethodBasedOnParentalLine(femaleParent, maleParent,
				Mockito.mock(Germplasm.class), fatherOfFemale, Mockito.mock(Germplasm.class),
				Mockito.mock(Germplasm.class));

		Assert.assertEquals("Invalid method id computed using parental line", Methods.COMPLEX_CROSS.getMethodID(),
				methodId);
	}

	/**
	 * One of the rules for complex cross is when both parents have parents
	 * (gnpgs > 0), and one OR both breeding methods are NOT single crosses
	 */
	@Test
	public void testDetermineBreedingMethodBasedOnParentalLineComplexCrossBothGNPGSAboveZero() {
		final Germplasm maleParent = new Germplasm();
		maleParent.setGnpgs(1);
		maleParent.setMethodId(Methods.BACKCROSS.getMethodID());
		final Germplasm femaleParent = new Germplasm();
		femaleParent.setGnpgs(1);
		femaleParent.setMethodId(Methods.SINGLE_CROSS.getMethodID());

		final Integer methodId = CrossingUtil.determineBreedingMethodBasedOnParentalLine(femaleParent, maleParent,
				Mockito.mock(Germplasm.class), Mockito.mock(Germplasm.class), Mockito.mock(Germplasm.class),
				Mockito.mock(Germplasm.class));

		Assert.assertEquals("Invalid method id computed using parental line", Methods.COMPLEX_CROSS.getMethodID(),
				methodId);
	}

	/**
	 * The other rule for complex cross is when one parent has no parent (gnpgs
	 * < 0), and the other parent has more than 2 (gnpgs > 2)
	 */
	@Test
	public void testDetermineBreedingMethodBasedOnParentalLineComplexCrossOneGnpgsNegative() {
		final Germplasm maleParent = new Germplasm();
		maleParent.setGnpgs(-1);
		final Germplasm femaleParent = new Germplasm();
		femaleParent.setGnpgs(3);

		final Integer methodId = CrossingUtil.determineBreedingMethodBasedOnParentalLine(femaleParent, maleParent,
				Mockito.mock(Germplasm.class), Mockito.mock(Germplasm.class), Mockito.mock(Germplasm.class),
				Mockito.mock(Germplasm.class));

		Assert.assertEquals("Invalid method id computed using parental line", Methods.COMPLEX_CROSS.getMethodID(),
				methodId);
	}

	@Test
	public void testApplyMethodNameTypeIfMethodSnametypeExists() throws Exception {
		final Method method = new Method();
		method.setSnametype(88);
		Mockito.doReturn(method).when(this.germplasmDataManager).getMethodByID(this.methodId);
		CrossingUtil.applyMethodNameType(this.germplasmDataManager, this.germplasmPairs, this.defaultTypeId);
		Assert.assertEquals("Sname type should be the same as the method snametype", method.getSnametype(),
				this.name.getTypeId());
	}

	@Test
	public void testApplyMethodNameTypeIfMethodSnametypeDoesNotExists() throws Exception {
		final Method method = new Method();
		method.setSnametype(null);
		Mockito.doReturn(method).when(this.germplasmDataManager).getMethodByID(this.methodId);
		CrossingUtil.applyMethodNameType(this.germplasmDataManager, this.germplasmPairs, this.defaultTypeId);
		Assert.assertEquals("Should use the default name type", this.defaultTypeId, this.name.getTypeId());
	}
}
