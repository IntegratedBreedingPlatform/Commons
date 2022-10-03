package org.generationcp.commons.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.ruleengine.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;

public class ImportedGermplasmTestDataInitializer {

	public static ImportedGermplasm createImportedGermplasm() {
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		importedGermplasm.setIndex(1);
		importedGermplasm.setEntryNumber(1);
		importedGermplasm.setEntryCode("1");
		importedGermplasm.setDesig("Desig");
		importedGermplasm.setGid("1");
		importedGermplasm.setGroupId(1);
		importedGermplasm.setSource("SOURCE");
		importedGermplasm.setEntryCode("1");
		importedGermplasm.setCross("CROSS");
		importedGermplasm.setEntryTypeCategoricalID(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		return importedGermplasm;
	}

	public static List<ImportedGermplasm> createImportedGermplasmList() {
		final List<ImportedGermplasm> list = new ArrayList<>();
		list.add(ImportedGermplasmTestDataInitializer.createImportedGermplasm());
		return list;
	}
}
