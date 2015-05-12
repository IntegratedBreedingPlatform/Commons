package org.generationcp.commons.pojo;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "reports")
public class CustomReportList implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	List<CustomReportType> customReportType;
	public CustomReportList(){
		
	}
	public CustomReportList(List<CustomReportType> customReportType) {
		super();
		this.customReportType = customReportType;
	}
	@XmlElement(name = "report")
	public List<CustomReportType> getCustomReportType() {
		return customReportType;
	}

	public void setCustomReportType(List<CustomReportType> customReportType) {
		this.customReportType = customReportType;
	}
	
}
