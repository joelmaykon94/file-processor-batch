package com.example.siafibatch.repository;

import com.example.siafibatch.model.DhDetalhe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DhDetalheRepository extends JpaRepository<DhDetalhe, Long> {
}
