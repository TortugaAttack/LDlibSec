package org.ldlibsec.anonymization.rdf.evaluation.limes;

import org.aksw.limes.core.io.config.Configuration;

public class BenchmarkConfig {

    public static final Configuration SIMPLE_BENCHMARK = createConfiguration("");

    private static Configuration createConfiguration(String metric) {
        Configuration base = new Configuration();
        base.setMetricExpression(metric);
        return base;
    }
}
