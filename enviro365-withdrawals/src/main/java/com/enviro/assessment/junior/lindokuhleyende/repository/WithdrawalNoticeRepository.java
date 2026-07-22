package com.enviro.assessment.junior.lindokuhleyende.repository;

import com.enviro.assessment.junior.lindokuhleyende.entity.WithdrawalNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WithdrawalNoticeRepository
        extends JpaRepository<WithdrawalNotice, Long>, JpaSpecificationExecutor<WithdrawalNotice> {

    List<WithdrawalNotice> findByInvestorIdOrderByRequestDateDesc(Long investorId);
}
