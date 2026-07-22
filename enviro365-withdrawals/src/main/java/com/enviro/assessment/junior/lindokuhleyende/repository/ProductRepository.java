package com.enviro.assessment.junior.lindokuhleyende.repository;

import com.enviro.assessment.junior.lindokuhleyende.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByInvestorId(Long investorId);
}
