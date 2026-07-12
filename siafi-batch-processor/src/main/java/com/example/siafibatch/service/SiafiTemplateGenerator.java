package com.example.siafibatch.service;

import org.springframework.stereotype.Component;

@Component
public class SiafiTemplateGenerator {

    public String getSampleDhXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<sb:arquivo xmlns:sb=\"http://www.tesouro.gov.br/siafi/submissao\">\n" +
                "    <sb:header>\n" +
                "        <sb:codigoLayout>DH001</sb:codigoLayout>\n" +
                "        <sb:dataGeracao>11/07/2026</sb:dataGeracao>\n" +
                "        <sb:sequencialGeracao>1001</sb:sequencialGeracao>\n" +
                "        <sb:anoReferencia>2026</sb:anoReferencia>\n" +
                "        <sb:ugResponsavel>080014</sb:ugResponsavel>\n" +
                "        <sb:cpfResponsavel>99999999999</sb:cpfResponsavel>\n" +
                "    </sb:header>\n" +
                "    <sb:detalhes>\n" +
                "        <sb:detalhe>\n" +
                "            <cpr:CprDhCadastrar xmlns:cpr=\"http://services.docHabil.cpr.siafi.tesouro.fazenda.gov.br/\">\n" +
                "                <codUgEmit>080014</codUgEmit>\n" +
                "                <anoDH>2026</anoDH>\n" +
                "                <codTipoDH>NP</codTipoDH>\n" +
                "                <numDH>990101</numDH>\n" +
                "                <dadosBasicos>\n" +
                "                    <dtEmis>2026-07-10</dtEmis>\n" +
                "                    <dtVenc>2026-07-15</dtVenc>\n" +
                "                    <codUgPgto>080014</codUgPgto>\n" +
                "                    <vlr>1500.00</vlr>\n" +
                "                    <txtObser>INTEGRACAO BATCH EXEMPLE DH</txtObser>\n" +
                "                    <txtProcesso>12345678901234567890</txtProcesso>\n" +
                "                    <codCredorDevedor>05462743000954</codCredorDevedor>\n" +
                "                </dadosBasicos>\n" +
                "            </cpr:CprDhCadastrar>\n" +
                "        </sb:detalhe>\n" +
                "    </sb:detalhes>\n" +
                "    <sb:trailler>\n" +
                "        <sb:quantidadeDetalhe>1</sb:quantidadeDetalhe>\n" +
                "    </sb:trailler>\n" +
                "</sb:arquivo>";
    }

    public String getSamplePfXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<sb:arquivo xmlns:sb=\"http://www.tesouro.gov.br/siafi/submissao\">\n" +
                "    <sb:header>\n" +
                "        <sb:codigoLayout>PF001</sb:codigoLayout>\n" +
                "        <sb:dataGeracao>11/07/2026</sb:dataGeracao>\n" +
                "        <sb:sequencialGeracao>2001</sb:sequencialGeracao>\n" +
                "        <sb:anoReferencia>2026</sb:anoReferencia>\n" +
                "        <sb:ugResponsavel>160075</sb:ugResponsavel>\n" +
                "        <sb:cpfResponsavel>99999999999</sb:cpfResponsavel>\n" +
                "    </sb:header>\n" +
                "    <sb:detalhes>\n" +
                "        <sb:detalhe>\n" +
                "            <ns3:PFDTO xmlns:ns3=\"http://www.tesouro.gov.br/siafi/services/pf/manterProgramacaoFinanceira\">\n" +
                "                <tipoPF>TRF</tipoPF>\n" +
                "                <observacao>SAMPLE PROGRAMACAO FINANCEIRA INTEGRATION</observacao>\n" +
                "                <codUgEmit>160075</codUgEmit>\n" +
                "                <codUgFavorecida>160348</codUgFavorecida>\n" +
                "                <limiteDeSaque>true</limiteDeSaque>\n" +
                "                <listaItemPFDTO>\n" +
                "                    <vlr>3450.00</vlr>\n" +
                "                    <codVinc>415</codVinc>\n" +
                "                    <codFontRecur>0100000000</codFontRecur>\n" +
                "                    <codCtgoGasto>D</codCtgoGasto>\n" +
                "                    <codSit>TRF020</codSit>\n" +
                "                </listaItemPFDTO>\n" +
                "                <numeroDocumento>778899</numeroDocumento>\n" +
                "            </ns3:PFDTO>\n" +
                "        </sb:detalhe>\n" +
                "    </sb:detalhes>\n" +
                "    <sb:trailler>\n" +
                "        <sb:quantidadeDetalhe>1</sb:quantidadeDetalhe>\n" +
                "    </sb:trailler>\n" +
                "</sb:arquivo>";
    }

    public String getSampleDhCsv() {
        return "layout,codUgEmit,anoDH,codTipoDH,numDH,dtEmis,dtVenc,codUgPgto,vlr,txtObser,txtProcesso,dtAteste,codCredorDevedor,dtPgtoReceb,docOrigemIdentEmit,docOrigemNum,docOrigemVlr,pcoSit,pcoUgEmpe,pcoEmpeNum,pcoVlr\n" +
                "DH001,080014,2026,NP,990202,2026-07-10,2026-07-15,080014,2750.50,CSV DH LOAD TEST,98765432109876543210,2026-07-10,05462743000954,2026-07-15,05462743000954,CSVORIGEM123,2750.50,DSP001,080014,2026NE9902,2750.50";
    }

    public String getSamplePfCsv() {
        return "layout,tipoPF,observacao,codUgEmit,codUgFavorecida,limiteDeSaque,vlr,codVinc,codFontRecur,codCtgoGasto,codSit,txtInscrA,numeroDocumento\n" +
                "PF001,TRF,CSV PF LOAD TEST,160075,160348,true,9900.00,415,0100000000,D,TRF020,675901,889900";
    }
}
