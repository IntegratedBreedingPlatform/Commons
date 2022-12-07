
package org.generationcp.commons.exceptions;

public class BreedingViewImportException extends Exception {

	private static final long serialVersionUID = -1639961960516233500L;

	private String messageKey;
	private Object[] messageParameters;

	public BreedingViewImportException() {
		super("Error with importing breeding view output file.");
	}

	public BreedingViewImportException(final String message) {
		super(message);
	}

	public BreedingViewImportException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public BreedingViewImportException(final String message, final String messageKey, final Object... messageParameters) {
		super(message);
		this.messageKey = messageKey;
		this.messageParameters = messageParameters;
	}

	public String getMessageKey() {
		return this.messageKey;
	}

	public Object[] getMessageParameters() {
		return this.messageParameters;
	}
}
