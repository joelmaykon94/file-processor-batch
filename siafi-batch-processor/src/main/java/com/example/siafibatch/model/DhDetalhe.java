package com.example.siafibatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "dh_detalhe")
public class DhDetalhe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dh_carga_id")
    @JsonIgnore
    private DhCarga dhCarga;

    private String codUgEmit;
    private String anoDh;
    private String codTipoDh;
    private String numDh;

    // basic data
    private String dtEmis;
    private String dtVenc;
    private String codUgPgto;
    private BigDecimal vlr;
    private String txtObser;
    private String txtProcesso;
    private String dtAteste;
    private String codCredorDevedor;
    private String dtPgtoReceb;

    // origin doc
    private String docOrigemIdentEmit;
    private String docOrigemNum;
    private BigDecimal docOrigemVlr;

    // commitment (pco)
    private String pcoSit;
    private String pcoUgEmpe;
    private String pcoEmpeNum;
    private BigDecimal pcoVlr;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public DhCarga getDhCarga() { return dhCarga; }
    public void setDhCarga(DhCarga dhCarga) { this.dhCarga = dhCarga; }
    public String getCodUgEmit() { return codUgEmit; }
    public void setCodUgEmit(String codUgEmit) { this.codUgEmit = codUgEmit; }
    public String getAnoDh() { return anoDh; }
    public void setAnoDh(String anoDh) { this.anoDh = anoDh; }
    public String getCodTipoDh() { return codTipoDh; }
    public void setCodTipoDh(String codTipoDh) { this.codTipoDh = codTipoDh; }
    public String getNumDh() { return numDh; }
    public void setNumDh(String numDh) { this.numDh = numDh; }
    public String getDtEmis() { return dtEmis; }
    public void setDtEmis(String dtEmis) { this.dtEmis = dtEmis; }
    public String getDtVenc() { return dtVenc; }
    public void setDtVenc(String dtVenc) { this.dtVenc = dtVenc; }
    public String getCodUgPgto() { return codUgPgto; }
    public void setCodUgPgto(String codUgPgto) { this.codUgPgto = codUgPgto; }
    public BigDecimal getVlr() { return vlr; }
    public void setVlr(BigDecimal vlr) { this.vlr = vlr; }
    public String getTxtObser() { return txtObser; }
    public void setTxtObser(String txtObser) { this.txtObser = txtObser; }
    public String getTxtProcesso() { return txtProcesso; }
    public void setTxtProcesso(String txtProcesso) { this.txtProcesso = txtProcesso; }
    public String getDtAteste() { return dtAteste; }
    public void setDtAteste(String dtAteste) { this.dtAteste = dtAteste; }
    public String getCodCredorDevedor() { return codCredorDevedor; }
    public void setCodCredorDevedor(String codCredorDevedor) { this.codCredorDevedor = codCredorDevedor; }
    public String getDtPgtoReceb() { return dtPgtoReceb; }
    public void setDtPgtoReceb(String dtPgtoReceb) { this.dtPgtoReceb = dtPgtoReceb; }
    public String getDocOrigemIdentEmit() { return docOrigemIdentEmit; }
    public void setDocOrigemIdentEmit(String docOrigemIdentEmit) { this.docOrigemIdentEmit = docOrigemIdentEmit; }
    public String getDocOrigemNum() { return docOrigemNum; }
    public void setDocOrigemNum(String docOrigemNum) { this.docOrigemNum = docOrigemNum; }
    public BigDecimal getDocOrigemVlr() { return docOrigemVlr; }
    public void setDocOrigemVlr(BigDecimal docOrigemVlr) { this.docOrigemVlr = docOrigemVlr; }
    public String getPcoSit() { return pcoSit; }
    public void setPcoSit(String pcoSit) { this.pcoSit = pcoSit; }
    public String getPcoUgEmpe() { return pcoUgEmpe; }
    public void setPcoUgEmpe(String pcoUgEmpe) { this.pcoUgEmpe = pcoUgEmpe; }
    public String getPcoEmpeNum() { return pcoEmpeNum; }
    public void setPcoEmpeNum(String pcoEmpeNum) { this.pcoEmpeNum = pcoEmpeNum; }
    public BigDecimal getPcoVlr() { return pcoVlr; }
    public void setPcoVlr(BigDecimal pcoVlr) { this.pcoVlr = pcoVlr; }
}
