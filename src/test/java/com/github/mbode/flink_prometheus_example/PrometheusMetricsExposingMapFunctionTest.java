package com.github.mbode.flink_prometheus_example;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Objects;
import io.prometheus.client.CollectorRegistry;
import org.apache.flink.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PrometheusMetricsExposingMapFunctionTest {
  private static final Integer TEST_VALUE = 42;

  private final PrometheusMetricsExposingMapFunction prometheusMetricsExposingMapFunction =
      new PrometheusMetricsExposingMapFunction();

  @Before
  public void openOperator() {
    prometheusMetricsExposingMapFunction.open(new Configuration());
  }

  @After
  public void closeOperator() {
    prometheusMetricsExposingMapFunction.close();
  }

  @Test
  public void mapActsAsIdentity() {
    assertThat(prometheusMetricsExposingMapFunction.map(TEST_VALUE)).isEqualTo(TEST_VALUE);
  }

  @Test
  public void eventsAreCounted() {
    final double before = sample("events_total");
    prometheusMetricsExposingMapFunction.map(TEST_VALUE);
    assertThat(sample("events_total") - before).isEqualTo(1);
  }

  @Test
  public void valueIsReportedToHistogram() {
    final double beforeShouldContain = sampleHistogram("50.0");
    final double beforeShouldNotContain = sampleHistogram("25.0");
    final double countBefore = sample("values_count");
    final double sumBefore = sample("values_sum");

    prometheusMetricsExposingMapFunction.map(TEST_VALUE);

    assertThat(sampleHistogram("50.0") - beforeShouldContain).isEqualTo(1);
    assertThat(sampleHistogram("25.0") - beforeShouldNotContain).isEqualTo(0);
    assertThat(sample("values_count") - countBefore).isEqualTo(1);
    assertThat(sample("values_sum") - sumBefore).isEqualTo((double) TEST_VALUE);
  }

  private double sampleHistogram(String upperBound) {
    return sample("values_bucket", new String[] {"le"}, new String[] {upperBound});
  }

  private double sample(String metricName) {
    return sample(metricName, new String[] {}, new String[] {});
  }

  private double sample(String metricName, String[] labelNames, String[] labelValues) {
    return Objects.firstNonNull(
        CollectorRegistry.defaultRegistry.getSampleValue(metricName, labelNames, labelValues), 0.0);
  }
}
