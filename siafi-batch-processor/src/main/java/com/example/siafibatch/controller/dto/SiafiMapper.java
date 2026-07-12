package com.example.siafibatch.controller.dto;

import com.example.siafibatch.model.DhCarga;
import com.example.siafibatch.model.DhDetalhe;
import com.example.siafibatch.model.PfCarga;
import com.example.siafibatch.model.PfDetalhe;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SiafiMapper {

    public static DhCargaDto toDto(DhCarga entity) {
        if (entity == null) return null;
        
        List<DhDetalheDto> details = entity.getDetalhes() != null ?
                entity.getDetalhes().stream().map(SiafiMapper::toDto).collect(Collectors.toList()) :
                Collections.emptyList();
                
        return new DhCargaDto(
            entity.getId(),
            entity.getFileName(),
            entity.getCodigoLayout(),
            entity.getDataGeracao(),
            entity.getSequencialGeracao(),
            entity.getAnoReferencia(),
            entity.getUgResponsavel(),
            entity.getCpfResponsavel(),
            entity.getQuantidadeDetalhesXml(),
            entity.getQuantidadeDetalhesProcessados(),
            entity.getStatus(),
            entity.getLogProcessamento(),
            entity.getDataProcessamento(),
            details
        );
    }

    public static DhDetalheDto toDto(DhDetalhe entity) {
        if (entity == null) return null;
        
        return new DhDetalheDto(
            entity.getId(),
            entity.getCodUgEmit(),
            entity.getAnoDh(),
            entity.getCodTipoDh(),
            entity.getNumDh(),
            entity.getDtEmis(),
            entity.getDtVenc(),
            entity.getCodUgPgto(),
            entity.getVlr(),
            entity.getTxtObser(),
            entity.getTxtProcesso(),
            entity.getDtAteste(),
            entity.getCodCredorDevedor(),
            entity.getDtPgtoReceb(),
            entity.getDocOrigemIdentEmit(),
            entity.getDocOrigemNum(),
            entity.getDocOrigemVlr(),
            entity.getPcoSit(),
            entity.getPcoUgEmpe(),
            entity.getPcoEmpeNum(),
            entity.getPcoVlr()
        );
    }

    public static PfCargaDto toDto(PfCarga entity) {
        if (entity == null) return null;
        
        List<PfDetalheDto> details = entity.getDetalhes() != null ?
                entity.getDetalhes().stream().map(SiafiMapper::toDto).collect(Collectors.toList()) :
                Collections.emptyList();

        return new PfCargaDto(
            entity.getId(),
            entity.getFileName(),
            entity.getCodigoLayout(),
            entity.getDataGeracao(),
            entity.getSequencialGeracao(),
            entity.getAnoReferencia(),
            entity.getUgResponsavel(),
            entity.getCpfResponsavel(),
            entity.getQuantidadeDetalhesXml(),
            entity.getQuantidadeDetalhesProcessados(),
            entity.getStatus(),
            entity.getLogProcessamento(),
            entity.getDataProcessamento(),
            details
        );
    }

    public static PfDetalheDto toDto(PfDetalhe entity) {
        if (entity == null) return null;
        
        return new PfDetalheDto(
            entity.getId(),
            entity.getTipoPf(),
            entity.getObservacao(),
            entity.getCodUgEmit(),
            entity.getCodUgFavorecida(),
            entity.getLimiteDeSaque(),
            entity.getVlr(),
            entity.getCodVinc(),
            entity.getCodFontRecur(),
            entity.getCodCtgoGasto(),
            entity.getCodSit(),
            entity.getTxtInscrA(),
            entity.getNumeroDocumento()
        );
    }
}
