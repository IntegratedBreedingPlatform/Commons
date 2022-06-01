package org.generationcp.commons.spring.util;

import com.google.common.base.Optional;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.middleware.api.program.ProgramService;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ProjectActivity;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * This is the spring bean managed version of some of the methods used in the org.generationcp.commons.util.ContextUtil class
 */
public class ContextUtil {

	@Resource
	private HttpServletRequest request;

	@Resource
	private ProgramService programService;

	@Resource
	private WorkbenchDataManager workbenchDataManager;

	@Resource
	private UserService userService;

	public String getCurrentProgramUUID() {
		final Project program = org.generationcp.commons.util.ContextUtil.getProjectInContext(this.programService, this.request);
		if (program != null) {
			return program.getUniqueID();
		}
		return null;
	}

	public ContextInfo getContextInfoFromSession() {
		return (ContextInfo) WebUtils.getSessionAttribute(this.request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);
	}

	public Project getProjectInContext() {
		return org.generationcp.commons.util.ContextUtil.getProjectInContext(this.programService, this.request);
	}

	public Optional<Project> getProject() {
		return org.generationcp.commons.util.ContextUtil.getProject(this.programService, this.request);
	}

	public int getCurrentWorkbenchUserId() {
		return org.generationcp.commons.util.ContextUtil.getCurrentWorkbenchUserId(this.request);
	}

	public WorkbenchUser getCurrentWorkbenchUser() {
		return org.generationcp.commons.util.ContextUtil.getCurrentWorkbenchUser(this.userService, this.request);
	}

	public void logProgramActivity(final String activityTitle, final String activityDescription) {
		final Project currentProject = this.getProjectInContext();
		final WorkbenchUser currentUser = this.getCurrentWorkbenchUser();

		final ProjectActivity projAct =
				new ProjectActivity(currentProject.getProjectId().intValue(), currentProject, activityTitle, activityDescription,
						currentUser, new Date());

		this.workbenchDataManager.addProjectActivity(projAct);
	}

	public boolean shouldShowReleaseNotes() {
		final ContextInfo contextInfo = this.getContextInfoFromSession();
		if (contextInfo.shouldShowReleaseNotes()) {
			// Set showReleaseNotes value to false in order to don't show again the popup after the success login
			org.generationcp.commons.util.ContextUtil.setContextInfo(this.request, contextInfo.getLoggedInUserId(), contextInfo.getSelectedProjectId(), false);
			return true;
		}

		return false;
	}

}
