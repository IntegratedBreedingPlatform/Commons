
package org.generationcp.commons.help.document;

public enum HelpModule {
	// Programs
	DASHBOARD("dashboard"), 
	PROGRAM_CREATION("program.creation"), 
	
	// Breeding Activities
	MANAGE_LIST("manage.list"), 
	DESIGN_CROSSES("design.crosses"),
	MANAGE_GERMPLASM("manage.germplasm"),
	MANAGE_GERMPLASM_IMPORT("manage.germplasm.import"),
	MANAGE_GERMPLASM_IMPORT_TEMPLATE("manage.germplasm.import.template"),
	MANAGE_GERMPLASM_IMPORT_UPDATES("manage.germplasm.import.updates"),
	MANAGE_GERMPLASM_IMPORT_UPDATES_TEMPLATE("manage.germplasm.import.updates.template"),

	// ManageStudies
	MANAGE_STUDIES("manage.studies"),
	MANAGE_STUDIES_SETTINGS("manage.studies.settings"),
	MANAGE_STUDIES_GERMPLASM("manage.studies.germplasm"),
	MANAGE_STUDIES_TREATMENT_FACTORS("manage.studies.treatment.factors"),
	MANAGE_STUDIES_ENVIRONMENT("manage.studies.environment"),
	MANAGE_STUDIES_EXPERIMENTAL_DESIGN("manage.studies.experimental.design"),
	MANAGE_STUDIES_SUB_OBSERVATIONS("manage.studies.sub.observations"),
	MANAGE_STUDIES_FIELDMAP_GEOREFERENCE("manage.studies.fieldmap.georeference"),
	MAKE_FIELD_MAPS("make.field.maps"),
	MANAGE_LOCATIONS("manage.locations"),
	MANAGE_BREEDING_METHODS("manage.breeding.methods"),
	MANAGE_CROP_BREEDING_METHODS("manage.crop.settings.breeding.methods"),
	MANAGE_CROP_BREEDING_LOCATIONS("manage.crop.settings.breeding.locations"),

	// Label Printing
	LABEL_PRINTING_GERMPLASM_MANAGER("label.printing.germplasm.manager"),
	LABEL_PRINTING_GERMPLASM_LIST_MANAGER("label.printing.germplasm.list.manager"),
	LABEL_PRINTING_STUDY_MANAGER("label.printing.study.manager"),
	LABEL_PRINTING_INVENTORY_MANAGER("label.printing.inventory.manager"),

	// Information Management
	IMPORT_GERMPLASM("import.germplasm"), 
	MANAGE_GENOTYPING_DATA("manage.genotyping.data"), 
	BROWSE_STUDIES("browse.studies"), 
	MANAGE_ONTOLOGIES("manage.ontologies"),
	MANAGE_ONTOLOGIES_FORMULAS("manage.ontologies.formulas"),
	DATA_IMPORT_TOOL("data.import.tool"),
	
	// Breeder Queries
	HEAD_TO_HEAD("head.to.head"), 
	ADAPTED_GERMPLASM("adapted.germplasm"), 
	TRAIT_DONOR("trait.donor"), 
	
	// Statistical Analysis
	SINGLE_SITE_ANALYSIS("single.site.analysis"), 
	MULTI_SITE_ANALYSIS("multi.site.analysis"), 
	MULTI_YEAR_MULTI_SITE_ANALYSIS("multi.year.multi.site.analysis"), 
	
	// Program Admin
	MANAGE_PROGRAM_SETTINGS_MANAGE_PROGRAM_SETTING("manage.program.settings.update"), 
	BACKUP_PROGRAM_DATA("backup.program.data"), 
	RESTORE_PROGRAM_DATA("restore.program.data"),

	// Manage Inventory
	MANAGE_INVENTORY("manage.inventory"),
	// Manage Samples
	MANAGE_SAMPLES("manage.samples"),

	// Site Administration
	SITE_ADMINISTRATION("site.administration"),

	// Navigation bar
	NAVIGATION_BAR_ABOUT_BMS("navigation.bar.about.bms"),
	NAVIGATION_BAR_ASK_FOR_SUPPORT("navigation.bar.ask.for.support"),

	// Germplasm Lists
	GERMPLASM_LIST("germplasm.list"),
	GERMPLASM_LIST_IMPORT("germplasm.list.import"),
	GERMPLASM_LIST_IMPORT_UPDATE("germplasm.list.import.update"),
	GERMPLASM_LIST_COP_BETA("germplasm.list.cop.beta"),

	// Graphical Queries
	GRAPHICAL_QUERIES("graphical.queries"),

	// Manage Program Settings
	MANAGE_PROGRAM_SETTINGS("manage.program.settings"),

	// Manage Crop Settings
	MANAGE_CROP_SETTINGS("manage.crop.settings"),
	NAME_RULES_FOR_NEW_GERMPLASM("name.rules.for.new.germplasm");

	/* This is the variable name from the property file helplinks.properties */
	private String propertyName;

	HelpModule(String link) {
		this.propertyName = link;
	}

	public String getPropertyName() {
		return this.propertyName;
	}

}
