
package org.generationcp.commons.help.document;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.generationcp.commons.tomcat.util.TomcatUtil;
import org.generationcp.commons.util.WorkbenchAppPathResolver;
import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
import org.generationcp.commons.vaadin.ui.BaseSubWindow;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import org.springframework.beans.factory.annotation.Value;

@Configurable
public class HelpWindow extends BaseSubWindow implements InitializingBean, InternationalizableComponent {

	private static final long serialVersionUID = -5526578750776397878L;
	private static final Logger LOG = LoggerFactory.getLogger(HelpWindow.class);
	private static final String HTML_DOC_URL = "BMS_HTML/index.html";
	private static final String BMS_INSTALLATION_DIR_POSTFIX = "infrastructure/tomcat/webapps/";
	private static final String BMS_HTML = "BMS_HTML";

	// Components
	private ComponentContainer rootLayout;

	private static final String WINDOW_WIDTH = "640px";
	private static final String WINDOW_HEIGHT = "415px";

	private WorkbenchDataManager workbenchDataManager;

	private TomcatUtil tomcatUtil;

	@Value("${workbench.version}")
	private String workbenchVersion;

	public HelpWindow(WorkbenchDataManager workbenchDataManager, TomcatUtil tomcatUtil) {
		super();
		this.workbenchDataManager = workbenchDataManager;
		this.tomcatUtil = tomcatUtil;
	}

	@Override
	public void afterPropertiesSet() {
		this.initializeLayout();
	}

	protected void initializeLayout() {
		this.setWidth(HelpWindow.WINDOW_WIDTH);
		this.setHeight(HelpWindow.WINDOW_HEIGHT);
		this.setResizable(false);
		this.setModal(true);
		this.setCaption("BREEDING MANAGEMENT SYSTEM | WORKBENCH");
		this.setStyleName("gcp-help-window");

		this.rootLayout = this.getContent();

		Label version = new Label(workbenchVersion);
		version.setStyleName("gcp-version");
		this.rootLayout.addComponent(version);

		Panel panel = new Panel();
		// fix for IE
		panel.setWidth("600px");
		this.rootLayout.addComponent(panel);

		// detect if docs are installed
		// if not, show a message prompting them to download and install it first

		String installationDirectory = HelpDocumentUtil.getInstallationDirectory(this.workbenchDataManager);
		if (!HelpDocumentUtil.isDocumentsFolderFound(installationDirectory)) {
			// if the document directory does not exist,
			// it means that the BMS Documentation has not been installed
			CustomLayout helpLayout = new CustomLayout("help_not_installed");
			panel.setContent(helpLayout);
			return;
		} else {
			CustomLayout helpLayout = new CustomLayout("help");
			panel.setContent(helpLayout);

			this.deployDocumentsToTomcat(installationDirectory);

			Link htmlLink = this.buildHTMLLink();
			helpLayout.addComponent(htmlLink, "html_link");
		}
	}

	private Link buildHTMLLink() {
		Link htmlLink = new Link();
		htmlLink.setResource(new ExternalResource("https://www.integratedbreeding.net/62/training/bms-user-manual"));
		htmlLink.setCaption("BMS Manual HTML Version");
		htmlLink.setTargetName("_blank");
		htmlLink.setIcon(new ThemeResource("../gcp-default/images/html_icon.png"));
		htmlLink.addStyleName("gcp-html-link");
		return htmlLink;
	}

	private void deployDocumentsToTomcat(String installationDirectory) {
		String docsDirectory = installationDirectory + File.separator + "Documents" + File.separator;

		String targetHTMLPath = installationDirectory + File.separator + HelpWindow.BMS_INSTALLATION_DIR_POSTFIX + HelpWindow.BMS_HTML;

		try {
			FileUtils.deleteDirectory(new File(targetHTMLPath));
			FileUtils.copyDirectory(new File(this.getHtmlFilesLocation(docsDirectory)), new File(targetHTMLPath));

			String contextPath = TomcatUtil.getContextPathFromUrl(WorkbenchAppPathResolver.getFullWebAddress(HelpWindow.HTML_DOC_URL));
			String localWarPath = TomcatUtil.getLocalWarPathFromUrl(WorkbenchAppPathResolver.getFullWebAddress(HelpWindow.HTML_DOC_URL));
			this.tomcatUtil.deployLocalWar(contextPath, localWarPath);
		} catch (IOException e) {
			HelpWindow.LOG.error(e.getMessage(), e);
		}
	}

	private String getHtmlFilesLocation(String baseDir) {
		File baseDirFile = new File(baseDir);
		Collection<File> files = FileUtils.listFiles(baseDirFile, new RegexFileFilter("index.html$"), DirectoryFileFilter.DIRECTORY);

		if (files.isEmpty()) {
			return "";
		}

		for (File f : files) {
			File parentFile = f.getParentFile();
			if (parentFile != null && parentFile.getParent().equals(baseDirFile.getPath())) {
				return f.getParent();
			}
		}

		return "";

	}

	@Override
	public void updateLabels() {
		// no labels to update
	}

}
