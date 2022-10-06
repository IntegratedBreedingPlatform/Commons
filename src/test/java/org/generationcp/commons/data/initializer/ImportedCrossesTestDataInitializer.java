package org.generationcp.commons.data.initializer;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.ruleengine.pojo.ImportedCross;

public class ImportedCrossesTestDataInitializer {
	public ImportedCross createImportedCrosses(final boolean hasHybridMethod){
		ImportedCross importedCross = new ImportedCross();
		final String method = hasHybridMethod ? "TCR" : "UGM";
		importedCross.setRawBreedingMethod(method);
		return importedCross;
	}

	public List<ImportedCross> createImportedCrossesList(final int count, final boolean hasHybridMethod){
		List<ImportedCross> importedCrossList = new ArrayList<ImportedCross>();
		for(int ctr  = 0; ctr<count; ctr++){
			importedCrossList.add(createImportedCrosses(hasHybridMethod));
		}
		return importedCrossList;
	}
	
}
