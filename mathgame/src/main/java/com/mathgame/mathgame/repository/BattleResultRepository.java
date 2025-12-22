package com.mathgame.mathgame.repository;

import com.mathgame.mathgame.dto.BattleRankingRowDto;
import com.mathgame.mathgame.entity.BattleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BattleResultRepository extends JpaRepository<BattleResult, Long> {

    List<BattleResult> findByUsernameOrderByPlayedAtDesc(String username);

    @Query("""
        select new com.mathgame.mathgame.dto.BattleRankingRowDto(
            b.username,
            coalesce(sum(b.scoreDelta), 0),
            count(b.id),
            sum(case when b.outcome = com.mathgame.mathgame.entity.BattleOutcome.WIN then 1 else 0 end),
            sum(case when b.outcome = com.mathgame.mathgame.entity.BattleOutcome.DRAW then 1 else 0 end),
            sum(case when b.outcome = com.mathgame.mathgame.entity.BattleOutcome.LOSE then 1 else 0 end)
        )
        from BattleResult b
        group by b.username
        order by coalesce(sum(b.scoreDelta),0) desc,
                 sum(case when b.outcome = com.mathgame.mathgame.entity.BattleOutcome.WIN then 1 else 0 end) desc,
                 count(b.id) desc
    """)
    List<BattleRankingRowDto> getBattleRanking();
}
