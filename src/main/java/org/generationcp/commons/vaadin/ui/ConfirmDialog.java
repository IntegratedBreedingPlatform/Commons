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

package org.generationcp.commons.vaadin.ui;

import java.io.Serializable;

import org.generationcp.commons.vaadin.theme.Bootstrap;

import com.vaadin.terminal.gwt.server.JsonPaintTarget;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

/**
 * Cyrus: I've added the ConfirmDialog source from the Vaadin plugins as the base vaadin did not include confirmation dialogs
 *
 *
 */
public class ConfirmDialog extends Window {

	private static final long serialVersionUID = -2363125714643244070L;

	public interface Factory extends Serializable {

		ConfirmDialog create(String windowCaption, String message, String okTitle, String cancelTitle);
	}

	static final String DEFAULT_WINDOW_CAPTION = "Confirm";
	static final String DEFAULT_CANCEL_CAPTION = "Cancel";
	static final String DEFAULT_OK_CAPTION = "Ok";

	public static final int CONTENT_TEXT_WITH_NEWLINES = -1;
	public static final int CONTENT_TEXT = Label.CONTENT_TEXT;
	public static final int CONTENT_PREFORMATTED = Label.CONTENT_PREFORMATTED;
	public static final int CONTENT_HTML = Label.CONTENT_RAW;
	public static final int CONTENT_DEFAULT = ConfirmDialog.CONTENT_TEXT_WITH_NEWLINES;

	/**
	 * Listener for dialog close events. Implement and register an instance of this interface to dialog to receive close events.
	 * 
	 * @author Sami Ekblad
	 * 
	 */
	public interface Listener extends Serializable {

		void onClose(ConfirmDialog dialog);
	}

	/**
	 * Default dialog factory.
	 * 
	 */
	private static ConfirmDialog.Factory factoryInstance;

	/**
	 * Get the ConfirmDialog.Factory used to create and configure the dialog.
	 *
	 * By default the {@link DefaultConfirmDialogFactory} is used.
	 *
	 * @return
	 */
	public static ConfirmDialog.Factory getFactory() {
		if (ConfirmDialog.factoryInstance == null) {
			ConfirmDialog.factoryInstance = new DefaultConfirmDialogFactory();
		}
		return ConfirmDialog.factoryInstance;
	}

	/**
	 * Set the ConfirmDialog.Factory used to create and configure the dialog.
	 *
	 * By default the {@link DefaultConfirmDialogFactory} is used.
	 *
	 * @return
	 */
	public static void setFactory(final ConfirmDialog.Factory newFactory) {
		ConfirmDialog.factoryInstance = newFactory;
	}

	/**
	 * Show a modal ConfirmDialog in a window.
	 * 
	 * @param parentWindow
	 * @param listener
	 */
	public static ConfirmDialog show(final Window parentWindow, final Listener listener) {
		return ConfirmDialog.show(parentWindow, null, null, null, null, listener);
	}

	/**
	 * Show a modal ConfirmDialog in a window.
	 * 
	 * @param parentWindow
	 * @param messageLabel
	 * @param listener
	 * @return
	 */
	public static ConfirmDialog show(final Window parentWindow, final String message, final Listener listener) {
		return ConfirmDialog.show(parentWindow, null, message, null, null, listener);
	}

	/**
	 * Show a modal ConfirmDialog in a window.
	 * 
	 * @param parentWindow Main level window.
	 * @param windowCaption Caption for the confirmation dialog window.
	 * @param message Message to display as window content.
	 * @param okCaption Caption for the ok button.
	 * @param cancelCaption Caption for cancel button.
	 * @param listener Listener for dialog result.
	 * @return
	 */
	public static ConfirmDialog show(final Window parentWindow, final String windowCaption, final String message, final String okCaption,
			final String cancelCaption, final Listener listener) {
		ConfirmDialog d = ConfirmDialog.getFactory().create(windowCaption, message, okCaption, cancelCaption);
		d.show(parentWindow, listener, true);
		return d;
	}

	/**
	 * Shows a modal ConfirmDialog in given window and executes Runnable if OK is chosen.
	 * 
	 * @param parentWindow Main level window.
	 * @param windowCaption Caption for the confirmation dialog window.
	 * @param message Message to display as window content.
	 * @param okCaption Caption for the ok button.
	 * @param cancelCaption Caption for cancel button.
	 * @param r Runnable to be run if confirmed
	 * @return
	 */
	public static ConfirmDialog show(final Window parentWindow, final String windowCaption, final String message, final String okCaption,
			final String cancelCaption, final Runnable r) {
		ConfirmDialog d = ConfirmDialog.getFactory().create(windowCaption, message, okCaption, cancelCaption);
		d.show(parentWindow, new Listener() {

			@Override
			public void onClose(ConfirmDialog dialog) {
				if (dialog.isConfirmed()) {
					r.run();
				}
			}
		}, true);
		return d;
	}

	private Listener confirmListener = null;
	private boolean isConfirmed = false;
	private Label messageLabel = null;
	private Button okBtn = null;
	private Button cancelBtn = null;
	private String originalMessageText;
	private int msgContentMode = ConfirmDialog.CONTENT_TEXT_WITH_NEWLINES;

	/**
	 * Show confirm dialog.
	 * 
	 * @param listener
	 */
	public final void show(final Window parentWindow, final Listener listener, final boolean modal) {
		this.confirmListener = listener;
		this.center();
		this.setModal(modal);
		parentWindow.addWindow(this);

		this.addStyleName(Bootstrap.WINDOW.CONFIRM.styleName());
	}

	/**
	 * Did the user confirm the dialog.
	 * 
	 * @return
	 */
	public final boolean isConfirmed() {
		return this.isConfirmed;
	}

	public final Listener getListener() {
		return this.confirmListener;
	}

	protected final void setOkButton(final Button okButton) {
		this.okBtn = okButton;
	}

	public final Button getOkButton() {
		return this.okBtn;
	}

	protected final void setCancelButton(final Button cancelButton) {
		this.cancelBtn = cancelButton;
	}

	public final Button getCancelButton() {
		return this.cancelBtn;
	}

	protected final void setMessageLabel(final Label message) {
		this.messageLabel = message;
	}

	public final void setMessage(final String message) {
		this.originalMessageText = message;
		this.messageLabel.setValue(ConfirmDialog.CONTENT_TEXT_WITH_NEWLINES == this.msgContentMode ? this.formatDialogMessage(message)
				: message);
	}

	public final String getMessage() {
		return this.originalMessageText;
	}

	public final int getContentMode() {
		return this.msgContentMode;
	}

	public final void setContentMode(final int contentMode) {
		this.msgContentMode = contentMode;
		this.messageLabel
				.setContentMode(contentMode == ConfirmDialog.CONTENT_TEXT_WITH_NEWLINES ? ConfirmDialog.CONTENT_TEXT : contentMode);
		this.messageLabel.setValue(contentMode == ConfirmDialog.CONTENT_TEXT_WITH_NEWLINES ? this
				.formatDialogMessage(this.originalMessageText) : this.originalMessageText);
	}

	/**
	 * Format the messageLabel by maintaining text only.
	 * 
	 * @param text
	 * @return
	 */
	protected final String formatDialogMessage(final String text) {
		return JsonPaintTarget.escapeXML(text).replaceAll("\n", "<br />");
	}

	/**
	 * Set the isConfirmed state.
	 * 
	 * Note: this should only be called internally by the listeners.
	 * 
	 * @param isConfirmed
	 */
	protected final void setConfirmed(final boolean confirmed) {
		this.isConfirmed = confirmed;
	}
}
