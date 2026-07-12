package com.example.siafibatch.service;

import com.example.siafibatch.model.DhCarga;
import com.example.siafibatch.model.DhDetalhe;
import com.example.siafibatch.model.PfCarga;
import com.example.siafibatch.model.PfDetalhe;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class SiafiCsvParser {

    public Object parseSiafiFile(InputStream inputStream, String fileName) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Empty CSV file.");
            }

            // Simple header parsing logic: columns separated by comma or semicolon
            String delimiter = headerLine.contains(";") ? ";" : ",";
            String[] headers = headerLine.split(delimiter);

            // Read the first data line to determine layout or check headers
            String dataLine = reader.readLine();
            if (dataLine == null) {
                throw new IllegalArgumentException("CSV file has header but no data rows.");
            }

            String[] firstRow = dataLine.split(delimiter);
            String layout = firstRow[0].replace("\"", "").trim();

            if ("DH001".equalsIgnoreCase(layout)) {
                DhCarga dhCarga = new DhCarga();
                dhCarga.setFileName(fileName);
                dhCarga.setCodigoLayout("DH001");
                dhCarga.setDataGeracao("CSV_IMPORT");
                dhCarga.setSequencialGeracao("1001");
                dhCarga.setAnoReferencia("2026");
                dhCarga.setUgResponsavel("SYSTEM");
                dhCarga.setCpfResponsavel("99999999999");
                dhCarga.setDataProcessamento(LocalDateTime.now());

                // Process first row
                dhCarga.addDetalhe(parseDhRow(firstRow));
                int totalRows = 1;

                // Process remaining rows
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] row = line.split(delimiter);
                    dhCarga.addDetalhe(parseDhRow(row));
                    totalRows++;
                }

                dhCarga.setQuantidadeDetalhesXml(totalRows);
                return dhCarga;

            } else if ("PF001".equalsIgnoreCase(layout)) {
                PfCarga pfCarga = new PfCarga();
                pfCarga.setFileName(fileName);
                pfCarga.setCodigoLayout("PF001");
                pfCarga.setDataGeracao("CSV_IMPORT");
                pfCarga.setSequencialGeracao("2001");
                pfCarga.setAnoReferencia("2026");
                pfCarga.setUgResponsavel("SYSTEM");
                pfCarga.setCpfResponsavel("99999999999");
                pfCarga.setDataProcessamento(LocalDateTime.now());

                // Process first row
                pfCarga.addDetalhe(parsePfRow(firstRow));
                int totalRows = 1;

                // Process remaining rows
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] row = line.split(delimiter);
                    pfCarga.addDetalhe(parsePfRow(row));
                    totalRows++;
                }

                pfCarga.setQuantidadeDetalhesXml(totalRows);
                return pfCarga;
            } else {
                throw new IllegalArgumentException("Unknown layout in CSV: " + layout + ". Must be DH001 or PF001");
            }
        }
    }

    private DhDetalhe parseDhRow(String[] row) {
        DhDetalhe d = new DhDetalhe();
        // Index mapping:
        // 0: layout (DH001)
        // 1: codUgEmit
        // 2: anoDh
        // 3: codTipoDh
        // 4: numDh
        // 5: dtEmis
        // 6: dtVenc
        // 7: codUgPgto
        // 8: vlr
        // 9: txtObser
        // 10: txtProcesso
        // 11: dtAteste
        // 12: codCredorDevedor
        // 13: dtPgtoReceb
        // 14: docOrigemIdentEmit
        // 15: docOrigemNum
        // 16: docOrigemVlr
        // 17: pcoSit
        // 18: pcoUgEmpe
        // 19: pcoEmpeNum
        // 20: pcoVlr

        d.setCodUgEmit(valAt(row, 1));
        d.setAnoDh(valAt(row, 2));
        d.setCodTipoDh(valAt(row, 3));
        d.setNumDh(valAt(row, 4));
        d.setDtEmis(valAt(row, 5));
        d.setDtVenc(valAt(row, 6));
        d.setCodUgPgto(valAt(row, 7));
        d.setVlr(parseBigDecimal(valAt(row, 8)));
        d.setTxtObser(valAt(row, 9));
        d.setTxtProcesso(valAt(row, 10));
        d.setDtAteste(valAt(row, 11));
        d.setCodCredorDevedor(valAt(row, 12));
        d.setDtPgtoReceb(valAt(row, 13));

        d.setDocOrigemIdentEmit(valAt(row, 14));
        d.setDocOrigemNum(valAt(row, 15));
        d.setDocOrigemVlr(parseBigDecimal(valAt(row, 16)));

        d.setPcoSit(valAt(row, 17));
        d.setPcoUgEmpe(valAt(row, 18));
        d.setPcoEmpeNum(valAt(row, 19));
        d.setPcoVlr(parseBigDecimal(valAt(row, 20)));

        return d;
    }

    private PfDetalhe parsePfRow(String[] row) {
        // Index mapping:
        // 0: layout (PF001)
        // 1: tipoPF
        // 2: observacao
        // 3: codUgEmit
        // 4: codUgFavorecida
        // 5: limiteDeSaque
        // 6: vlr
        // 7: codVinc
        // 8: codFontRecur
        // 9: codCtgoGasto
        // 10: codSit
        // 11: txtInscrA
        // 12: numeroDocumento

        PfDetalhe p = new PfDetalhe();
        p.setTipoPf(valAt(row, 1));
        p.setObservacao(valAt(row, 2));
        p.setCodUgEmit(valAt(row, 3));
        p.setCodUgFavorecida(valAt(row, 4));
        p.setLimiteDeSaque(Boolean.parseBoolean(valAt(row, 5)));
        p.setVlr(parseBigDecimal(valAt(row, 6)));
        p.setCodVinc(valAt(row, 7));
        p.setCodFontRecur(valAt(row, 8));
        p.setCodCtgoGasto(valAt(row, 9));
        p.setCodSit(valAt(row, 10));
        p.setTxtInscrA(valAt(row, 11));
        p.setNumeroDocumento(valAt(row, 12));

        return p;
    }

    private String valAt(String[] row, int index) {
        if (index < row.length) {
            String val = row[index];
            if (val != null) {
                return val.replace("\"", "").trim();
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
