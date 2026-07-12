package com.example.siafibatch.service;

import com.example.siafibatch.model.DhCarga;
import com.example.siafibatch.model.DhDetalhe;
import com.example.siafibatch.model.PfCarga;
import com.example.siafibatch.model.PfDetalhe;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class SiafiXmlParser {

    public Object parseSiafiFile(InputStream inputStream, String fileName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        doc.getDocumentElement().normalize();

        Element headerEl = getHeaderElement(doc);
        String codigoLayout = getTagValue(headerEl, "codigoLayout");

        if ("DH001".equals(codigoLayout)) {
            return buildDhCarga(doc, headerEl, fileName, codigoLayout);
        } else if ("PF001".equals(codigoLayout)) {
            return buildPfCarga(doc, headerEl, fileName, codigoLayout);
        } else {
            throw new IllegalArgumentException("Unknown layout code: " + codigoLayout);
        }
    }

    private Element getHeaderElement(Document doc) {
        NodeList headerList = doc.getElementsByTagNameNS("*", "header");
        if (headerList.getLength() == 0) {
            throw new IllegalArgumentException("Header element not found in SIAFI XML file.");
        }
        return (Element) headerList.item(0);
    }

    private DhCarga buildDhCarga(Document doc, Element headerEl, String fileName, String codigoLayout) {
        DhCarga dhCarga = new DhCarga();
        dhCarga.setFileName(fileName);
        dhCarga.setCodigoLayout(codigoLayout);
        dhCarga.setDataGeracao(getTagValue(headerEl, "dataGeracao"));
        dhCarga.setSequencialGeracao(getTagValue(headerEl, "sequencialGeracao"));
        dhCarga.setAnoReferencia(getTagValue(headerEl, "anoReferencia"));
        dhCarga.setUgResponsavel(getTagValue(headerEl, "ugResponsavel"));
        dhCarga.setCpfResponsavel(getTagValue(headerEl, "cpfResponsavel"));
        dhCarga.setDataProcessamento(LocalDateTime.now());

        NodeList detalheList = doc.getElementsByTagNameNS("*", "detalhe");
        dhCarga.setQuantidadeDetalhesXml(detalheList.getLength());

        for (int i = 0; i < detalheList.getLength(); i++) {
            Element detalheEl = (Element) detalheList.item(i);
            NodeList dhNodes = detalheEl.getElementsByTagNameNS("*", "CprDhCadastrar");
            if (dhNodes.getLength() > 0) {
                dhCarga.addDetalhe(parseDhDetalhe((Element) dhNodes.item(0)));
            }
        }
        return dhCarga;
    }

    private PfCarga buildPfCarga(Document doc, Element headerEl, String fileName, String codigoLayout) {
        PfCarga pfCarga = new PfCarga();
        pfCarga.setFileName(fileName);
        pfCarga.setCodigoLayout(codigoLayout);
        pfCarga.setDataGeracao(getTagValue(headerEl, "dataGeracao"));
        pfCarga.setSequencialGeracao(getTagValue(headerEl, "sequencialGeracao"));
        pfCarga.setAnoReferencia(getTagValue(headerEl, "anoReferencia"));
        pfCarga.setUgResponsavel(getTagValue(headerEl, "ugResponsavel"));
        pfCarga.setCpfResponsavel(getTagValue(headerEl, "cpfResponsavel"));
        pfCarga.setDataProcessamento(LocalDateTime.now());

        NodeList detalheList = doc.getElementsByTagNameNS("*", "detalhe");
        pfCarga.setQuantidadeDetalhesXml(detalheList.getLength());

        for (int i = 0; i < detalheList.getLength(); i++) {
            Element detalheEl = (Element) detalheList.item(i);
            NodeList pfNodes = detalheEl.getElementsByTagNameNS("*", "PFDTO");
            if (pfNodes.getLength() > 0) {
                pfCarga.addDetalhe(parsePfDetalhe((Element) pfNodes.item(0)));
            }
        }
        return pfCarga;
    }

    private DhDetalhe parseDhDetalhe(Element dhEl) {
        DhDetalhe d = new DhDetalhe();
        d.setCodUgEmit(getTagValue(dhEl, "codUgEmit"));
        d.setAnoDh(getTagValue(dhEl, "anoDH"));
        d.setCodTipoDh(getTagValue(dhEl, "codTipoDH"));
        d.setNumDh(getTagValue(dhEl, "numDH"));

        parseBasicData(dhEl, d);
        parseCommitmentData(dhEl, d);

        return d;
    }

    private void parseBasicData(Element dhEl, DhDetalhe d) {
        NodeList basicNodes = dhEl.getElementsByTagName("dadosBasicos");
        if (basicNodes.getLength() == 0) return;
        
        Element basic = (Element) basicNodes.item(0);
        d.setDtEmis(getTagValue(basic, "dtEmis"));
        d.setDtVenc(getTagValue(basic, "dtVenc"));
        d.setCodUgPgto(getTagValue(basic, "codUgPgto"));
        d.setVlr(parseBigDecimal(getTagValue(basic, "vlr")));
        d.setTxtObser(getTagValue(basic, "txtObser"));
        d.setTxtProcesso(getTagValue(basic, "txtProcesso"));
        d.setDtAteste(getTagValue(basic, "dtAteste"));
        d.setCodCredorDevedor(getTagValue(basic, "codCredorDevedor"));
        d.setDtPgtoReceb(getTagValue(basic, "dtPgtoReceb"));

        parseDocOrigem(basic, d);
    }

    private void parseDocOrigem(Element basic, DhDetalhe d) {
        NodeList docOrigList = basic.getElementsByTagName("docOrigem");
        if (docOrigList.getLength() == 0) return;
        
        Element docOrig = (Element) docOrigList.item(0);
        d.setDocOrigemIdentEmit(getTagValue(docOrig, "codIdentEmit"));
        d.setDocOrigemNum(getTagValue(docOrig, "numDocOrigem"));
        d.setDocOrigemVlr(parseBigDecimal(getTagValue(docOrig, "vlr")));
    }

    private void parseCommitmentData(Element dhEl, DhDetalhe d) {
        NodeList pcoNodes = dhEl.getElementsByTagName("pco");
        if (pcoNodes.getLength() == 0) return;
        
        Element pco = (Element) pcoNodes.item(0);
        d.setPcoSit(getTagValue(pco, "codSit"));
        d.setPcoUgEmpe(getTagValue(pco, "codUgEmpe"));
        
        NodeList itemNodes = pco.getElementsByTagName("pcoItem");
        if (itemNodes.getLength() > 0) {
            Element item = (Element) itemNodes.item(0);
            d.setPcoEmpeNum(getTagValue(item, "numEmpe"));
            d.setPcoVlr(parseBigDecimal(getTagValue(item, "vlr")));
        }
    }

    private PfDetalhe parsePfDetalhe(Element pfEl) {
        PfDetalhe p = new PfDetalhe();
        p.setTipoPf(getTagValue(pfEl, "tipoPF"));
        p.setObservacao(getTagValue(pfEl, "observacao"));
        p.setCodUgEmit(getTagValue(pfEl, "codUgEmit"));
        p.setCodUgFavorecida(getTagValue(pfEl, "codUgFavorecida"));
        p.setLimiteDeSaque(Boolean.parseBoolean(getTagValue(pfEl, "limiteDeSaque")));
        p.setNumeroDocumento(getTagValue(pfEl, "numeroDocumento"));

        parsePfItem(pfEl, p);

        return p;
    }

    private void parsePfItem(Element pfEl, PfDetalhe p) {
        NodeList itemNodes = pfEl.getElementsByTagName("listaItemPFDTO");
        if (itemNodes.getLength() == 0) return;
        
        Element item = (Element) itemNodes.item(0);
        p.setVlr(parseBigDecimal(getTagValue(item, "vlr")));
        p.setCodVinc(getTagValue(item, "codVinc"));
        p.setCodFontRecur(getTagValue(item, "codFontRecur"));
        p.setCodCtgoGasto(getTagValue(item, "codCtgoGasto"));
        p.setCodSit(getTagValue(item, "codSit"));
        p.setTxtInscrA(getTagValue(item, "txtInscrA"));
    }

    private String getTagValue(Element element, String tagName) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            Node node = list.item(0);
            if (node.hasChildNodes()) {
                return node.getFirstChild().getNodeValue();
            }
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null || val.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(val.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
