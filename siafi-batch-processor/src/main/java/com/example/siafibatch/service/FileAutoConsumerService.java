package com.example.siafibatch.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

@Service
public class FileAutoConsumerService {

    private static final Logger LOGGER = Logger.getLogger(FileAutoConsumerService.class.getName());

    private final JobLauncher jobLauncher;
    private final Job importDhJob;
    private final Job importPfJob;
    private final SiafiBatchService siafiBatchService;

    private static final String BASE_DIR = "siafi-files";
    private static final String INCOMING_DIR = BASE_DIR + "/incoming";
    private static final String PROCESSED_DIR = BASE_DIR + "/processed";
    private static final String FAILED_DIR = BASE_DIR + "/failed";

    public FileAutoConsumerService(JobLauncher jobLauncher, Job importDhJob, Job importPfJob, SiafiBatchService siafiBatchService) {
        this.jobLauncher = jobLauncher;
        this.importDhJob = importDhJob;
        this.importPfJob = importPfJob;
        this.siafiBatchService = siafiBatchService;
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(INCOMING_DIR));
            Files.createDirectories(Paths.get(PROCESSED_DIR));
            Files.createDirectories(Paths.get(FAILED_DIR));
            LOGGER.info("SIAFI batch file storage folders initialized at: " + new File(BASE_DIR).getAbsolutePath());
        } catch (Exception e) {
            LOGGER.severe("Failed to create file monitoring directories: " + e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 10000) // Scan every 10 seconds
    public void scanAndProcessFiles() {
        File folder = new File(INCOMING_DIR);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xml") || name.endsWith(".csv"));

        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            LOGGER.info("Detected incoming file: " + file.getName());
            siafiBatchService.addNotification("Novo arquivo detectado na watch folder: " + file.getName(), "INFO");
            processFile(file);
        }
    }

    public void processFile(File file) {
        String filename = file.getName();
        String layout = detectLayout(file);
        
        if (layout == null) {
            LOGGER.severe("Could not detect SIAFI layout for file: " + filename + ". Moving to failed.");
            siafiBatchService.addNotification("Layout não identificado para o arquivo: " + filename, "ERROR");
            moveFile(file, FAILED_DIR);
            return;
        }

        try {
            Job jobToRun = "DH001".equals(layout) ? importDhJob : importPfJob;
            LOGGER.info("Starting Batch Job for file: " + filename + " using layout: " + layout);
            siafiBatchService.addNotification("Processando lote de " + layout + " para o arquivo: " + filename, "INFO");

            JobParameters params = new JobParametersBuilder()
                    .addString("filePath", file.getAbsolutePath())
                    .addString("fileType", filename.endsWith(".csv") ? "CSV" : "XML")
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(jobToRun, params);

            LOGGER.info("Successfully finished Batch Job for file: " + filename);
            siafiBatchService.addNotification("Lote processado com sucesso: " + filename, "SUCCESS");
            moveFile(file, PROCESSED_DIR);
        } catch (Exception e) {
            LOGGER.severe("Error running Batch Job for file: " + filename + ". Reason: " + e.getMessage());
            siafiBatchService.addNotification("Falha no processamento do lote (" + filename + "): " + e.getMessage(), "ERROR");
            moveFile(file, FAILED_DIR);
        }
    }

    private String detectLayout(File file) {
        String filename = file.getName().toLowerCase();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int linesRead = 0;
            while ((line = reader.readLine()) != null && linesRead < 20) {
                if (line.contains("DH001") || line.contains("CprDhCadastrar") || line.contains("cpr:")) {
                    return "DH001";
                }
                if (line.contains("PF001") || line.contains("PFDTO") || line.contains("ns3:PFDTO")) {
                    return "PF001";
                }
                linesRead++;
            }
        } catch (Exception e) {
            LOGGER.severe("Error scanning file content for layout type: " + e.getMessage());
        }

        if (filename.contains("dh") || filename.contains("documento")) {
            return "DH001";
        } else if (filename.contains("pf") || filename.contains("programacao")) {
            return "PF001";
        }
        return null;
    }

    private void moveFile(File file, String destDir) {
        try {
            Path source = file.toPath();
            Path target = Paths.get(destDir).resolve(file.getName());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Moved file " + file.getName() + " to " + destDir);
        } catch (Exception e) {
            LOGGER.severe("Failed to move file " + file.getName() + " to " + destDir + ": " + e.getMessage());
        }
    }

    public String getIncomingDirPath() {
        return new File(INCOMING_DIR).getAbsolutePath();
    }
}
