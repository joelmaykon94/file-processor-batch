package com.example.siafibatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pf_detalhe")
public class PfDetalhe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pf_carga_id")
    @JsonIgnore
    private PfCarga pfCarga;

    private String tipoPf;
    private String observacao;
    private String codUgEmit;
    private String codUgFavorecida;
    private Boolean limiteDeSaque;
    
    // item PF details
    private BigDecimal vlr;
    private String codVinc;
    private String codFontRecur;
    private String codCtgoGasto;
    private String codSit;
    private String txtInscrA;

    private String numeroDocumento;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PfCarga getPfCarga() { return pfCarga; }
    public void setPfCarga(PfCarga pfCarga) { this.pfCarga = pfCarga; }
    public String getTipoPf() { return tipoPf; }
    public void setTipoPf(String tipoPf) { this.tipoPf = tipoPf; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getCodUgEmit() { return codUgEmit; }
    public void setCodUgEmit(String codUgEmit) { this.codUgEmit = codUgEmit; }
    public String getCodUgFavorecida() { return codUgFavorecida; }
    public void setCodUgFavorecida(String codUgFavorecida) { this.codUgFavorecida = codUgFavorecida; }
    public Boolean getLimiteDeSaque() { return limiteDeSaque; }
    public void setLimiteDeSaque(Boolean limiteDeSaque) { this.limiteDeSaque = limiteDeSaque; }
    public BigDecimal getVlr() { return vlr; }
    public void setVlr(BigDecimal vlr) { this.vlr = vlr; }
    public String getCodVinc() { return codVinc; }
    public void setCodVinc(String codVinc) { this.codVinc = codVinc; }
    public String getCodFontRecur() { return codFontRecur; }
    public void setCodFontRecur(String codFontRecur) { this.codFontRecur = codFontRecur; }
    public String getCodCtgoGasto() { return codCtgoGasto; }
    public void setCodCtgoGasto(String codCtgoGasto) { this.codCtgoGasto = codCtgoGasto; }
    public String getCodSit() { return codSit; }
    public void setCodSit(String codSit) { this.codSit = codSit; }
    public String getTxtInscrA() { return txtInscrA; }
    public void setTxtInscrA(String txtInscrA) { this.txtInscrA = txtInscrA; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
}
