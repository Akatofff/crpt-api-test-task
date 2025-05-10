import com.project.crpt.CrptApi;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        CrptApi api = new CrptApi(TimeUnit.MINUTES, 60);

        CrptApi.Document doc = new CrptApi.Document();
        doc.token            = "<YOUR_TOKEN>";
        doc.doc_type         = "IN";
        doc.doc_id           = "DOC-001";
        doc.participant_inn  = "1234567890";
        doc.owner_inn        = "0987654321";
        doc.producer_inn     = "1122334455";
        doc.production_date  = "2025-05-10";

        CrptApi.Document.Product p = new CrptApi.Document.Product();
        p.tnved_code = "6401100000";
        p.uit_code   = "010123450000000001";
        p.uitu_code  = null;
        doc.products = new CrptApi.Document.Product[]{ p };

        api.createDocument(doc, "<SIGNATURE_BASE64>");
        System.out.println("Документ отправлен");
    }
}
