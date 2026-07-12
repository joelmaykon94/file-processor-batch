package com.example.siafibatch.repository;

import com.example.siafibatch.model.DhCarga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DhCargaRepository extends JpaRepository<DhCarga, Long> {
}
