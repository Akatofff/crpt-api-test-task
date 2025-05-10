import com.project.crpt.CrptApi;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CrptApiTest {

    private HttpClient mockClient;
    private CrptApi api;

    @BeforeEach
    void setUp() {
        mockClient = Mockito.mock(HttpClient.class);
        api = new CrptApi(mockClient, "https://demo.crpt.ru", TimeUnit.SECONDS, 2);
    }

    @Test
    void testCreateDocumentSuccess() throws Exception {
        // настроим успешный ответ
        HttpResponse<String> okResp = Mockito.mock(HttpResponse.class);
        when(okResp.statusCode()).thenReturn(200);
        when(mockClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(okResp);

        CrptApi.Document doc = new CrptApi.Document();
        doc.token = "tok";
        doc.doc_type = "IN";
        doc.doc_id = "123";
        doc.participant_inn = "000";
        doc.owner_inn = "111";
        doc.producer_inn = "222";
        doc.production_date = "2025-05-10";
        api.createDocument(doc, "sig");

        verify(mockClient, times(1)).send(any(), any());
    }

    @Test
    void testCreateDocumentRateLimit() {
        // пустой успешный ответ
        HttpResponse<String> okResp = Mockito.mock(HttpResponse.class);
        when(okResp.statusCode()).thenReturn(200);
        try {
            when(mockClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(okResp);
            long start = System.nanoTime();
            // три запроса на лимите 2/sec — третий должен затормозить ~0.5 с
            api.createDocument(new CrptApi.Document(), "s");
            api.createDocument(new CrptApi.Document(), "s");
            api.createDocument(new CrptApi.Document(), "s");
            long elapsedMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
            assertTrue(elapsedMs >= 900, "Ожидаем блокировку на ~1s, получили " + elapsedMs + "ms");
        } catch (IOException | InterruptedException e) {
            fail(e);
        }
    }

    @Test
    void testCreateDocumentError() throws Exception {
        HttpResponse<String> err = Mockito.mock(HttpResponse.class);
        when(err.statusCode()).thenReturn(500);
        when(err.body()).thenReturn("Internal");
        when(mockClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(err);

        assertThrows(IOException.class, () ->
                api.createDocument(new CrptApi.Document(), "sig")
        );
    }
}
