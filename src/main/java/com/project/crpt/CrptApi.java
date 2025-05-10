package com.project.crpt;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Queue;
import java.util.concurrent.*;

public class CrptApi {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final int requestLimit;
    private final long timeWindowMillis;
    private final Semaphore semaphore;
    private final Queue<Long> requestTimestamps = new ConcurrentLinkedQueue<>();
    private URI apiUri;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.requestLimit = requestLimit;
        this.timeWindowMillis = timeUnit.toMillis(1);
        this.semaphore = new Semaphore(requestLimit, true);
        this.apiUri = URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create");
        startCleanupScheduler();
    }

    public CrptApi(HttpClient httpClient, String baseUrl, TimeUnit timeUnit, int requestLimit) {
        this.httpClient = httpClient;
        this.semaphore = new Semaphore(requestLimit, true);
        this.objectMapper = new ObjectMapper();
        this.requestLimit = requestLimit;
        this.timeWindowMillis = timeUnit.toMillis(1);
        this.apiUri = URI.create(baseUrl + "/api/v3/lk/documents/create");
        startCleanupScheduler();
    }

    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        rateLimit();

        String body = objectMapper.writeValueAsString(new DocumentWrapper(document, signature));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(apiUri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer <token>") // заменить на ваш токен
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IOException("API error: " + response.statusCode() + " - " + response.body());
        }
    }

    private void rateLimit() throws InterruptedException {
        synchronized (requestTimestamps) {
            long now = System.currentTimeMillis();
            while (requestTimestamps.size() >= requestLimit) {
                Long earliest = requestTimestamps.peek();
                if (earliest != null && now - earliest < timeWindowMillis) {
                    requestTimestamps.wait(timeWindowMillis - (now - earliest));
                } else {
                    requestTimestamps.poll();
                }
            }
            requestTimestamps.add(now);
            requestTimestamps.notifyAll();
        }
    }

    private void startCleanupScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            synchronized (requestTimestamps) {
                while (!requestTimestamps.isEmpty() && now - requestTimestamps.peek() > timeWindowMillis) {
                    requestTimestamps.poll();
                }
                requestTimestamps.notifyAll();
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    private static class DocumentWrapper {
        public final Document description;
        public final String signature;

        public DocumentWrapper(Document description, String signature) {
            this.description = description;
            this.signature = signature;
        }
    }

    public static class Document {
        public String token;
        public String participant_inn;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public String import_request;
        public String owner_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public String reg_date;
        public String reg_number;
        public Product[] products;

        public static class Product {
            public String certificate_document;
            public String certificate_document_date;
            public String certificate_document_number;
            public String owner_inn;
            public String producer_inn;
            public String production_date;
            public String tnved_code;
            public String uit_code;
            public String uitu_code;
        }
    }
}
