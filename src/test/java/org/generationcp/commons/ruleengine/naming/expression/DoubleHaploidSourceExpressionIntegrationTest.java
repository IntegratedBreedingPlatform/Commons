package org.generationcp.commons.ruleengine.naming.expression;

import org.generationcp.commons.pojo.AdvancingSource;
import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.generationcp.middleware.service.impl.KeySequenceRegisterServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class DoubleHaploidSourceExpressionIntegrationTest extends IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(DoubleHaploidSourceExpressionIntegrationTest.class);

    @Test
    public void testDoubleHaploidApplyRuleWithMultipleThreads() throws ExecutionException, InterruptedException {

        final AdvancingSource source = new AdvancingSource();

        int threads = 10;
        List<Future<String>> resultingDesignations = new ArrayList<>();

        ExecutorService threadPool = Executors.newFixedThreadPool(threads);

        final KeySequenceRegisterService keySequenceRegisterService =
                new KeySequenceRegisterServiceImpl(this.sessionProvder);

        for (int i = 1; i <= threads; i++) {
            Future<String> result = threadPool.submit(new Callable<String>() {
                @Override
                public String call() {
                    final List<StringBuilder> values = new ArrayList<>();
                    values.add(new StringBuilder("WM14AST0001L@0[DHSOURCE]"));
                    final DoubleHaploidSourceExpression doubleHaploidSourceExpression = new DoubleHaploidSourceExpression();
                    doubleHaploidSourceExpression.setKeySequenceRegisterService(keySequenceRegisterService);

                    doubleHaploidSourceExpression.apply(values, source, null);
                    return values.get(0).toString();
                }
            });
            resultingDesignations.add(result);
        }

        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
        }

        Set<String> uniqueDesignationSet = new HashSet<>();
        for (Future<String> future : resultingDesignations) {
            String generatedDesignation = future.get();
            uniqueDesignationSet.add(generatedDesignation);
            LOG.info("Designation returned: {}.", generatedDesignation);
        }

        Assert.assertEquals("Each thread must return a unique designation.", threads, uniqueDesignationSet.size());
    }

}
