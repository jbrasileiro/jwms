package com.github.jbrasileiro.jwms.infrastructure.persistence;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

public final class MinimalXmlRootName {

    private MinimalXmlRootName() {}

    public static Optional<String> parse(byte[] xmlBytes) {
        if (xmlBytes == null || xmlBytes.length == 0) {
            return Optional.empty();
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var builder = dbf.newDocumentBuilder();
            var doc = builder.parse(new ByteArrayInputStream(xmlBytes));
            Element root = doc.getDocumentElement();
            if (root == null) {
                return Optional.empty();
            }
            String local = root.getLocalName();
            if (local != null && !local.isEmpty()) {
                return Optional.of(local);
            }
            String raw = root.getTagName();
            if (raw != null && raw.contains(":")) {
                return Optional.of(raw.substring(raw.indexOf(':') + 1));
            }
            return Optional.ofNullable(raw);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
