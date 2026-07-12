package com.example.siafibatch.repository;

import com.example.siafibatch.model.PfCarga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PfCargaRepository extends JpaRepository<PfCarga, Long> {
}
