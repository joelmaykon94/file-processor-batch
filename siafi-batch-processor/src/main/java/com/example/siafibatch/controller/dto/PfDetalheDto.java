package com.example.siafibatch.controller.dto;

import java.math.BigDecimal;

public record PfDetalheDto(
    Long id,
    String tipoPf,
    String observacao,
    String codUgEmit,
    String codUgFavorecida,
    Boolean limiteDeSaque,
    BigDecimal vlr,
    String codVinc,
    String codFontRecur,
    String codCtgoGasto,
    String codSit,
    String txtInscrA,
    String numeroDocumento
) {}
