package com.example.siafibatch.controller;

import com.example.siafibatch.controller.dto.DhCargaDto;
import com.example.siafibatch.controller.dto.PfCargaDto;
import com.example.siafibatch.controller.dto.SiafiMapper;
import com.example.siafibatch.controller.dto.SiafiNotification;
import com.example.siafibatch.service.FileAutoConsumerService;
import com.example.siafibatch.service.SiafiBatchService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/siafi")
@CrossOrigin(origins = "*")
public class SiafiBatchController {

    private final SiafiBatchService siafiBatchService;
    private final FileAutoConsumerService fileAutoConsumerService;

    public SiafiBatchController(SiafiBatchService siafiBatchService,
                                FileAutoConsumerService fileAutoConsumerService) {
        this.siafiBatchService = siafiBatchService;
        this.fileAutoConsumerService = fileAutoConsumerService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("active", true);
        status.put("incomingDir", fileAutoConsumerService.getIncomingDirPath());
        status.put("dhCount", siafiBatchService.getDhCount());
        status.put("pfCount", siafiBatchService.getPfCount());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/dh-batches")
    public ResponseEntity<List<DhCargaDto>> getDhBatches() {
        List<DhCargaDto> dtos = siafiBatchService.getDhBatches().stream()
                .map(SiafiMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/pf-batches")
    public ResponseEntity<List<PfCargaDto>> getPfBatches() {
        List<PfCargaDto> dtos = siafiBatchService.getPfBatches().stream()
                .map(SiafiMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        Map<String, String> response = new HashMap<>();
        if (multipartFile.isEmpty()) {
            response.put("error", "Empty file provided.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null) originalFilename = "uploaded_file.xml";

            // Save to incoming folder to be picked up
            String incomingPath = fileAutoConsumerService.getIncomingDirPath();
            File destFile = new File(incomingPath + File.separator + originalFilename);
            
            try (InputStream is = multipartFile.getInputStream();
                 FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // Manually trigger processing
            fileAutoConsumerService.processFile(destFile);

            response.put("message", "File uploaded and processed successfully: " + originalFilename);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Error uploading/processing file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearData() {
        siafiBatchService.clearData();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All database load history cleared successfully.");
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // NOTIFICATIONS ENDPOINTS
    // ==========================================

    @GetMapping("/notifications")
    public ResponseEntity<List<SiafiNotification>> getNotifications() {
        return ResponseEntity.ok(siafiBatchService.getNotifications());
    }

    @PostMapping("/notifications/read")
    public ResponseEntity<Map<String, String>> markNotificationsAsRead() {
        siafiBatchService.markAllAsRead();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read.");
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // SAMPLES GENERATORS FOR TESTING
    // ==========================================

    @GetMapping("/samples/dh/xml")
    public ResponseEntity<String> getSampleDhXml() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DH_Sample.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    @GetMapping("/samples/pf/xml")
    public ResponseEntity<String> getSamplePfXml() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PF_Sample.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    @GetMapping("/samples/dh/csv")
    public ResponseEntity<String> getSampleDhCsv() {
        String csv = "layout,codUgEmit,anoDH,codTipoDH,numDH,dtEmis,dtVenc,codUgPgto,vlr,txtObser,txtProcesso,dtAteste,codCredorDevedor,dtPgtoReceb,docOrigemIdentEmit,docOrigemNum,docOrigemVlr,pcoSit,pcoUgEmpe,pcoEmpeNum,pcoVlr\n" +
                "DH001,080014,2026,NP,990202,2026-07-10,2026-07-15,080014,2750.50,CSV DH LOAD TEST,98765432109876543210,2026-07-10,05462743000954,2026-07-15,05462743000954,CSVORIGEM123,2750.50,DSP001,080014,2026NE9902,2750.50";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DH_Sample.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }

    @GetMapping("/samples/pf/csv")
    public ResponseEntity<String> getSamplePfCsv() {
        String csv = "layout,tipoPF,observacao,codUgEmit,codUgFavorecida,limiteDeSaque,vlr,codVinc,codFontRecur,codCtgoGasto,codSit,txtInscrA,numeroDocumento\n" +
                "PF001,TRF,CSV PF LOAD TEST,160075,160348,true,9900.00,415,0100000000,D,TRF020,675901,889900";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PF_Sample.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
}
