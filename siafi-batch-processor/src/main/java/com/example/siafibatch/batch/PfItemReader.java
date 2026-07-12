package com.example.siafibatch.batch;

import com.example.siafibatch.model.PfCarga;
import com.example.siafibatch.model.PfDetalhe;
import com.example.siafibatch.repository.PfCargaRepository;
import com.example.siafibatch.service.SiafiCsvParser;
import com.example.siafibatch.service.SiafiXmlParser;
import org.springframework.batch.item.ItemReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PfItemReader implements ItemReader<PfDetalhe> {

    private final String filePath;
    private final SiafiXmlParser xmlParser;
    private final SiafiCsvParser csvParser;
    private final PfCargaRepository pfCargaRepository;

    private Iterator<PfDetalhe> iterator;
    private PfCarga savedCarga;

    public PfItemReader(String filePath,
                        SiafiXmlParser xmlParser,
                        SiafiCsvParser csvParser,
                        PfCargaRepository pfCargaRepository) {
        this.filePath = filePath;
        this.xmlParser = xmlParser;
        this.csvParser = csvParser;
        this.pfCargaRepository = pfCargaRepository;
    }

    @Override
    public PfDetalhe read() throws Exception {
        if (iterator == null) {
            File file = new File(filePath);
            try (InputStream is = new FileInputStream(file)) {
                Object parsed;
                if (file.getName().endsWith(".csv")) {
                    parsed = csvParser.parseSiafiFile(is, file.getName());
                } else {
                    parsed = xmlParser.parseSiafiFile(is, file.getName());
                }

                if (parsed instanceof PfCarga) {
                    PfCarga pfCarga = (PfCarga) parsed;
                    pfCarga.setStatus("PROCESSING");
                    this.savedCarga = pfCargaRepository.save(pfCarga);

                    List<PfDetalhe> details = new ArrayList<>(pfCarga.getDetalhes());
                    for (PfDetalhe p : details) {
                        p.setPfCarga(this.savedCarga);
                    }
                    this.iterator = details.iterator();
                } else {
                    throw new IllegalArgumentException("Parsed file is not a Programacao Financeira (PF) layout.");
                }
            } catch (Exception e) {
                PfCarga failed = new PfCarga();
                failed.setFileName(file.getName());
                failed.setCodigoLayout("PF001");
                failed.setStatus("FAILED");
                failed.setLogProcessamento("Error parsing file: " + e.getMessage());
                pfCargaRepository.save(failed);
                throw e;
            }
        }

        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            if (savedCarga != null) {
                savedCarga.setStatus("SUCCESS");
                savedCarga.setQuantidadeDetalhesProcessados(savedCarga.getQuantidadeDetalhesXml());
                savedCarga.setLogProcessamento("All items parsed and written to the database successfully.");
                pfCargaRepository.save(savedCarga);
            }
            return null; // EOF
        }
    }
}
