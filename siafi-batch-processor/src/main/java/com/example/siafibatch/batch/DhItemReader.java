package com.example.siafibatch.batch;

import com.example.siafibatch.model.DhCarga;
import com.example.siafibatch.model.DhDetalhe;
import com.example.siafibatch.repository.DhCargaRepository;
import com.example.siafibatch.service.SiafiCsvParser;
import com.example.siafibatch.service.SiafiXmlParser;
import org.springframework.batch.item.ItemReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DhItemReader implements ItemReader<DhDetalhe> {

    private final String filePath;
    private final SiafiXmlParser xmlParser;
    private final SiafiCsvParser csvParser;
    private final DhCargaRepository dhCargaRepository;

    private Iterator<DhDetalhe> iterator;
    private DhCarga savedCarga;

    public DhItemReader(String filePath,
                        SiafiXmlParser xmlParser,
                        SiafiCsvParser csvParser,
                        DhCargaRepository dhCargaRepository) {
        this.filePath = filePath;
        this.xmlParser = xmlParser;
        this.csvParser = csvParser;
        this.dhCargaRepository = dhCargaRepository;
    }

    @Override
    public DhDetalhe read() throws Exception {
        if (iterator == null) {
            File file = new File(filePath);
            try (InputStream is = new FileInputStream(file)) {
                Object parsed;
                if (file.getName().endsWith(".csv")) {
                    parsed = csvParser.parseSiafiFile(is, file.getName());
                } else {
                    parsed = xmlParser.parseSiafiFile(is, file.getName());
                }
                
                if (parsed instanceof DhCarga) {
                    DhCarga dhCarga = (DhCarga) parsed;
                    dhCarga.setStatus("PROCESSING");
                    this.savedCarga = dhCargaRepository.save(dhCarga);
                    
                    List<DhDetalhe> details = new ArrayList<>(dhCarga.getDetalhes());
                    for (DhDetalhe d : details) {
                        d.setDhCarga(this.savedCarga);
                    }
                    this.iterator = details.iterator();
                } else {
                    throw new IllegalArgumentException("Parsed file is not a Documento Habil (DH) layout.");
                }
            } catch (Exception e) {
                DhCarga failed = new DhCarga();
                failed.setFileName(file.getName());
                failed.setCodigoLayout("DH001");
                failed.setStatus("FAILED");
                failed.setLogProcessamento("Error parsing file: " + e.getMessage());
                dhCargaRepository.save(failed);
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
                dhCargaRepository.save(savedCarga);
            }
            return null; // EOF
        }
    }
}
