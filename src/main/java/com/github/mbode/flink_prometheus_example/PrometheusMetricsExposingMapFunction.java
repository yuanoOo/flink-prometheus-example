package com.github.mbode.flink_prometheus_example;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

public class PrometheusMetricsExposingMapFunction extends RichMapFunction<Integer, Integer> {
  private static final long serialVersionUID = 1L;

  private transient Counter events_total;
  private transient Histogram values;

  @Override
  public void open(Configuration parameters) {
    events_total =
        Counter.build()
            .name("events_total")
            .help(
                String.format(
                    "Total events observed in %s.",
                    PrometheusMetricsExposingMapFunction.class.getSimpleName()))
            .register();
    values =
        Histogram.build()
            .name("values")
            .buckets(5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000, 2_500, 5_000, 7_500, 10_000)
            .help(
                String.format(
                    "Values observed in %s.",
                    PrometheusMetricsExposingMapFunction.class.getSimpleName()))
            .register();
  }

  @Override
  public Integer map(Integer value) {
    events_total.inc();
    values.observe(value);
    return value;
  }
}
