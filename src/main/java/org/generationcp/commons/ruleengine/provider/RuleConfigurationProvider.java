
package org.generationcp.commons.ruleengine.provider;

import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 2/14/2015 Time: 6:52 AM
 */
public interface RuleConfigurationProvider {

	public Map<String, String[]> getRuleSequenceConfiguration();
}
