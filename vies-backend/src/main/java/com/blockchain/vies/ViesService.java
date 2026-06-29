package com.blockchain.vies;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class ViesService {

    private static final String VIES_URL =
            "https://ec.europa.eu/taxation_customs/vies/services/checkVatService";

    private static final String VIES_TEST_URL =
            "https://ec.europa.eu/taxation_customs/vies/services/checkVatTestService";

    public VatResponse verify(String rawVatId) {
        if (rawVatId == null || rawVatId.isBlank())
            return new VatResponse(false, "Bitte eine USt-ID eingeben." );

        String vatId      = rawVatId.trim().toUpperCase().replaceAll("\\s+", "");
        if (vatId.length() < 3)
            return new VatResponse(false, "USt-ID zu kurz. Format: DE123456789");

        String countryCode = vatId.substring(0, 2);
        String vatNumber   = vatId.substring(2);

        if (!countryCode.matches("[A-Z]{2}"))
            return new VatResponse(false, "Ungültiger Ländercode (z. B. DE).");
        if (vatNumber.isBlank())
            return new VatResponse(false, "Keine Nummer nach dem Ländercode.");
        if (countryCode.equals("DE") && !vatNumber.matches("\\d{9}"))
            return new VatResponse(false, "Deutschland: DE + genau 9 Ziffern (z. B. DE123456789).");

        // 1. Produktiv versuchen
        try {
            VatResponse result = callVies(VIES_URL, countryCode, vatNumber, vatId, false);
            if (result.getValid() != null) return result;
            System.out.println("[VIES] MS_UNAVAILABLE → wechsle zu Test-Endpoint");
        } catch (Exception e) {
            System.out.println("[VIES] Produktiv-Fehler: " + e.getMessage() + " → Fallback");
        }

        // 2. Test-Endpoint als Fallback
        try {
            return callVies(VIES_TEST_URL, countryCode, vatNumber, vatId, true);
        } catch (Exception e) {
            return new VatResponse(null, "VIES nicht erreichbar: " + e.getMessage());
        }
    }

    private VatResponse callVies(String endpoint, String countryCode,
                                 String vatNumber, String fullVatId,
                                 boolean isTestEndpoint) throws Exception {
        String soapBody =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                        "  xmlns:urn=\"urn:ec.europa.eu:taxud:vies:services:checkVat:types\">" +
                        "  <soapenv:Body><urn:checkVat>" +
                        "    <urn:countryCode>" + countryCode + "</urn:countryCode>" +
                        "    <urn:vatNumber>" + vatNumber + "</urn:vatNumber>" +
                        "  </urn:checkVat></soapenv:Body>" +
                        "</soapenv:Envelope>";

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
        conn.setRequestProperty("SOAPAction", "");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(soapBody.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status < 400) ? conn.getInputStream() : conn.getErrorStream();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        doc.getDocumentElement().normalize();

        NodeList faultNodes = doc.getElementsByTagName("faultstring");
        if (faultNodes.getLength() > 0) {
            String fault = faultNodes.item(0).getTextContent().trim();
            if (fault.contains("INVALID_INPUT"))
                return new VatResponse(false, "Ungültiges Format für dieses Land.");
            if (fault.contains("SERVICE_UNAVAILABLE") || fault.contains("MS_UNAVAILABLE")
                    || fault.contains("MS_MAX_CONCURRENT_REQ"))
                return new VatResponse(null, fault);
            return new VatResponse(null, "VIES Fehler: " + fault);
        }

        String validText = getTagValueByLocalName(doc, "valid");
        boolean isValid  = "true".equalsIgnoreCase(validText);

        if (!isValid)
            return new VatResponse(false, "USt-ID nicht gültig oder nicht aktiv (laut EU VIES).");

        String name    = getTagValueByLocalName(doc, "name");
        String address = getTagValueByLocalName(doc, "address");
        if ("---".equals(name))    name    = null;
        if ("---".equals(address)) address = null;

        if (isTestEndpoint) {
            return new VatResponse(true, fullVatId,
                    "Format gültig (Echtprüfung aktuell nicht verfügbar)",
                    "⚠ Geprüft via EU VIES Test-Service – kein Echtzeit-Datenbankabgleich");
        }

        return new VatResponse(true, fullVatId, name, address);
    }

    private String getTagValueByLocalName(Document doc, String localName) {
        NodeList all = doc.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            String nodeName = all.item(i).getNodeName();
            if (nodeName.equals(localName) || nodeName.endsWith(":" + localName)) {
                String val = all.item(i).getTextContent();
                return (val == null || val.isBlank()) ? null : val.trim();
            }
        }
        return null;
    }
}
