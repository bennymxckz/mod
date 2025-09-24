package com.craftoria.dashboardmod;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import me.lucko.spark.api.statistic.types.cpu.CpuUsageStatistic;
import me.lucko.spark.api.statistic.types.memory.MemoryUsageStatistic;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ApiServer {
    public static void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/stats", new StatsHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Spark spark = SparkProvider.get();

                // TPS
                GenericStatistic<Double> tps = spark.tps();
                double tpsValue = Optional.ofNullable(tps.poll(StatisticWindow.MINUTES_1)).orElse(0.0);

                // CPU
                CpuUsageStatistic cpu = spark.cpuProcess();
                double cpuValue = Optional.ofNullable(cpu.poll(StatisticWindow.SECONDS_10)).orElse(0.0);

                // Memory
                MemoryUsageStatistic memory = spark.memoryUsage();
                long usedMemory = memory.poll(StatisticWindow.LATEST).getUsed() / (1024 * 1024);
                long maxMemory = memory.poll(StatisticWindow.LATEST).getMax() / (1024 * 1024);

                // Format as JSON
                String jsonResponse = String.format(
                    "{\"tps\": %.2f, \"cpu_process\": %.2f, \"memory_used_mb\": %d, \"memory_max_mb\": %d}",
                    tpsValue, cpuValue, usedMemory, maxMemory
                );

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
                os.close();
            } catch (Exception e) {
                String errorResponse = "{\"error\": \"Could not retrieve server stats.\"}";
                exchange.sendResponseHeaders(500, errorResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(errorResponse.getBytes());
                os.close();
            }
        }
    }
}