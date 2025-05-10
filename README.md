<!-- README.md -->
# CrptApi Java Client

Клиент для работы с API ГИС МТ (Честный знак) на Java 11.

## Возможности

- Потокобезопасность
- Ограничение скорости запросов (rate limiter)
- Лёгкое расширение через внутренние классы
- JUnit 5 + Mockito тесты

## Быстрый старт

1. Добавьте в `pom.xml`:
   ```xml
   <dependency>
     <groupId>com.fasterxml.jackson.core</groupId>
     <artifactId>jackson-databind</artifactId>
     <version>2.13.3</version>
   </dependency>
   <dependency>
     <groupId>org.junit.jupiter</groupId>
     <artifactId>junit-jupiter</artifactId>
     <version>5.8.2</version>
     <scope>test</scope>
   </dependency>
   <dependency>
     <groupId>org.mockito</groupId>
     <artifactId>mockito-core</artifactId>
     <version>4.0.0</version>
     <scope>test</scope>
   </dependency>

## Пример использования

```java
import java.util.concurrent.TimeUnit;

public class Main {
  public static void main(String[] args) throws Exception {
    CrptApi api = new CrptApi(TimeUnit.MINUTES, 60);

    CrptApi.Document doc = new CrptApi.Document();
    doc.token           = "<YOUR_TOKEN>";
    doc.docType         = "IN";
    doc.docId           = "DOC-001";
    doc.participantInn  = "1234567890";
    doc.ownerInn        = "0987654321";
    doc.producerInn     = "1122334455";
    doc.productionDate  = "2025-05-10";

    CrptApi.Document.Product prod = new CrptApi.Document.Product();
    prod.tnvedCode = "6401100000";
    prod.uitCode   = "010123450000000001";
    prod.uituCode  = null;
    doc.products   = new CrptApi.Document.Product[]{ prod };

    api.createDocument(doc, "<SIGNATURE_BASE64>");
    System.out.println("Документ отправлен");
  }
}
