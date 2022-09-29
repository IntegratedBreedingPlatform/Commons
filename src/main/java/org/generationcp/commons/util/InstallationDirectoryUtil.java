package org.generationcp.commons.util;

import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ToolName;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class InstallationDirectoryUtil {
	
	public static final String WORKSPACE_DIR = "workspace";
	public static final String INPUT = "input";
	public static final String OUTPUT = "output";
	public static final String TEMP = "temp";

	public void createWorkspaceDirectoriesForProject(final String cropName, final String projectName) {

		final File projectDir = this.getFileForWorkspaceProjectDirectory(cropName, projectName);
		final File breedingViewDir = new File(projectDir, ToolName.BREEDING_VIEW.getName());

		// Verify is already exsits the directories.
		if (projectDir.exists() && breedingViewDir.exists()) {
			return;
		}
		// create the directory for the project
		projectDir.mkdirs();

		// create the directory only for breeding_view tool
		final List<String> toolList = Collections.singletonList(ToolName.BREEDING_VIEW.getName());
		for (final String toolName : toolList) {
			final File toolDir = new File(projectDir, toolName);
			toolDir.mkdirs();

			// create the input and output directories
			new File(toolDir, InstallationDirectoryUtil.INPUT).mkdirs();
			new File(toolDir, InstallationDirectoryUtil.OUTPUT).mkdirs();
		}
	}

	private File getFileForWorkspaceProjectDirectory(final String cropName, final String projectName) {
		return new File(this.buildWorkspaceCropDirectoryPath(cropName), projectName);
	}
	
	private String buildWorkspaceCropDirectoryPath(final String cropName) {
		return InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + cropName;
	}

	public void renameOldWorkspaceDirectory(final String oldProjectName, final String cropName, final String newProjectName) {
		final File oldDir = this.getFileForWorkspaceProjectDirectory(cropName, oldProjectName);

		// Rename old project name folder if found, otherwise create folder for latest project name
		if (oldDir.exists()) {
			oldDir.renameTo(this.getFileForWorkspaceProjectDirectory(cropName, newProjectName));
		} else {
			this.createWorkspaceDirectoriesForProject(cropName, newProjectName);
		}
	}

	public String getInputDirectoryForProjectAndTool(final Project project, final ToolName tool) {
		final File toolDir = this.getToolDirectoryForProject(project, tool);
		return new File(toolDir, InstallationDirectoryUtil.INPUT).getAbsolutePath();
	}
	
	public String getOutputDirectoryForProjectAndTool(final Project project, final ToolName tool) {
		final File toolDir = this.getToolDirectoryForProject(project, tool);
		return new File(toolDir, InstallationDirectoryUtil.OUTPUT).getAbsolutePath();
	}
	
	public String getTempFileInOutputDirectoryForProjectAndTool(final String fileName, final String extension, final Project project,
			final ToolName tool) throws IOException {
		final File outputDir = new File(this.getOutputDirectoryForProjectAndTool(project, tool));
		// Create temporary file under output directory of project and tool
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		String finalFilename = fileName;
		// Perform checking that fileName is > 3 characters, else temp file will throw "Prefix too short" error
		if (fileName.length() < 3){
			finalFilename = TEMP;
		}
		return File.createTempFile(finalFilename, extension, outputDir).getAbsolutePath();
	}
	
	protected File getToolDirectoryForProject(final Project project, final ToolName tool) {
		final File projectDir = this.getFileForWorkspaceProjectDirectory(project.getCropType().getCropName(), project.getProjectName());
		return new File(projectDir, tool.getName());
	}

	public void recursiveFileDelete(final File file) {
        //to end the recursive loop
        if (!file.exists()){
        	return;
        }
         
        //if directory, go inside and call recursively
        if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
                //call recursively
                recursiveFileDelete(f);
            }
        }
        //call delete to delete files and empty directory
        file.delete();
    }

}
