package com.github.mbode.flink_prometheus_example;

import io.prometheus.client.CollectorRegistry;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;
import org.apache.flink.test.util.AbstractTestBase;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

class PrometheusMetricsExposingMapFunctionIT extends AbstractTestBase {
  @Test
  void metricsAreReported() throws Exception {
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(1);
    env.addSource(new RandomSourceFunction(10))
        .map(new PrometheusMetricsExposingMapFunction())
        .addSink(new DiscardingSink<>());
    env.execute();
    await().atMost(5, SECONDS).until(prometheusMetricsAreReported());
  }

  private Callable<Boolean> prometheusMetricsAreReported() {
    return () -> {
      System.out.println(CollectorRegistry.defaultRegistry.getSampleValue("events_total"));
      return true;
    };
  }
}