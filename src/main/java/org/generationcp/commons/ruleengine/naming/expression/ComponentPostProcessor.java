package org.generationcp.commons.ruleengine.naming.expression;

import org.generationcp.commons.ruleengine.RulesPostProcessor;
import org.generationcp.commons.ruleengine.naming.expression.Expression;
import org.springframework.beans.BeansException;

import org.generationcp.commons.ruleengine.naming.impl.ProcessCodeFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Daniel Villafuerte on 6/15/2015.
 */
public class ComponentPostProcessor extends RulesPostProcessor{

    private ProcessCodeFactory processCodeFactory;

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        super.postProcessAfterInitialization(o, s);
        if (o instanceof Expression) {
            processCodeFactory.addExpression((Expression) o);
        }

        return o;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return super.postProcessBeforeInitialization(o, s);
    }

    @Autowired
    public void setProcessCodeFactory(ProcessCodeFactory processCodeFactory) {
        this.processCodeFactory = processCodeFactory;
    }
}