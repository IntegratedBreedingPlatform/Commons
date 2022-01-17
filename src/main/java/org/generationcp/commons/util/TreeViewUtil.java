/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package org.generationcp.commons.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.pojo.treeview.TreeTableNode;
import org.generationcp.commons.pojo.treeview.TypeAheadSearchTreeNode;
import org.generationcp.commons.workbook.generator.RowColumnType;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.oms.PropertyReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TraitClassReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListMetadata;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class TreeViewUtil.
 */
public class TreeViewUtil {

	private static final Logger LOG = LoggerFactory.getLogger(TreeViewUtil.class);

	private TreeViewUtil() {

	}

	public static List<TreeTableNode> convertToTableNode(List<TreeNode> treeNodes) {
		List<TreeTableNode> tableNodes = new ArrayList<>();
		for (TreeNode treeNode : treeNodes) {
			tableNodes.add(convertToTableNode(treeNode));
		}
		return tableNodes;
	}

	public static TreeTableNode convertToTableNode(TreeNode treeNode) {
		TreeTableNode tableNode = new TreeTableNode();
		tableNode.setId(treeNode.getKey());
		tableNode.setName(treeNode.getTitle());
		tableNode.setOwner(treeNode.getOwner());
		tableNode.setDescription(treeNode.getDescription());
		tableNode.setType(treeNode.getType());
		tableNode.setIsFolder(treeNode.getIsFolder());
		tableNode.setParentId(treeNode.getParentId());
		tableNode.setNumOfChildren(String.valueOf(treeNode.getNumOfChildren()));
		tableNode.setNoOfEntries(treeNode.getNoOfEntries() != null ? String.valueOf(treeNode.getNoOfEntries()) : "");
		return tableNode;
	}

	/**
	 * Convert references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertReferencesToJson(final List<Reference> references) {
		final List<TreeNode> treeNodes = TreeViewUtil.convertReferencesToTreeView(references);
		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert folder references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertStudyFolderReferencesToJson(final List<Reference> references, final boolean isLazy,
			final boolean isFolderOnly) {
		final List<TreeNode> treeNodes = TreeViewUtil.convertStudyFolderReferencesToTreeView(references, isLazy, isFolderOnly);
		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert folder references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertFolderReferencesToJson(final List<FolderReference> references, final boolean isLazy) {
		final List<TreeNode> treeNodes = TreeViewUtil.convertFolderReferencesToTreeView(references, isLazy);
		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert dataset references to json.
	 *
	 * @param references the references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertDatasetReferencesToJson(final List<DatasetReference> references) {
		final List<TreeNode> treeNodes = TreeViewUtil.convertDatasetReferencesToTreeView(references);
		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert references to tree view.
	 *
	 * @param references the references
	 * @return the list
	 */
	private static List<TreeNode> convertReferencesToTreeView(final List<Reference> references) {
		final List<TreeNode> treeNodes = new ArrayList<>();
		if (references != null && !references.isEmpty()) {
			for (final Reference reference : references) {
				treeNodes.add(TreeViewUtil.convertReferenceToTreeNode(reference));
			}
		}
		return treeNodes;
	}

	/**
	 * Convert folder references to tree view.
	 *
	 * @param references the references
	 * @return the list
	 */
	private static List<TreeNode> convertFolderReferencesToTreeView(final List<FolderReference> references, final boolean isLazy) {
		final List<TreeNode> treeNodes = new ArrayList<>();
		TreeNode treeNode;
		if (references != null && !references.isEmpty()) {
			for (final FolderReference reference : references) {
				treeNode = TreeViewUtil.convertReferenceToTreeNode(reference);
				treeNode.setIsLazy(isLazy);
				treeNodes.add(treeNode);
				if (reference.getSubFolders() != null && !reference.getSubFolders().isEmpty()) {
					treeNode.setChildren(TreeViewUtil.convertFolderReferencesToTreeView(reference.getSubFolders(), isLazy));
				} else {
					treeNode.setIsFolder(false);
				}
			}
		}
		return treeNodes;
	}

	@Deprecated
	public static List<TreeNode> convertStudyFolderReferencesToTreeView(final List<Reference> references, final boolean isLazy,
			final boolean isFolderOnly) {
		final List<TreeNode> treeNodes = new ArrayList<>();
		if (references != null && !references.isEmpty()) {
			for (final Reference reference : references) {
				// isFolderOnly comes all the way from UI, to display nodes that can contain children (e.g saving, drag-drop)
				// TODO Move logic to the frontend and remove parameter
				if (reference.isStudy() && isFolderOnly) {
					continue;
				}

				final TreeNode treeNode = TreeViewUtil.convertStudyFolderReferenceToTreeNode(reference);
				treeNode.setIsLazy(isLazy);
				treeNodes.add(treeNode);
			}
		}
		return treeNodes;
	}

	/**
	 * TODO merge with {@link #convertStudyFolderReferencesToTreeView(List, boolean, boolean)} after removing parameter isFolderOnly
	 */
	public static List<TreeNode> convertStudyFolderReferencesToTreeView(final List<Reference> references, final boolean isLazy) {
		return convertStudyFolderReferencesToTreeView(references, isLazy, false);
	}


	/**
	 * Convert dataset references to tree view.
	 *
	 * @param references the references
	 * @return the list
	 */
	private static List<TreeNode> convertDatasetReferencesToTreeView(final List<DatasetReference> references) {
		final List<TreeNode> treeNodes = new ArrayList<>();
		if (references != null && !references.isEmpty()) {
			for (final DatasetReference reference : references) {
				treeNodes.add(TreeViewUtil.convertReferenceToTreeNode(reference));
			}
		}
		return treeNodes;
	}

	/**
	 * Convert germplasm list to tree view.
	 *
	 * @param germplasmLists the germplasm lists
	 * @return the list
	 */
	public static List<TreeNode> convertGermplasmListToTreeView(final List<GermplasmList> germplasmLists, final boolean isFolderOnly, final List<UserDefinedField> listTypes) {
		final List<TreeNode> treeNodes = new ArrayList<>();
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			for (final GermplasmList germplasmList : germplasmLists) {
				final TreeNode node = TreeViewUtil.convertGermplasmListToTreeNode(germplasmList, isFolderOnly, listTypes);
				if (node != null) {
					treeNodes.add(node);
				}
			}
		}
		return treeNodes;
	}

	/**
	 * Convert sample list to tree view.
	 *
	 * @param sampleLists the sample lists
	 * @return the list
	 */
	public static List<TreeNode> convertListToTreeView(final List<SampleList> sampleLists, final boolean isFolderOnly) {
		final List<TreeNode> treeNodes = new ArrayList<>();
		if (sampleLists != null && !sampleLists.isEmpty()) {
			for (final SampleList sampleList : sampleLists) {
				final TreeNode node = TreeViewUtil.convertListToTreeNode(sampleList, isFolderOnly);
				if (node != null) {
					treeNodes.add(node);
				}
			}
		}
		return treeNodes;
	}

	/**
	 * Convert list of germplasmList to tree table nodes.
	 *
	 * @param germplasmLists the germplasm lists
	 * @return the list
	 */
	public static List<TreeTableNode> convertGermplasmListToTreeTableNodes(final String parentId, final List<GermplasmList> germplasmLists,
			final GermplasmListManager germplasmListManager, final GermplasmDataManager germplasmDataManager) {
		final List<TreeTableNode> treeTableNodes = new ArrayList<>();
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			germplasmListManager.populateGermplasmListCreatedByName(germplasmLists);
			final List<UserDefinedField> listTypes = germplasmDataManager
					.getUserDefinedFieldByFieldTableNameAndType(RowColumnType.LIST_TYPE.getFtable(), RowColumnType.LIST_TYPE.getFtype());
			final Map<Integer, ListMetadata> allListMetaData = germplasmListManager.getGermplasmListMetadata(germplasmLists);
			for (final GermplasmList germplasmList : germplasmLists) {
				final TreeTableNode node = TreeViewUtil
						.convertGermplasmListToTreeTableNode(parentId, germplasmList, listTypes, allListMetaData.get(germplasmList.getId()));
				if (node != null) {
					treeTableNodes.add(node);
				}
			}
		}
		return treeTableNodes;
	}

	private static String getDescriptionForDisplay(final GermplasmList germplasmList) {
		String description = "-";
		if (germplasmList != null && germplasmList.getDescription() != null && germplasmList.getDescription().length() != 0) {
			description = germplasmList.getDescription().replaceAll("<", "&lt;");
			description = description.replaceAll(">", "&gt;");
			if (description.length() > 27) {
				description = description.substring(0, 27) + "...";
			}
		}
		return description;
	}

	/**
	 * Convert reference to tree node.
	 *
	 * @param reference the reference
	 * @return the tree node
	 */
	private static TreeNode convertReferenceToTreeNode(final Reference reference) {
		final TreeNode treeNode = new TreeNode();

		treeNode.setKey(reference.getId().toString());
		treeNode.setTitle(reference.getName());
		treeNode.setIsFolder(reference instanceof DatasetReference ? false : true);
		treeNode.setIsLazy(true);
		treeNode.setProgramUUID(reference.getProgramUUID());

		return treeNode;
	}

	/**
	 * Convert reference to tree node.
	 *
	 * @param reference the reference
	 * @return the tree node
	 */
	private static TreeNode convertStudyFolderReferenceToTreeNode(final Reference reference) {
		final TreeNode treeNode = new TreeNode();
		treeNode.setKey(reference.getId().toString());
		treeNode.setTitle(reference.getName());
		final boolean isFolder = reference.isFolder();
		treeNode.setIsFolder(isFolder);
		treeNode.setIsLazy(true);
		treeNode.setProgramUUID(reference.getProgramUUID());
		if (isFolder) {
			treeNode.setIcon(AppConstants.FOLDER_ICON_PNG.getString());
		} else {
			treeNode.setIcon(AppConstants.STUDY_ICON_PNG.getString());
			final StudyReference studyReference = (StudyReference) reference;
			treeNode.setType(studyReference.getStudyType().getName());
			treeNode.setIsLocked(studyReference.getIsLocked());
			treeNode.setOwnerId(String.valueOf(studyReference.getOwnerId()));
			treeNode.setOwner(studyReference.getOwnerName());
		}
		return treeNode;
	}

	/**
	 * Convert sample list to tree node.
	 *
	 * @param sampleList the Sample list
	 * @return the tree node
	 */
	private static TreeNode convertListToTreeNode(final SampleList sampleList, final boolean isFolderOnly) {
		final TreeNode treeNode = new TreeNode();

		treeNode.setKey(sampleList.getId().toString());
		treeNode.setTitle(sampleList.getListName());
		treeNode.setIsFolder(sampleList.isFolder());
		treeNode.setType(sampleList.getType().getDisplayName());
		treeNode.setDescription(sampleList.getDescription());
		if (sampleList.getCreatedBy() != null) {
			treeNode.setOwner(sampleList.getCreatedBy());
			treeNode.setOwnerId(String.valueOf(sampleList.getCreatedByUserId()));
		}
		treeNode.setIsLazy(false);
		if (treeNode.getIsFolder()) {
			treeNode.setIcon(AppConstants.FOLDER_ICON_PNG.getString());
		} else {
			treeNode.setIcon(AppConstants.BASIC_DETAILS_PNG.getString());
		}
		if (isFolderOnly && !treeNode.getIsFolder()) {
			return null;
		}

		return treeNode;
	}

	/**
	 * Convert germplasm list to tree node.
	 *
	 * @param germplasmList the germplasm list
	 * @return the tree node
	 */
	private static TreeNode convertGermplasmListToTreeNode(final GermplasmList germplasmList, final boolean isFolderOnly, final List<UserDefinedField> listTypes) {
		final TreeNode treeNode = new TreeNode();

		treeNode.setKey(germplasmList.getId().toString());
		treeNode.setTitle(germplasmList.getName());
		treeNode.setIsFolder(germplasmList.getType() != null && "FOLDER".equals(germplasmList.getType()) ? true : false);
		treeNode.setIsLazy(false);
		if (treeNode.getIsFolder()) {
			treeNode.setIcon(AppConstants.FOLDER_ICON_PNG.getString());
		} else {
			treeNode.setIcon(AppConstants.BASIC_DETAILS_PNG.getString());
		}
		if (isFolderOnly && !treeNode.getIsFolder()) {
			return null;
		}
		treeNode.setDescription(germplasmList.getDescription());
		treeNode.setOwnerId((germplasmList.getUserId() != null) ? String.valueOf(germplasmList.getUserId()) : null);
		treeNode.setOwner(germplasmList.getCreatedBy());
		treeNode.setIsLocked(germplasmList.isLockedList());
		treeNode.setType(TreeViewUtil.getTypeString(germplasmList.getType(), listTypes));

		return treeNode;
	}

	/**
	 * Convert germplasm list to tree node.
	 *
	 * @param germplasmList           the germplasm list
	 * @return the tree node
	 */
	private static TreeTableNode convertGermplasmListToTreeTableNode(final String parentFolderId, final GermplasmList germplasmList,
			final List<UserDefinedField> listTypes, final ListMetadata listMetaData) {
		final TreeTableNode treeTableNode = new TreeTableNode();

		treeTableNode.setId(germplasmList.getId().toString());
		treeTableNode.setName(germplasmList.getName());
		treeTableNode.setDescription(TreeViewUtil.getDescriptionForDisplay(germplasmList));
		treeTableNode.setType(TreeViewUtil.getTypeString(germplasmList.getType(), listTypes));
		treeTableNode.setOwner(germplasmList.getCreatedBy());

		treeTableNode.setIsFolder(germplasmList.isFolder());

		if (listMetaData != null) {
			treeTableNode.setNoOfEntries((listMetaData.getNumberOfEntries()!=null && listMetaData.getNumberOfEntries()!=0 )?String.valueOf(listMetaData.getNumberOfEntries()):"");
			treeTableNode.setNumOfChildren((listMetaData.getNumberOfChildren() != null) ? String.valueOf(listMetaData.getNumberOfChildren()) : "0");
		} else {
			treeTableNode.setNoOfEntries("");
			treeTableNode.setNumOfChildren("0");
		}
		treeTableNode.setParentId(TreeViewUtil.getParentId(parentFolderId, germplasmList));
		return treeTableNode;
	}

	protected static String getParentId(final String parentFolderId, final GermplasmList germplasmList) {
		if (germplasmList.getParentId() == null) {
			return parentFolderId;
		}
		return String.valueOf(parentFolderId);
	}

	private static String getTypeString(final String typeCode, final List<UserDefinedField> listTypes) {
		final String type = "Germplasm List";
		if (typeCode == null) {
			return type;
		}
		try {
			for (final UserDefinedField listType : listTypes) {
				if (typeCode.equals(listType.getFcode())) {
					return listType.getFname();
				}
			}
		} catch (final MiddlewareQueryException ex) {
			TreeViewUtil.LOG.error("Error in getting list types.", ex);
			return "";
		}
		return type;
	}

	/**
	 * Convert tree view to json.
	 *
	 * @param treeNodes the tree nodes
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertTreeViewToJson(final List<TreeNode> treeNodes) {
		if (treeNodes != null && !treeNodes.isEmpty()) {
			final ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.writeValueAsString(treeNodes);
			} catch (final IOException e) {
				throw new IllegalStateException(
						"Error converting tree node to JSON. " + "Please contact administrator for further assistance.", e);
			}
		}
		return "[]";
	}

	/**
	 * Convert search tree view to json.
	 *
	 * @param treeNodes the tree nodes
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertSearchTreeViewToJson(final List<TypeAheadSearchTreeNode> treeNodes) throws IOException {
		if (treeNodes != null && !treeNodes.isEmpty()) {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(treeNodes);
		}
		return "[]";
	}

	// for the ontology Browser

	/**
	 * Convert ontology traits to search single level json.
	 *
	 * @param traitClassReferences the trait references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertOntologyTraitsToSearchSingleLevelJson(final List<TraitClassReference> traitClassReferences,
			final Map<String, StandardVariableReference> mapVariableRef) throws IOException {
		return TreeViewUtil.convertSearchTreeViewToJson(TreeViewUtil.getTypeAheadTreeNodes("", traitClassReferences, mapVariableRef));
	}

	private static List<TypeAheadSearchTreeNode> getTypeAheadTreeNodes(final String parentId,
			final List<TraitClassReference> traitClassReferences, final Map<String, StandardVariableReference> mapVariableRef) {
		final List<TypeAheadSearchTreeNode> treeNodes = new ArrayList<>();

		if (traitClassReferences != null && !traitClassReferences.isEmpty()) {
			for (final TraitClassReference reference : traitClassReferences) {
				// this is for the inner trait classes
				if (reference.getTraitClassChildren() != null && !reference.getTraitClassChildren().isEmpty()) {
					String newParentId = "";
					if (parentId != null && !"".equals(parentId)) {
						newParentId = parentId + "_";
					}
					newParentId = newParentId + reference.getId().toString();
					treeNodes.addAll(TreeViewUtil.getTypeAheadTreeNodes(newParentId, reference.getTraitClassChildren(), mapVariableRef));
				}

				final List<PropertyReference> propRefList = reference.getProperties();
				for (final PropertyReference propRef : propRefList) {
					final List<StandardVariableReference> variableRefList = propRef.getStandardVariables();
					final String parentTitle = reference.getName();
					String key = reference.getId().toString() + "_" + propRef.getId().toString();

					if (parentId != null && !"".equals(parentId)) {
						key = parentId + "_" + key;
					}

					final List<String> token = new ArrayList<>();
					token.add(propRef.getName());
					final TypeAheadSearchTreeNode searchTreeNode =
							new TypeAheadSearchTreeNode(key, token, propRef.getName(), parentTitle, "Property");
					treeNodes.add(searchTreeNode);

					for (final StandardVariableReference variableRef : variableRefList) {
						boolean addVariableToSearch = true;
						if (mapVariableRef != null && !mapVariableRef.isEmpty()) {
							// we only show variables that are in the map
							if (mapVariableRef.containsKey(variableRef.getId().toString())) {
								addVariableToSearch = true;
							} else {
								addVariableToSearch = false;
							}
						}

						if (addVariableToSearch) {
							final String varParentTitle = reference.getName() + " > " + propRef.getName();
							final String varKey = key + "_" + variableRef.getId().toString();
							final List<String> varToken = new ArrayList<>();
							varToken.add(variableRef.getName());
							final TypeAheadSearchTreeNode varSearchTreeNode =
									new TypeAheadSearchTreeNode(varKey, varToken, variableRef.getName(), varParentTitle,
											"Standard Variable");
							treeNodes.add(varSearchTreeNode);
						}
					}
				}

			}
		}

		return treeNodes;
	}

	/**
	 * Convert ontology traits to json.
	 *
	 * @param traitClassReferences the trait references
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String convertOntologyTraitsToJson(final List<TraitClassReference> traitClassReferences,
			final Map<String, StandardVariableReference> mapVariableRef) {

		final List<TreeNode> treeNodes = TreeViewUtil.convertTraitClassReferencesToTreeView(traitClassReferences, mapVariableRef);

		return TreeViewUtil.convertTreeViewToJson(treeNodes);
	}

	/**
	 * Convert trait references to tree view.
	 *
	 * @param traitClassReferences the trait references
	 * @return the list
	 */
	private static List<TreeNode> convertTraitClassReferencesToTreeView(final List<TraitClassReference> traitClassReferences,
			final Map<String, StandardVariableReference> mapVariableRef) {
		final List<TreeNode> treeNodes = new ArrayList<>();
		if (traitClassReferences != null && !traitClassReferences.isEmpty()) {
			for (final TraitClassReference reference : traitClassReferences) {
				treeNodes.add(TreeViewUtil.convertTraitClassReferenceToTreeNode("", reference, mapVariableRef));
			}
		}
		return treeNodes;
	}

	/**
	 * Convert trait reference to tree node.
	 *
	 * @param reference the reference
	 * @return the tree node
	 */
	private static TreeNode convertTraitClassReferenceToTreeNode(final String parentParentId, final TraitClassReference reference,
			final Map<String, StandardVariableReference> mapVariableRef) {
		final TreeNode treeNode = new TreeNode();
		String parentId = reference.getId().toString();
		if (parentParentId != null && !"".equals(parentParentId)) {
			parentId = parentParentId + "_" + parentId;
		}
		treeNode.setKey(parentId);
		treeNode.setAddClass(parentId);
		treeNode.setTitle(reference.getName());
		treeNode.setIsFolder(true);
		treeNode.setIsLazy(false);
		treeNode.setIcon(false);
		treeNode.setIncludeInSearch(false);

		final List<TreeNode> treeNodes = new ArrayList<>();

		// this is for the inner trait classes
		if (reference.getTraitClassChildren() != null && !reference.getTraitClassChildren().isEmpty()) {
			for (final TraitClassReference childTrait : reference.getTraitClassChildren()) {
				treeNodes.add(TreeViewUtil.convertTraitClassReferenceToTreeNode(parentId, childTrait, mapVariableRef));
			}
		}
		// we need to set the children for the property

		if (reference.getProperties() != null && !reference.getProperties().isEmpty()) {
			for (final PropertyReference propRef : reference.getProperties()) {
				treeNodes.add(TreeViewUtil.convertPropertyReferenceToTreeNode(parentId, propRef, reference.getName(), mapVariableRef));
			}

		}
		treeNode.setChildren(treeNodes);

		return treeNode;
	}

	/**
	 * Convert property reference to tree node.
	 *
	 * @param parentId    the parent id
	 * @param reference   the reference
	 * @param parentTitle the parent title
	 * @return the tree node
	 */
	private static TreeNode convertPropertyReferenceToTreeNode(final String parentId, final PropertyReference reference,
			final String parentTitle, final Map<String, StandardVariableReference> mapVariableRef) {
		final TreeNode treeNode = new TreeNode();
		final String id = parentId + "_" + reference.getId().toString();
		treeNode.setKey(id);
		treeNode.setAddClass(id);
		treeNode.setTitle(reference.getName());
		treeNode.setIsFolder(true);
		treeNode.setIsLazy(false);
		treeNode.setIcon(false);
		treeNode.setIncludeInSearch(true);
		final String newParentTitle = parentTitle + " > " + reference.getName();
		treeNode.setParentTitle(newParentTitle);
		// we need to set the children for the property
		final List<TreeNode> treeNodes = new ArrayList<>();
		if (reference.getStandardVariables() != null && !reference.getStandardVariables().isEmpty()) {
			for (final StandardVariableReference variableRef : reference.getStandardVariables()) {
				final TreeNode variableTreeNode =
						TreeViewUtil.convertStandardVariableReferenceToTreeNode(id, variableRef, newParentTitle, mapVariableRef);
				if (variableTreeNode != null) {
					treeNodes.add(variableTreeNode);
				}
			}

		}
		treeNode.setChildren(treeNodes);

		return treeNode;
	}

	/**
	 * Convert standard variable reference to tree node.
	 *
	 * @param parentId    the parent id
	 * @param reference   the reference
	 * @param parentTitle the parent title
	 * @return the tree node
	 */
	private static TreeNode convertStandardVariableReferenceToTreeNode(final String parentId, final StandardVariableReference reference,
			final String parentTitle, final Map<String, StandardVariableReference> mapVariableRef) {

		if (mapVariableRef != null && !mapVariableRef.isEmpty() && !mapVariableRef.containsKey(reference.getId().toString())) {
			return null;
		}

		final TreeNode treeNode = new TreeNode();
		final String id = parentId + "_" + reference.getId().toString();
		treeNode.setKey(id);
		treeNode.setAddClass(id);
		treeNode.setTitle(reference.getName());
		treeNode.setIsFolder(false);
		treeNode.setIsLazy(false);
		treeNode.setLastChildren(true);
		treeNode.setIcon(false);
		treeNode.setIncludeInSearch(true);
		final String newParentTitle = parentTitle + " > " + reference.getName();
		treeNode.setParentTitle(newParentTitle);
		// we need to set the children for the property
		final List<TreeNode> treeNodes = new ArrayList<>();
		treeNode.setChildren(treeNodes);

		return treeNode;
	}
}
