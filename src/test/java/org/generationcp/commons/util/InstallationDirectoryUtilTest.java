package org.generationcp.commons.util;

import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class InstallationDirectoryUtilTest {
	
	private static final String DUMMY_PROJECT_NAME = "Maize Tutorial Program";
	private static final String TEMP_FILENAME = "temp";
	private static final String XLS_EXTENSION = ".xls";

	private final InstallationDirectoryUtil installationDirUtil = new InstallationDirectoryUtil();
	
	private Project project;
	
	@Before
	public void setup() {
		this.project = ProjectTestDataInitializer.createProject();
		project.setProjectName(DUMMY_PROJECT_NAME + new Random().nextInt());
		
		// Make sure test environment is clean
		this.deleteTestInstallationDirectory();
		final File workspaceDir = new File(InstallationDirectoryUtil.WORKSPACE_DIR);
		Assert.assertFalse(workspaceDir.exists());
	}
	
	@Test
	public void testCreateWorkspaceDirectoriesForProject() {
		this.installationDirUtil
			.createWorkspaceDirectoriesForProject(this.project.getCropType().getCropName(), this.project.getProjectName());

		final File projectWorkspaceDirectory =
				new File(InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + this.project.getCropType().getCropName(), this.project.getProjectName());
		Assert.assertTrue(projectWorkspaceDirectory.exists());
		this.verifyProjectFolderSubdirectories(projectWorkspaceDirectory, true);
	}

	// Check the existence of "breeding_view" directory under program with "input" and "output" subdirectories
	private void verifyProjectFolderSubdirectories(final File projectWorkspaceDirectory, final boolean exists) {
		if (exists) {
			Assert.assertEquals(1, projectWorkspaceDirectory.list().length);
		}
		final File breedingViewDirectory = new File(projectWorkspaceDirectory, ToolName.BREEDING_VIEW.getName());
		Assert.assertEquals(exists, breedingViewDirectory.exists());
		final File bvInputDirectory = new File(breedingViewDirectory, InstallationDirectoryUtil.INPUT);
		Assert.assertEquals(exists, bvInputDirectory.exists());
		final File bvOutputDirectory = new File(breedingViewDirectory, InstallationDirectoryUtil.OUTPUT);
		Assert.assertEquals(exists, bvOutputDirectory.exists());
	}
	
	@Test
	public void testCreateWorkspaceDirectoriesForProjectWhenDirectoryAlreadyExists() {
		// Already create project directory. Test method should not continue with creating sub-contents
		final String cropName = "banana";
		final File projectWorkspaceDirectory =
				new File(InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + cropName, this.project.getProjectName());
		projectWorkspaceDirectory.mkdirs();

		this.installationDirUtil
			.createWorkspaceDirectoriesForProject(this.project.getCropType().getCropName(), this.project.getProjectName());
		
		Assert.assertTrue(projectWorkspaceDirectory.exists());
		// Check that "breeding_view" directory and sub-folders will not be created anymore
		this.verifyProjectFolderSubdirectories(projectWorkspaceDirectory, false);
	}
	
	@Test
	public void testRenameOldWorkspaceDirectoryWhenOldProgramFolderExists() {
		// Existing directory should be renamed to new program name
		final String oldProjectName = "Old Maize Program";
		final File oldProjectWorkspaceDirectory =
				new File(InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + project.getCropType().getCropName(), oldProjectName);
		oldProjectWorkspaceDirectory.mkdirs();
		Assert.assertTrue(oldProjectWorkspaceDirectory.exists());
		
		this.installationDirUtil.renameOldWorkspaceDirectory(oldProjectName, this.project);
		// Folder for old project name should not exist anymore since it was renamed
		Assert.assertFalse(oldProjectWorkspaceDirectory.exists());
		final File newProjectWorkspaceDirectory = new File(
				InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + project.getCropType().getCropName(), this.project.getProjectName());
		Assert.assertTrue(newProjectWorkspaceDirectory.exists());
		// Check that "breeding_view" directory and sub-folders will not be created anymore
		this.verifyProjectFolderSubdirectories(newProjectWorkspaceDirectory, false);
	}
	
	@Test
	public void testRenameOldWorkspaceDirectoryWhenOldProgramFolderDoesNotExist() {
		final String oldProjectName = "Old Maize Program";
		final File oldProjectWorkspaceDirectory =
				new File(InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + project.getCropType().getCropName(), oldProjectName);
		Assert.assertFalse(oldProjectWorkspaceDirectory.exists());
		
		this.installationDirUtil.renameOldWorkspaceDirectory(oldProjectName, project);
		// Folder for old project name should still not exist
		Assert.assertFalse(oldProjectWorkspaceDirectory.exists());
		final File newProjectWorkspaceDirectory = new File(
				InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + project.getCropType().getCropName(), this.project.getProjectName());
		// Folder for new project name should now exist
		Assert.assertTrue(newProjectWorkspaceDirectory.exists());
		this.verifyProjectFolderSubdirectories(newProjectWorkspaceDirectory, true);
	}
	
	@Test
	public void testResetWorkspaceDirectoryForCropWhenOldProgramFoldersExists() {
		// Create crop workspace directory with existing programs
		final String oldProjectName1 = "Old Maize Program 1";
		final Project project = ProjectTestDataInitializer.createProject();
		final CropType cropType = project.getCropType();
		final File oldProjectWorkspaceDirectory1 =
				new File(InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + cropType.getCropName(), oldProjectName1);
		oldProjectWorkspaceDirectory1.mkdirs();
		Assert.assertTrue(oldProjectWorkspaceDirectory1.exists());
		final String oldProjectName2 = "Old Maize Program 2";
		final File oldProjectWorkspaceDirectory2 =
				new File(InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + cropType.getCropName(), oldProjectName2);
		oldProjectWorkspaceDirectory2.mkdirs();
		Assert.assertTrue(oldProjectWorkspaceDirectory2.exists());
		
		project.setProjectName(DUMMY_PROJECT_NAME + "1");
		final Project project2 = ProjectTestDataInitializer.createProject();
		project2.setProjectName(DUMMY_PROJECT_NAME + "2");
		final Project project3 = ProjectTestDataInitializer.createProject();
		project3.setProjectName(DUMMY_PROJECT_NAME + "3");
		final List<Project> projects = Arrays.asList(project, project2, project3);
		this.installationDirUtil.resetWorkspaceDirectoryForCrop(cropType, projects);
		
		// Folder for old project names should not exist anymore and folders for new programs should have been generated
		Assert.assertFalse(oldProjectWorkspaceDirectory1.exists());
		Assert.assertFalse(oldProjectWorkspaceDirectory2.exists());
		final File cropWorkspaceDirectory = new File(InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + cropType.getCropName());
		Assert.assertTrue(cropWorkspaceDirectory.exists());
		Assert.assertEquals(projects.size(), cropWorkspaceDirectory.list().length);
		for (final Project newProject : projects) {
			final File newProjectWorkspaceDirectory = new File(
					InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + cropType.getCropName(), newProject.getProjectName());
			Assert.assertTrue(newProjectWorkspaceDirectory.exists());
			this.verifyProjectFolderSubdirectories(newProjectWorkspaceDirectory, true);
		}
		
	}
	
	@Test
	public void testResetWorkspaceDirectoryForCropWhenCropDirectoryDoesNotExist() {
		// Create crop workspace directory with existing programs
		final Project project = ProjectTestDataInitializer.createProject();
		final CropType cropType = project.getCropType();
		final File cropWorkspaceDirectory = new File(InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + cropType.getCropName());
		Assert.assertFalse(cropWorkspaceDirectory.exists());
		
		project.setProjectName(DUMMY_PROJECT_NAME + "1");
		final Project project2 = ProjectTestDataInitializer.createProject();
		project2.setProjectName(DUMMY_PROJECT_NAME + "2");
		final Project project3 = ProjectTestDataInitializer.createProject();
		project3.setProjectName(DUMMY_PROJECT_NAME + "3");
		final List<Project> projects = Arrays.asList(project, project2, project3);
		this.installationDirUtil.resetWorkspaceDirectoryForCrop(cropType, projects);
		
		// Folders for crop and new programs should have been generated
		Assert.assertTrue(cropWorkspaceDirectory.exists());
		for (final Project newProject : projects) {
			final File newProjectWorkspaceDirectory = new File(
					InstallationDirectoryUtil.WORKSPACE_DIR + File.separator + cropType.getCropName(), newProject.getProjectName());
			Assert.assertTrue(newProjectWorkspaceDirectory.exists());
			this.verifyProjectFolderSubdirectories(newProjectWorkspaceDirectory, true);
		}
		
	}

	private void deleteTestInstallationDirectory() {
		// Delete test installation directory and its contents as part of cleanup
		final File testInstallationDirectory = new File(InstallationDirectoryUtil.WORKSPACE_DIR);
		this.installationDirUtil.recursiveFileDelete(testInstallationDirectory);
	}
	
	@Test
	public void testGetInputDirectoryForTool() {
		try {
			final String inputDirectory = this.installationDirUtil.getInputDirectoryForProjectAndTool(project, ToolName.BREEDING_VIEW);
			Assert.assertNotNull(inputDirectory);
			Assert.assertEquals(new File(InstallationDirectoryUtil.WORKSPACE_DIR + File.separator
					+ project.getCropType().getCropName() + File.separator + this.project.getProjectName() + File.separator + ToolName.BREEDING_VIEW.getName() + File.separator
					+ InstallationDirectoryUtil.INPUT).getAbsolutePath(), new File(inputDirectory).getAbsolutePath());

		} catch (final IllegalStateException e) {
			Assert.fail("There should be no exception thrown");
		}
	}
	
	@Test
	public void testGetOutputDirectoryForTool() {
		try {
			final String outputDirectory = this.installationDirUtil.getOutputDirectoryForProjectAndTool(project, ToolName.BREEDING_VIEW);
			Assert.assertNotNull(outputDirectory);
			Assert.assertEquals(new File(InstallationDirectoryUtil.WORKSPACE_DIR + File.separator
					+ project.getCropType().getCropName() + File.separator + this.project.getProjectName() + File.separator + ToolName.BREEDING_VIEW.getName() + File.separator
					+ InstallationDirectoryUtil.OUTPUT).getAbsolutePath(), new File(outputDirectory).getAbsolutePath());

		} catch (final IllegalStateException e) {
			Assert.fail("There should be no exception thrown");
		}
	}
	
	@Test
	public void testGetTempFileInOutputDirectoryForProjectAndTool() {
		try {
			final String tempFilePath = this.installationDirUtil.getTempFileInOutputDirectoryForProjectAndTool(TEMP_FILENAME, XLS_EXTENSION, project, ToolName.FIELDBOOK_WEB);
			final String outputDirectory = this.installationDirUtil.getOutputDirectoryForProjectAndTool(project, ToolName.FIELDBOOK_WEB);
			final File outputDirectoryFile = new File(outputDirectory);
			Assert.assertTrue(outputDirectoryFile.exists());
			Assert.assertFalse(outputDirectory.isEmpty());
			Assert.assertEquals(1, outputDirectoryFile.list().length);
			Assert.assertEquals(outputDirectory + File.separator + outputDirectoryFile.list()[0], tempFilePath);
			final File tempFile = new File(tempFilePath);
			Assert.assertTrue(tempFile.exists());
			Assert.assertTrue(tempFile.getName().startsWith(TEMP_FILENAME));

		} catch (final IOException e) {
			Assert.fail("There should be no exception thrown");
		}
	}
	
	@Test
	public void testGetTempFileInOutputDirectoryWhenPrefixIsLessThan3Characters() {
		try {
			final String prefix = "AB";
			final String tempFilePath = this.installationDirUtil.getTempFileInOutputDirectoryForProjectAndTool(prefix, XLS_EXTENSION, project, ToolName.FIELDBOOK_WEB);
			final String outputDirectory = this.installationDirUtil.getOutputDirectoryForProjectAndTool(project, ToolName.FIELDBOOK_WEB);
			final File outputDirectoryFile = new File(outputDirectory);
			Assert.assertTrue(outputDirectoryFile.exists());
			Assert.assertFalse(outputDirectory.isEmpty());
			Assert.assertEquals(1, outputDirectoryFile.list().length);
			Assert.assertEquals(outputDirectory + File.separator + outputDirectoryFile.list()[0], tempFilePath);
			final File tempFile = new File(tempFilePath);
			Assert.assertTrue(tempFile.exists());
			// Check that "temp" was used as prefix in place of original prefix since it was too short
			Assert.assertTrue(tempFile.getName().startsWith(InstallationDirectoryUtil.TEMP));

		} catch (final IOException | IllegalArgumentException e) {
			Assert.fail("There should be no exception thrown");
		}
	}
	
	@Test
	public void testGetFileInTemporaryDirectoryForProjectAndTool() {
		try {
			final String tempFilePath = this.installationDirUtil.getFileInTemporaryDirectoryForProjectAndTool(TEMP_FILENAME +  XLS_EXTENSION, project, ToolName.STUDY_BROWSER);
			final File toolDirectoryFile = this.installationDirUtil.getToolDirectoryForProject(project, ToolName.STUDY_BROWSER);
			Assert.assertTrue(toolDirectoryFile.exists());
			Assert.assertEquals(1, toolDirectoryFile.list().length);
			String outputTempDirectory = InstallationDirectoryUtil.OUTPUT;
			for (final File file : toolDirectoryFile.listFiles()) {
				if (file.getName().startsWith("output") && file.isDirectory()) {
					outputTempDirectory = file.getName();
				}
			}
			Assert.assertEquals(toolDirectoryFile.getAbsolutePath() + File.separator + outputTempDirectory + File.separator + TEMP_FILENAME
					+ XLS_EXTENSION, tempFilePath);
		} catch (final IOException e) {
			Assert.fail("There should be no exception thrown");
		}
	}
	

}
