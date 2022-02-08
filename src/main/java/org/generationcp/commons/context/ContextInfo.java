
package org.generationcp.commons.context;

import java.util.Objects;

/**
 * POJO used to expose context information (typically from the Workbench).
 *
 * @author Naymesh Mistry
 */
public class ContextInfo {

	private final Integer loggedInUserId;
	private final Long selectedProjectId;
	private final boolean showReleaseNotes;

	public ContextInfo(final Integer userId, final Long selectedProjectId, final boolean showReleaseNotes) {
		this.loggedInUserId = userId;
		this.selectedProjectId = selectedProjectId;
		this.showReleaseNotes = showReleaseNotes;
	}

	public ContextInfo(final Integer loggedInUserId, final Long selectedProjectId) {
		this(loggedInUserId, selectedProjectId, false);
	}

	public Integer getLoggedInUserId() {
		return this.loggedInUserId;
	}

	public Long getSelectedProjectId() {
		return this.selectedProjectId;
	}

	public boolean shouldShowReleaseNotes() {
		return this.showReleaseNotes;
	}

	@Override
	public boolean equals(final Object o) {

		if (o == this) {
			return true;
		}

		if (!(o instanceof ContextInfo)) {
			return false;
		}

		final ContextInfo other = (ContextInfo) o;
		return Objects.equals(this.loggedInUserId, other.loggedInUserId)
			&& Objects.equals(this.loggedInUserId, other.selectedProjectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.loggedInUserId, this.loggedInUserId);
	}
}
