package com.example.siafibatch.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dh_carga")
public class DhCarga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String codigoLayout;
    private String dataGeracao;
    private String sequencialGeracao;
    private String anoReferencia;
    private String ugResponsavel;
    private String cpfResponsavel;
    private int quantidadeDetalhesXml;
    private int quantidadeDetalhesProcessados;
    private String status; // SUCCESS, PARTIAL_SUCCESS, FAILED
    private String logProcessamento;
    private LocalDateTime dataProcessamento;

    @OneToMany(mappedBy = "dhCarga", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DhDetalhe> detalhes = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getCodigoLayout() { return codigoLayout; }
    public void setCodigoLayout(String codigoLayout) { this.codigoLayout = codigoLayout; }
    public String getDataGeracao() { return dataGeracao; }
    public void setDataGeracao(String dataGeracao) { this.dataGeracao = dataGeracao; }
    public String getSequencialGeracao() { return sequencialGeracao; }
    public void setSequencialGeracao(String sequencialGeracao) { this.sequencialGeracao = sequencialGeracao; }
    public String getAnoReferencia() { return anoReferencia; }
    public void setAnoReferencia(String anoReferencia) { this.anoReferencia = anoReferencia; }
    public String getUgResponsavel() { return ugResponsavel; }
    public void setUgResponsavel(String ugResponsavel) { this.ugResponsavel = ugResponsavel; }
    public String getCpfResponsavel() { return cpfResponsavel; }
    public void setCpfResponsavel(String cpfResponsavel) { this.cpfResponsavel = cpfResponsavel; }
    public int getQuantidadeDetalhesXml() { return quantidadeDetalhesXml; }
    public void setQuantidadeDetalhesXml(int quantidadeDetalhesXml) { this.quantidadeDetalhesXml = quantidadeDetalhesXml; }
    public int getQuantidadeDetalhesProcessados() { return quantidadeDetalhesProcessados; }
    public void setQuantidadeDetalhesProcessados(int quantidadeDetalhesProcessados) { this.quantidadeDetalhesProcessados = quantidadeDetalhesProcessados; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLogProcessamento() { return logProcessamento; }
    public void setLogProcessamento(String logProcessamento) { this.logProcessamento = logProcessamento; }
    public LocalDateTime getDataProcessamento() { return dataProcessamento; }
    public void setDataProcessamento(LocalDateTime dataProcessamento) { this.dataProcessamento = dataProcessamento; }
    public List<DhDetalhe> getDetalhes() { return detalhes; }
    public void setDetalhes(List<DhDetalhe> detalhes) { this.detalhes = detalhes; }
    
    public void addDetalhe(DhDetalhe detalhe) {
        detalhes.add(detalhe);
        detalhe.setDhCarga(this);
    }
}
