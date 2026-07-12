package com.example.siafibatch.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DhCargaDto(
    Long id,
    String fileName,
    String codigoLayout,
    String dataGeracao,
    String sequencialGeracao,
    String anoReferencia,
    String ugResponsavel,
    String cpfResponsavel,
    int quantidadeDetalhesXml,
    int quantidadeDetalhesProcessados,
    String status,
    String logProcessamento,
    LocalDateTime dataProcessamento,
    List<DhDetalheDto> detalhes
) {}
