package com.mathgame.mathgame.repository;

import com.mathgame.mathgame.dto.RankingRowDto;
import com.mathgame.mathgame.entity.PracticeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PracticeHistoryRepository extends JpaRepository<PracticeHistory, Long> {

    List<PracticeHistory> findByUsernameOrderByIdDesc(String username);

    @Query("""
        select new com.mathgame.mathgame.dto.RankingRowDto(
            p.username,
            coalesce(sum(p.score), 0),
            count(p.id),
            coalesce(sum(p.correctAnswers), 0)
        )
        from PracticeHistory p
        group by p.username
        order by coalesce(sum(p.score),0) desc, count(p.id) desc
    """)
    List<RankingRowDto> getRanking();
}
