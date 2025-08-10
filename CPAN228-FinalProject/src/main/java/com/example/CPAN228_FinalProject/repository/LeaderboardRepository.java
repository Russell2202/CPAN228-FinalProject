package com.example.CPAN228_FinalProject.repository;

import com.example.CPAN228_FinalProject.model.LeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardEntry, Long> {
    List<LeaderboardEntry> findTop10ByOrderByScoreDesc();
}
