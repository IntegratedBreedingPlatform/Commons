/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.commons.pojo.treeview;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the data needed for rendering a tree view using dynatree jquery.
 * TODO improvements:
 *  - merge {@link TreeTableNode} into this one
 *  - decouple with dynatree/frontend implementations: move to frontend
 *   - isLazy
 *   - icon
 *   - addClass
 *   - isLastChildren
 *  - Consolidate properties with the same amount of information:
 *   - noOfEntries, numOfChildren, children, expand
 *   - type, includeInSearch
 */
public class TreeNode {

	private String title;
	private String key;
	private String owner;
	private String ownerUserName;
	private String ownerId;
	private String description;
	private String type;
	private Integer noOfEntries;
	private boolean isFolder;
	private boolean isLazy;
	private String addClass;
	private int numOfChildren;
	private List<TreeNode> children;
	private boolean expand;
	private boolean isLastChildren;
	private String parentTitle;
	private String parentId;
	private boolean includeInSearch;
	private boolean isLocked;

	/**
	 * set icon to Boolean(false) to suppress icon. set icon to null to use default icon. set icon to an image file name relative the the
	 * image path to use a custom icon image.
	 */
	private Object icon;

	private String programUUID;

	/**
	 * Instantiates a new tree node.
	 */
	public TreeNode() {
	}

	/**
	 * Instantiates a new tree node.
	 *
	 * @param key the key
	 * @param title the title
	 * @param isFolder the is folder
	 * @param addClass the add class
	 * @param icon the icon
	 */
	public TreeNode(final String key, final String title, final boolean isFolder, final String addClass, final Object icon, final String programUUID) {
		this.key = key;
		this.title = title;
		this.isFolder = isFolder;
		this.addClass = addClass;
		this.icon = icon;
		this.isLazy = true;
		this.children = new ArrayList<TreeNode>();
		this.programUUID = programUUID;
	}

	public TreeNode(final String key, final String title, final boolean isFolder, final String programUUID) {
		this.key = key;
		this.title = title;
		this.isFolder = isFolder;
		this.children = new ArrayList<TreeNode>();
		this.programUUID = programUUID;
	}

	public boolean getIsLazy() {
		return this.isLazy;
	}

	public void setIsLazy(final boolean isLazy) {
		this.isLazy = isLazy;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public boolean getIsFolder() {
		return this.isFolder;
	}

	public void setIsFolder(final boolean isFolder) {
		this.isFolder = isFolder;
	}

	public String getAddClass() {
		return this.addClass;
	}

	public void setAddClass(final String addClass) {
		this.addClass = addClass;
	}

	public Object getIcon() {
		return this.icon;
	}

	public void setIcon(final Object icon) {
		this.icon = icon;
	}

	public List<TreeNode> getChildren() {
		return this.children;
	}

	public void setChildren(final List<TreeNode> children) {
		this.children = children;
	}

	public boolean isExpand() {
		return this.expand;
	}

	public void setExpand(final boolean expand) {
		this.expand = expand;
	}

	public boolean isLastChildren() {
		return this.isLastChildren;
	}

	public void setLastChildren(final boolean isLastChildren) {
		this.isLastChildren = isLastChildren;
	}

	public String getParentTitle() {
		return this.parentTitle;
	}

	public void setParentTitle(final String parentTitle) {
		this.parentTitle = parentTitle;
	}

	public String getParentId() {
		return this.parentId;
	}

	public void setParentId(final String parentId) {
		this.parentId = parentId;
	}

	public boolean isIncludeInSearch() {
		return this.includeInSearch;
	}

	public void setIncludeInSearch(final boolean includeInSearch) {
		this.includeInSearch = includeInSearch;
	}

	public String getProgramUUID() {
		return this.programUUID;
	}

	public void setProgramUUID(final String programUUID) {
		this.programUUID = programUUID;
	}

	public int getNumOfChildren() {
		return this.numOfChildren;
	}

	public void setNumOfChildren(final int numOfChildren) {
		this.numOfChildren = numOfChildren;
	}

	public void setNoOfEntries(final Integer noOfEntries) {
		this.noOfEntries = noOfEntries;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setOwner(final String owner) {
		this.owner = owner;
	}

	public String getOwnerUserName() {
		return ownerUserName;
	}

	public void setOwnerUserName(final String ownerUserName) {
		this.ownerUserName = ownerUserName;
	}

	public String getOwnerId() {
		return this.ownerId;
	}

	public void setOwnerId(final String ownerId) {
		this.ownerId = ownerId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getType() {
		return this.type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Integer getNoOfEntries() {
		return this.noOfEntries;
	}

	
	public boolean getIsLocked() {
		return this.isLocked;
	}

	
	public void setIsLocked(final boolean isLocked) {
		this.isLocked = isLocked;
	}

}
