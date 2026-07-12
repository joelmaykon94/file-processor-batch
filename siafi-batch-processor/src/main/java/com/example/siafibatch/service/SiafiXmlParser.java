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

        // 1. Get header information
        NodeList headerList = doc.getElementsByTagNameNS("*", "header");
        if (headerList.getLength() == 0) {
            throw new IllegalArgumentException("Header element not found in SIAFI XML file.");
        }
        Element headerEl = (Element) headerList.item(0);

        String codigoLayout = getTagValue(headerEl, "codigoLayout");
        String dataGeracao = getTagValue(headerEl, "dataGeracao");
        String sequencialGeracao = getTagValue(headerEl, "sequencialGeracao");
        String anoReferencia = getTagValue(headerEl, "anoReferencia");
        String ugResponsavel = getTagValue(headerEl, "ugResponsavel");
        String cpfResponsavel = getTagValue(headerEl, "cpfResponsavel");

        // 2. Determine type of layout
        if ("DH001".equals(codigoLayout)) {
            DhCarga dhCarga = new DhCarga();
            dhCarga.setFileName(fileName);
            dhCarga.setCodigoLayout(codigoLayout);
            dhCarga.setDataGeracao(dataGeracao);
            dhCarga.setSequencialGeracao(sequencialGeracao);
            dhCarga.setAnoReferencia(anoReferencia);
            dhCarga.setUgResponsavel(ugResponsavel);
            dhCarga.setCpfResponsavel(cpfResponsavel);
            dhCarga.setDataProcessamento(LocalDateTime.now());

            NodeList detalheList = doc.getElementsByTagNameNS("*", "detalhe");
            dhCarga.setQuantidadeDetalhesXml(detalheList.getLength());

            for (int i = 0; i < detalheList.getLength(); i++) {
                Element detalheEl = (Element) detalheList.item(i);
                NodeList dhNodes = detalheEl.getElementsByTagNameNS("*", "CprDhCadastrar");
                if (dhNodes.getLength() > 0) {
                    Element dhEl = (Element) dhNodes.item(0);
                    DhDetalhe detail = parseDhDetalhe(dhEl);
                    dhCarga.addDetalhe(detail);
                }
            }
            return dhCarga;

        } else if ("PF001".equals(codigoLayout)) {
            PfCarga pfCarga = new PfCarga();
            pfCarga.setFileName(fileName);
            pfCarga.setCodigoLayout(codigoLayout);
            pfCarga.setDataGeracao(dataGeracao);
            pfCarga.setSequencialGeracao(sequencialGeracao);
            pfCarga.setAnoReferencia(anoReferencia);
            pfCarga.setUgResponsavel(ugResponsavel);
            pfCarga.setCpfResponsavel(cpfResponsavel);
            pfCarga.setDataProcessamento(LocalDateTime.now());

            NodeList detalheList = doc.getElementsByTagNameNS("*", "detalhe");
            pfCarga.setQuantidadeDetalhesXml(detalheList.getLength());

            for (int i = 0; i < detalheList.getLength(); i++) {
                Element detalheEl = (Element) detalheList.item(i);
                NodeList pfNodes = detalheEl.getElementsByTagNameNS("*", "PFDTO");
                if (pfNodes.getLength() > 0) {
                    Element pfEl =  (Element) pfNodes.item(0);
                    PfDetalhe detail = parsePfDetalhe(pfEl);
                    pfCarga.addDetalhe(detail);
                }
            }
            return pfCarga;
        } else {
            throw new IllegalArgumentException("Unknown layout code: " + codigoLayout);
        }
    }

    private DhDetalhe parseDhDetalhe(Element dhEl) {
        DhDetalhe d = new DhDetalhe();
        d.setCodUgEmit(getTagValue(dhEl, "codUgEmit"));
        d.setAnoDh(getTagValue(dhEl, "anoDH"));
        d.setCodTipoDh(getTagValue(dhEl, "codTipoDH"));
        d.setNumDh(getTagValue(dhEl, "numDH"));

        // dadosBasicos
        NodeList basicNodes = dhEl.getElementsByTagName("dadosBasicos");
        if (basicNodes.getLength() > 0) {
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

            // docOrigem
            NodeList docOrigList = basic.getElementsByTagName("docOrigem");
            if (docOrigList.getLength() > 0) {
                Element docOrig = (Element) docOrigList.item(0);
                d.setDocOrigemIdentEmit(getTagValue(docOrig, "codIdentEmit"));
                d.setDocOrigemNum(getTagValue(docOrig, "numDocOrigem"));
                d.setDocOrigemVlr(parseBigDecimal(getTagValue(docOrig, "vlr")));
            }
        }

        // pco (Commitment)
        NodeList pcoNodes = dhEl.getElementsByTagName("pco");
        if (pcoNodes.getLength() > 0) {
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

        return d;
    }

    private PfDetalhe parsePfDetalhe(Element pfEl) {
        PfDetalhe p = new PfDetalhe();
        p.setTipoPf(getTagValue(pfEl, "tipoPF"));
        p.setObservacao(getTagValue(pfEl, "observacao"));
        p.setCodUgEmit(getTagValue(pfEl, "codUgEmit"));
        p.setCodUgFavorecida(getTagValue(pfEl, "codUgFavorecida"));
        p.setLimiteDeSaque(Boolean.parseBoolean(getTagValue(pfEl, "limiteDeSaque")));
        p.setNumeroDocumento(getTagValue(pfEl, "numeroDocumento"));

        // item (listaItemPFDTO)
        NodeList itemNodes = pfEl.getElementsByTagName("listaItemPFDTO");
        if (itemNodes.getLength() > 0) {
            Element item = (Element) itemNodes.item(0);
            p.setVlr(parseBigDecimal(getTagValue(item, "vlr")));
            p.setCodVinc(getTagValue(item, "codVinc"));
            p.setCodFontRecur(getTagValue(item, "codFontRecur"));
            p.setCodCtgoGasto(getTagValue(item, "codCtgoGasto"));
            p.setCodSit(getTagValue(item, "codSit"));
            p.setTxtInscrA(getTagValue(item, "txtInscrA"));
        }

        return p;
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
