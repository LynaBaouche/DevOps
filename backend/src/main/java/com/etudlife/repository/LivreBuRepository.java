package com.etudlife.repository;



import com.etudlife.model.LivreBu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivreBuRepository extends JpaRepository<LivreBu, Long> {

    List<LivreBu> findByDisponibleTrue();
}
