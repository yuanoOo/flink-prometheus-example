package com.github.mbode.flink_prometheus_example;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;

class PrometheusMetricsExposingMapFunction extends RichMapFunction<Integer, Integer> {
  private static final long serialVersionUID = 1L;

  private static Counter eventsTotal;
  private static Histogram valueHistogram;

  @Override
  public void open(Configuration parameters) {
    eventsTotal = Counter.build().name("events_total").help("Total events.").register();
    valueHistogram =
        Histogram.build()
            .name("values")
            .help("Values.")
            .buckets(5, 10, 25, 50, 75, 100, 250, 500, 750, 1_000, 2_500, 5_000, 7_500, 10_000)
            .register();
  }

  @Override
  public void close() {
    CollectorRegistry.defaultRegistry.unregister(eventsTotal);
    CollectorRegistry.defaultRegistry.unregister(valueHistogram);
  }

  @Override
  public Integer map(Integer value) {
    eventsTotal.inc();
    valueHistogram.observe(value);
    return value;
  }
}
