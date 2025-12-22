package com.mathgame.mathgame.controller.ws;

import com.mathgame.mathgame.dto.ws.AnswerRequest;
import com.mathgame.mathgame.dto.ws.MatchDecisionRequest;
import com.mathgame.mathgame.service.BattleRealtimeService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class BattleWsController {

    private final BattleRealtimeService service;

    public BattleWsController(BattleRealtimeService service) {
        this.service = service;
    }

    // ✅ tìm đối thủ (join queue)
    @MessageMapping("/battle/queue/join")
    public void joinQueue(Authentication auth) {
        service.joinQueue(auth.getName());
    }

    // ✅ hủy tìm trận
    @MessageMapping("/battle/queue/cancel")
    public void cancelQueue(Authentication auth) {
        service.cancelQueue(auth.getName());
    }

    // ✅ xác nhận thi đấu
    @MessageMapping("/battle/match/accept")
    public void accept(Authentication auth, MatchDecisionRequest req) {
        service.acceptMatch(auth.getName(), req.getMatchId());
    }

    // ✅ từ chối thi đấu
    @MessageMapping("/battle/match/decline")
    public void decline(Authentication auth, MatchDecisionRequest req) {
        service.declineMatch(auth.getName(), req.getMatchId());
    }

    // ✅ trả lời câu hỏi
    @MessageMapping("/battle/answer")
    public void answer(Authentication auth, AnswerRequest req) {
        service.submitAnswer(auth.getName(), req);
    }
}
