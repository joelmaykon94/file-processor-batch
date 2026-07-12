package com.example.siafibatch.controller.dto;

import java.math.BigDecimal;

public record DhDetalheDto(
    Long id,
    String codUgEmit,
    String anoDh,
    String codTipoDh,
    String numDh,
    String dtEmis,
    String dtVenc,
    String codUgPgto,
    BigDecimal vlr,
    String txtObser,
    String txtProcesso,
    String dtAteste,
    String codCredorDevedor,
    String dtPgtoReceb,
    String docOrigemIdentEmit,
    String docOrigemNum,
    BigDecimal docOrigemVlr,
    String pcoSit,
    String pcoUgEmpe,
    String pcoEmpeNum,
    BigDecimal pcoVlr
) {}
