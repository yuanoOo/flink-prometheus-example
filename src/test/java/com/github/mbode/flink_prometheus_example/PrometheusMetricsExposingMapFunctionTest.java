package com.github.mbode.flink_prometheus_example;

import static io.prometheus.client.CollectorRegistry.defaultRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.client.CollectorRegistry;
import org.apache.flink.configuration.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrometheusMetricsExposingMapFunctionTest {
  private static final Integer TEST_VALUE = 42;

  private final PrometheusMetricsExposingMapFunction prometheusMetricsExposingMapFunction =
      new PrometheusMetricsExposingMapFunction();

  @BeforeEach
  void clearRegistryAndInitializeMetrics() {
    CollectorRegistry.defaultRegistry.clear();
    prometheusMetricsExposingMapFunction.open(new Configuration());
  }

  @AfterEach
  void clearRegistry() {
    CollectorRegistry.defaultRegistry.clear();
  }

  @Test
  void mapActsAsIdentity() {
    assertThat(prometheusMetricsExposingMapFunction.map(TEST_VALUE)).isEqualTo(TEST_VALUE);
  }

  @Test
  void eventsAreCounted() {
    final Double before = defaultRegistry.getSampleValue("events_total");
    prometheusMetricsExposingMapFunction.map(TEST_VALUE);
    assertThat(defaultRegistry.getSampleValue("events_total") - before).isEqualTo(1);
  }

  @Test
  void valueIsReportedToHistogram() {
    final Double bucket25Before =
        defaultRegistry.getSampleValue("values_bucket", new String[] {"le"}, new String[] {"25.0"});
    final Double bucket50Before =
        defaultRegistry.getSampleValue("values_bucket", new String[] {"le"}, new String[] {"50.0"});
    final Double countBefore = defaultRegistry.getSampleValue("values_count");
    final Double sumBefore = defaultRegistry.getSampleValue("values_sum");

    prometheusMetricsExposingMapFunction.map(TEST_VALUE);

    assertThat(
            defaultRegistry.getSampleValue(
                    "values_bucket", new String[] {"le"}, new String[] {"25.0"})
                - bucket25Before)
        .isEqualTo(0);
    assertThat(
            defaultRegistry.getSampleValue(
                    "values_bucket", new String[] {"le"}, new String[] {"50.0"})
                - bucket50Before)
        .isEqualTo(1);
    assertThat(defaultRegistry.getSampleValue("values_count") - countBefore).isEqualTo(1);
    assertThat(defaultRegistry.getSampleValue("values_sum") - sumBefore)
        .isEqualTo(Integer.toUnsignedLong(TEST_VALUE));
  }
}
