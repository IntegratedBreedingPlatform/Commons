package org.generationcp.commons.service.impl;

import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.commons.ruleengine.RuleFactory;
import org.generationcp.commons.ruleengine.coding.CodingRuleExecutionContext;
import org.generationcp.commons.ruleengine.service.RulesService;
import org.generationcp.commons.service.GermplasmCodeGenerationService;
import org.generationcp.commons.service.GermplasmNamingService;
import org.generationcp.middleware.domain.germplasm.GermplasmCodeNameBatchRequestDto;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.pojos.naming.NamingConfiguration;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;
import org.generationcp.middleware.service.api.GermplasmGroupingService;
import org.generationcp.middleware.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service to generate Code Names (aka. Group Names)
 */
@Transactional
public class GermplasmCodeGenerationServiceImpl implements GermplasmCodeGenerationService {

	private static final String GERMPLASM_NOT_PART_OF_MANAGEMENT_GROUP =
		"Germplasm (gid: %s) is not part of a management group. Can not assign group name.";
	protected static final String CODING_RULE_SEQUENCE = "coding";

	@Autowired
	private RulesService rulesService;

	@Autowired
	private RuleFactory ruleFactory;

	@Autowired
	private GermplasmGroupingService germplasmGroupingService;

	@Autowired
	private GermplasmNamingService germplasmNamingService;

	private DaoFactory daoFactory;

	public GermplasmCodeGenerationServiceImpl() {
		// do nothing
	}

	public GermplasmCodeGenerationServiceImpl(final HibernateSessionProvider sessionProvider) {
		this.daoFactory = new DaoFactory(sessionProvider);
	}

	@Override
	public List<GermplasmGroupNamingResult> createCodeNames(final GermplasmCodeNameBatchRequestDto germplasmCodeNameBatchRequestDto)
		throws RuleException, InvalidGermplasmNameSettingException {

		final UserDefinedField nameType =
			this.daoFactory.getUserDefinedFieldDAO().getByTableTypeAndCode("NAMES", "NAME", germplasmCodeNameBatchRequestDto.getNameType());

		// For manual code naming
		if (germplasmCodeNameBatchRequestDto.getGermplasmCodeNameSetting() != null) {
			return this.applyGroupNamesForManualNaming(new HashSet<>(germplasmCodeNameBatchRequestDto.getGids()),
				germplasmCodeNameBatchRequestDto.getGermplasmCodeNameSetting(), nameType);
		} else {
			// For automatic code naming
			return this.applyGroupNamesForAutomaticNaming(new HashSet<>(germplasmCodeNameBatchRequestDto.getGids()),
				nameType);
		}
	}

	@Override
	public String getNextNameInSequence(final GermplasmNameSetting setting) throws InvalidGermplasmNameSettingException {
		return this.germplasmNamingService.getNextNameInSequence(setting);
	}

	protected List<GermplasmGroupNamingResult> applyGroupNamesForAutomaticNaming(final Set<Integer> gidsToProcess,
		final UserDefinedField nameType) throws RuleException {

		final NamingConfiguration namingConfiguration = this.daoFactory.getNamingConfigurationDAO().getByName(nameType.getFname());

		final List<String> executionOrder = Arrays.asList(this.ruleFactory.getRuleSequenceForNamespace(CODING_RULE_SEQUENCE));
		final CodingRuleExecutionContext codingRuleExecutionContext = new CodingRuleExecutionContext(executionOrder, namingConfiguration);
		final List<GermplasmGroupNamingResult> assignCodesResultsList = new ArrayList<>();

		for (final Integer gid : gidsToProcess) {
			final String generatedCodeName = (String) this.rulesService.runRules(codingRuleExecutionContext);
			assignCodesResultsList.add(this.applyGroupName(gid, nameType, generatedCodeName));
			codingRuleExecutionContext.reset();
		}

		return assignCodesResultsList;
	}

	protected List<GermplasmGroupNamingResult> applyGroupNamesForManualNaming(final Set<Integer> gids, final GermplasmNameSetting setting,
		final UserDefinedField nameType) throws InvalidGermplasmNameSettingException {
		final List<GermplasmGroupNamingResult> assignCodesResultsList = new ArrayList<>();
		final boolean startNumberSpecified = setting.getStartNumber() != null;
		Integer startNumber = setting.getStartNumber();

		// Call this method to check first if the name settings are valid.
		this.germplasmNamingService.getNextNameInSequence(setting);

		for (final Integer gid : gids) {
			// Increment start number of succeeding germplasm processed based on initial start # specified, if any
			if (startNumberSpecified) {
				setting.setStartNumber(startNumber++);
			}
			final String nameWithSequence = this.germplasmNamingService.generateNextNameAndIncrementSequence(setting);
			final GermplasmGroupNamingResult result = this.applyGroupName(gid, nameType, nameWithSequence);
			assignCodesResultsList.add(result);
		}
		return assignCodesResultsList;
	}

	protected GermplasmGroupNamingResult applyGroupName(final Integer gid,
		final UserDefinedField nameType, final String generatedCodeName) {

		final GermplasmGroupNamingResult result = new GermplasmGroupNamingResult();
		result.setGid(gid);

		final Germplasm germplasm = this.daoFactory.getGermplasmDao().getById(gid);

		if (germplasm.getMgid() == null || germplasm.getMgid() == 0) {
			result.addMessage(String.format(GERMPLASM_NOT_PART_OF_MANAGEMENT_GROUP, germplasm.getGid()));
			return result;
		}

		final List<Germplasm> groupMembers =
			this.germplasmGroupingService.getDescendantGroupMembers(germplasm.getGid(), germplasm.getMgid());
		groupMembers.add(0, germplasm);

		for (final Germplasm member : groupMembers) {
			this.addName(member, generatedCodeName, nameType, result);
		}

		return result;

	}

	private void addName(final Germplasm germplasm, final String groupName, final UserDefinedField nameType,
		final GermplasmGroupNamingResult result) {

		final List<Name> currentNames = germplasm.getNames();

		Name existingNameOfGivenType = null;
		if (!currentNames.isEmpty() && nameType != null) {
			for (final Name name : currentNames) {
				if (nameType.getFldno().equals(name.getTypeId())) {
					existingNameOfGivenType = name;
					break;
				}
			}
		}

		if (existingNameOfGivenType == null) {
			// Make the current preferred name as non-preferred by setting nstat = 0
			final Name currentPreferredName = germplasm.findPreferredName();
			if (currentPreferredName != null) {
				// nstat = 0 means it is not a preferred name.
				currentPreferredName.setNstat(0);
			}

			final Name name = new Name();
			name.setGermplasm(germplasm);
			name.setTypeId(nameType.getFldno());
			name.setNval(groupName);
			// nstat = 1 means it is preferred name.
			name.setNstat(1);
			// Hard coded to zero for now.
			name.setLocationId(0);
			name.setNdate(Util.getCurrentDateAsIntegerValue());
			name.setReferenceId(0);

			germplasm.getNames().add(name);
			this.daoFactory.getGermplasmDao().save(germplasm);
			result.addMessage(
				String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.", germplasm.getGid(),
					groupName, nameType.getFcode()));
		} else {
			result.addMessage(String.format("Germplasm (gid: %s) already has existing name %s of type %s. Supplied name %s was not added.",
				germplasm.getGid(), existingNameOfGivenType.getNval(), nameType.getFcode(), groupName));
		}
	}

}
