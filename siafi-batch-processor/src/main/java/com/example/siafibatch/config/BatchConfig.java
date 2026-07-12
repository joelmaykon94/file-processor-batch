package com.example.siafibatch.config;

import com.example.siafibatch.model.DhCarga;
import com.example.siafibatch.model.DhDetalhe;
import com.example.siafibatch.model.PfCarga;
import com.example.siafibatch.model.PfDetalhe;
import com.example.siafibatch.repository.DhCargaRepository;
import com.example.siafibatch.repository.DhDetalheRepository;
import com.example.siafibatch.repository.PfCargaRepository;
import com.example.siafibatch.repository.PfDetalheRepository;
import com.example.siafibatch.service.SiafiCsvParser;
import com.example.siafibatch.service.SiafiXmlParser;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Configuration
public class BatchConfig {

    private final DhCargaRepository dhCargaRepository;
    private final DhDetalheRepository dhDetalheRepository;
    private final PfCargaRepository pfCargaRepository;
    private final PfDetalheRepository pfDetalheRepository;
    private final SiafiXmlParser xmlParser;
    private final SiafiCsvParser csvParser;

    public BatchConfig(DhCargaRepository dhCargaRepository,
                       DhDetalheRepository dhDetalheRepository,
                       PfCargaRepository pfCargaRepository,
                       PfDetalheRepository pfDetalheRepository,
                       SiafiXmlParser xmlParser,
                       SiafiCsvParser csvParser) {
        this.dhCargaRepository = dhCargaRepository;
        this.dhDetalheRepository = dhDetalheRepository;
        this.pfCargaRepository = pfCargaRepository;
        this.pfDetalheRepository = pfDetalheRepository;
        this.xmlParser = xmlParser;
        this.csvParser = csvParser;
    }

    // ==========================================
    // DOCUMENTO HABIL (DH) JOB
    // ==========================================

    @Bean
    public Job importDhJob(JobRepository jobRepository, Step dhStep) {
        return new JobBuilder("importDhJob", jobRepository)
                .start(dhStep)
                .build();
    }

    @Bean
    public Step dhStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                       ItemReader<DhDetalhe> dhReader,
                       ItemProcessor<DhDetalhe, DhDetalhe> dhProcessor,
                       ItemWriter<DhDetalhe> dhWriter) {
        return new StepBuilder("dhStep", jobRepository)
                .<DhDetalhe, DhDetalhe>chunk(5, transactionManager)
                .reader(dhReader)
                .processor(dhProcessor)
                .writer(dhWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<DhDetalhe> dhReader(@Value("#{jobParameters['filePath']}") String filePath) {
        return new ItemReader<DhDetalhe>() {
            private Iterator<DhDetalhe> iterator;
            private DhCarga savedCarga;

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
                            // Save Carga header first to get ID
                            dhCarga.setStatus("PROCESSING");
                            this.savedCarga = dhCargaRepository.save(dhCarga);
                            
                            List<DhDetalhe> details = new ArrayList<>(dhCarga.getDetalhes());
                            // Associate child elements
                            for (DhDetalhe d : details) {
                                d.setDhCarga(this.savedCarga);
                            }
                            this.iterator = details.iterator();
                        } else {
                            throw new IllegalArgumentException("Parsed file is not a Documento Habil (DH) layout.");
                        }
                    } catch (Exception e) {
                        // Create failed Carga log
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
                    // Update header status when finished reading all items
                    if (savedCarga != null) {
                        savedCarga.setStatus("SUCCESS");
                        savedCarga.setQuantidadeDetalhesProcessados(savedCarga.getQuantidadeDetalhesXml());
                        savedCarga.setLogProcessamento("All items parsed and written to the database successfully.");
                        dhCargaRepository.save(savedCarga);
                    }
                    return null; // EOF
                }
            }
        };
    }

    @Bean
    public ItemProcessor<DhDetalhe, DhDetalhe> dhProcessor() {
        return item -> {
            // Apply formatting / cleanups / business rules
            if (item.getTxtObser() != null) {
                item.setTxtObser(item.getTxtObser().toUpperCase());
            }
            return item;
        };
    }

    @Bean
    public ItemWriter<DhDetalhe> dhWriter() {
        return chunk -> {
            for (DhDetalhe detail : chunk.getItems()) {
                dhDetalheRepository.save(detail);
            }
        };
    }

    // ==========================================
    // PROGRAMACAO FINANCEIRA (PF) JOB
    // ==========================================

    @Bean
    public Job importPfJob(JobRepository jobRepository, Step pfStep) {
        return new JobBuilder("importPfJob", jobRepository)
                .start(pfStep)
                .build();
    }

    @Bean
    public Step pfStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                       ItemReader<PfDetalhe> pfReader,
                       ItemProcessor<PfDetalhe, PfDetalhe> pfProcessor,
                       ItemWriter<PfDetalhe> pfWriter) {
        return new StepBuilder("pfStep", jobRepository)
                .<PfDetalhe, PfDetalhe>chunk(5, transactionManager)
                .reader(pfReader)
                .processor(pfProcessor)
                .writer(pfWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<PfDetalhe> pfReader(@Value("#{jobParameters['filePath']}") String filePath) {
        return new ItemReader<PfDetalhe>() {
            private Iterator<PfDetalhe> iterator;
            private PfCarga savedCarga;

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
        };
    }

    @Bean
    public ItemProcessor<PfDetalhe, PfDetalhe> pfProcessor() {
        return item -> {
            if (item.getObservacao() != null) {
                item.setObservacao(item.getObservacao().toUpperCase());
            }
            return item;
        };
    }

    @Bean
    public ItemWriter<PfDetalhe> pfWriter() {
        return chunk -> {
            for (PfDetalhe detail : chunk.getItems()) {
                pfDetalheRepository.save(detail);
            }
        };
    }
}
