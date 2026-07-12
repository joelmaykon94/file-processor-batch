package com.example.siafibatch.config;

import com.example.siafibatch.batch.DhItemReader;
import com.example.siafibatch.batch.PfItemReader;
import com.example.siafibatch.model.DhDetalhe;
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
        return new DhItemReader(filePath, xmlParser, csvParser, dhCargaRepository);
    }

    @Bean
    public ItemProcessor<DhDetalhe, DhDetalhe> dhProcessor() {
        return item -> {
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
        return new PfItemReader(filePath, xmlParser, csvParser, pfCargaRepository);
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
