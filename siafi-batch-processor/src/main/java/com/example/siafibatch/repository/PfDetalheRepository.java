package com.example.siafibatch.repository;

import com.example.siafibatch.model.PfDetalhe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PfDetalheRepository extends JpaRepository<PfDetalhe, Long> {
}
