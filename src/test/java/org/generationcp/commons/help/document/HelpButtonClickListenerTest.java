
package org.generationcp.commons.help.document;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.TooLittleActualInvocations;

import java.net.MalformedURLException;
import java.util.Properties;

public class HelpButtonClickListenerTest {

	private static final String INSTALLATION_PATH = "C:\\BMS 4";

	private HelpButtonClickListener listener;
	private HelpModule link;

	@Mock
	private Properties helpProperties;

	@Mock
	private ClickEvent event;
	@Mock
	private Component component;
	@Mock
	private Window window;
	@Mock
	private Component parentComponent;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.link = HelpModule.MANAGE_LIST;
		this.listener = new HelpButtonClickListener(this.link);
		this.listener.setHelpProperties(this.helpProperties);

		Mockito.when(this.event.getComponent()).thenReturn(this.component);
		Mockito.when(this.component.getWindow()).thenReturn(this.window);
		Mockito.when(this.component.getParent()).thenReturn(this.parentComponent);
		Mockito.when(this.parentComponent.getWindow()).thenReturn(this.window);
	}

	@Test
	public void testButtonClick_ForOnlineLink() throws MalformedURLException {
		final String onlineLink = "www.google.com";
		Mockito.doReturn(onlineLink).when(this.helpProperties).getProperty(this.link.getPropertyName());

		final ArgumentCaptor<ExternalResource> resourceCaptor = ArgumentCaptor.forClass(ExternalResource.class);
		this.listener.buttonClick(this.event);

		try {
			Mockito.verify(this.window, Mockito.times(1)).open(resourceCaptor.capture(), ArgumentMatchers.eq(" _BLANK"));
			Assert.assertEquals("https://" + onlineLink, resourceCaptor.getValue().getURL());
		} catch (final TooLittleActualInvocations e) {
			Assert.fail("Expecting to call the method that opens an online link.");
		}
	}

}
