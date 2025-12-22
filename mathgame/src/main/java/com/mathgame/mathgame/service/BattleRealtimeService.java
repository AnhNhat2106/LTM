package com.mathgame.mathgame.service;

import com.mathgame.mathgame.dto.battle.BattleQuestionItem;
import com.mathgame.mathgame.dto.ws.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class BattleRealtimeService {

    private final SimpMessagingTemplate ws;
    private final BattleResultService battleResultService;
    private final BattleQuestionBankService questionBank;

    private final Queue<String> waiting = new ConcurrentLinkedQueue<>();
    private final Map<String, RoomState> rooms = new ConcurrentHashMap<>();

    public BattleRealtimeService(SimpMessagingTemplate ws,
                                 BattleResultService battleResultService,
                                 BattleQuestionBankService questionBank) {
        this.ws = ws;
        this.battleResultService = battleResultService;
        this.questionBank = questionBank;
    }

    // ===================== QUEUE =====================

    public void joinQueue(String username) {
        if (username == null || username.isBlank()) return;
        if (waiting.contains(username)) return;

        waiting.add(username);
        ws.convertAndSendToUser(username, "/queue/system",
                new SystemEvent("QUEUING", "Đang tìm đối thủ..."));

        tryMatch();
    }

    public void cancelQueue(String username) {
        if (username == null) return;

        boolean removed = waiting.remove(username);
        if (removed) {
            ws.convertAndSendToUser(username, "/queue/system",
                    new SystemEvent("IDLE", "Đã hủy tìm trận"));
        }
    }

    private synchronized void tryMatch() {
        if (waiting.size() < 2) return;

        String p1 = waiting.poll();
        String p2 = waiting.poll();
        if (p1 == null || p2 == null) return;

        // trÃ¡nh match cÃ¹ng 1 user (hiáº¿m nhÆ°ng Ä‘á»ƒ cháº¯c)
        if (p1.equalsIgnoreCase(p2)) {
            waiting.add(p1);
            return;
        }

        String matchId = UUID.randomUUID().toString();
        RoomState room = new RoomState(matchId, p1, p2);

        // âœ… random cÃ¢u há»i cho match (5 cÃ¢u hoáº·c Ã­t hÆ¡n náº¿u pool Ã­t)
        List<BattleQuestionItem> pool = new ArrayList<>(questionBank.getAll());
        Collections.shuffle(pool);
        room.questions = pool.subList(0, Math.min(5, pool.size()));

        rooms.put(matchId, room);

        // âœ… chá»‰ thÃ´ng bÃ¡o "match found" -> CHÆ¯A start
        MatchFoundEvent evt = new MatchFoundEvent(matchId, p1, p2);
        ws.convertAndSendToUser(p1, "/queue/match", evt);
        ws.convertAndSendToUser(p2, "/queue/match", evt);
    }

    // ===================== CONFIRM MATCH =====================

    public void acceptMatch(String username, String matchId) {
        RoomState room = rooms.get(matchId);
        if (room == null) return;
        if (!room.isPlayer(username)) return;

        room.accepted.add(username);

        ws.convertAndSendToUser(username, "/queue/system",
                new SystemEvent("CONFIRM", "Bạn đã xác nhận. Đang chờ đối thủ..."));

        // âœ… cáº£ 2 ngÆ°á»i accept -> báº¯t Ä‘áº§u tráº­n
        if (room.accepted.size() == 2) {
            ws.convertAndSendToUser(room.p1, "/queue/system",
                    new SystemEvent("PLAYING", "Bắt đầu trận!"));
            ws.convertAndSendToUser(room.p2, "/queue/system",
                    new SystemEvent("PLAYING", "Bắt đầu trận!"));

            room.index = 0;
            sendQuestion(room);
        }
    }

    public void declineMatch(String username, String matchId) {
        RoomState room = rooms.get(matchId);
        if (room == null) return;
        if (!room.isPlayer(username)) return;

        String other = room.other(username);

        ws.convertAndSendToUser(username, "/queue/system",
                new SystemEvent("IDLE", "Bạn đã từ chối trận."));
        ws.convertAndSendToUser(other, "/queue/system",
                new SystemEvent("IDLE", "Đối thủ từ chối trận."));

        rooms.remove(matchId);
    }

    // ===================== GAMEPLAY =====================

    private void sendQuestion(RoomState room) {
        int idx = room.index;

        // an toÃ n: náº¿u háº¿t cÃ¢u -> end
        if (room.questions == null || idx >= room.questions.size()) {
            endMatch(room);
            return;
        }

        BattleQuestionItem q = room.questions.get(idx);

        // táº¡o options A/B/C/D tá»« Ä‘Ã¡p Ã¡n sá»‘
        OptGen gen = genOptions(q.getAnswer());

        room.openQuestion(idx);
        room.correctChoiceByIndex.put(idx, gen.correctChoice);

        ws.convertAndSend("/topic/match/" + room.matchId,
                new QuestionEvent(room.matchId, idx, room.questions.size(), q.getText(), gen.options, 10));
    }

    public void submitAnswer(String username, AnswerRequest req) {
        RoomState room = rooms.get(req.getMatchId());
        if (room == null) return;
        if (!room.isPlayer(username)) return;

        // âœ… chÆ°a start vÃ¬ chÆ°a Ä‘á»§ accept -> bá» qua
        if (room.accepted.size() < 2) return;

        // âœ… chá»‰ cho tráº£ lá»i Ä‘Ãºng cÃ¢u hiá»‡n táº¡i
        if (req.getIndex() != room.index) return;

        // âœ… anti-spam: má»—i user chá»‰ tráº£ lá»i 1 láº§n / cÃ¢u
        if (!room.firstTimeAnswer(room.index, username)) return;

        // âœ… cháº¥m Ä‘Ãºng/sai theo Ä‘Ã¡p Ã¡n tháº­t
        String correctChoice = room.correctChoiceByIndex.get(room.index);
        boolean correct = correctChoice != null && correctChoice.equalsIgnoreCase(req.getChoice());

        room.answer(username, correct);

        // cáº­p nháº­t Ä‘iá»ƒm realtime
        ws.convertAndSend("/topic/match/" + room.matchId,
                new ScoreUpdateEvent(room.matchId, room.p1, room.p2, room.score1, room.score2, room.remainSec()));

        // sang cÃ¢u tiáº¿p theo / káº¿t thÃºc
        if (room.index >= room.questions.size() - 1) {
            endMatch(room);
        } else {
            room.index++;
            sendQuestion(room);
        }
    }

    private void endMatch(RoomState room) {
        // âœ… lÆ°u DB theo rule +10/-5/+5
        battleResultService.recordBattle(room.p1, room.p2, room.score1, room.score2);

        int deltaP1 = BattlePointRule.delta(room.score1, room.score2);
        int deltaP2 = BattlePointRule.delta(room.score2, room.score1);

        ws.convertAndSendToUser(room.p1, "/queue/system",
                new SystemEvent("RESULT", "Trận đấu kết thúc!"));
        ws.convertAndSendToUser(room.p2, "/queue/system",
                new SystemEvent("RESULT", "Trận đấu kết thúc!"));

        ws.convertAndSend("/topic/match/" + room.matchId,
                new MatchEndEvent(room.matchId, room.p1, room.p2, room.score1, room.score2));

        ws.convertAndSendToUser(room.p1, "/queue/result",
                new ResultEvent(room.matchId, room.p1, room.p2, room.score1, room.score2, deltaP1));
        ws.convertAndSendToUser(room.p2, "/queue/result",
                new ResultEvent(room.matchId, room.p1, room.p2, room.score1, room.score2, deltaP2));

        rooms.remove(room.matchId);
    }

    // ===================== OPTIONS GENERATOR =====================

    private static String letter(int i) {
        return i == 0 ? "A" : i == 1 ? "B" : i == 2 ? "C" : "D";
    }

    private static class OptGen {
        String[] options;     // "A) 12", ...
        String correctChoice; // "A"/"B"/"C"/"D"
    }

    private OptGen genOptions(int correct) {
        Random rd = new Random();

        // táº¡o 3 Ä‘Ã¡p Ã¡n sai gáº§n Ä‘Ãºng
        Set<Integer> set = new LinkedHashSet<>();
        set.add(correct);
        while (set.size() < 4) {
            int cand = correct + (rd.nextInt(9) - 4); // -4..+4
            if (cand < 0 || cand == correct) continue;
            set.add(cand);
        }

        List<Integer> list = new ArrayList<>(set);
        Collections.shuffle(list);

        OptGen g = new OptGen();
        g.options = new String[4];

        for (int i = 0; i < 4; i++) {
            String ch = letter(i);
            g.options[i] = ch + ") " + list.get(i);
            if (list.get(i) == correct) g.correctChoice = ch;
        }
        return g;
    }

    // ===================== STATE =====================

    static class RoomState {
        final String matchId;
        final String p1;
        final String p2;

        int index = 0;
        int score1 = 0;
        int score2 = 0;

        // âœ… confirm
        final Set<String> accepted = ConcurrentHashMap.newKeySet();

        // âœ… anti-spam per question
        final Map<Integer, Set<String>> answered = new ConcurrentHashMap<>();

        // âœ… timer (táº¡m demo fixed 10s, báº¡n nÃ¢ng cáº¥p countdown sau)
        long questionDeadlineMs = 0;

        // âœ… cÃ¢u há»i + Ä‘Ã¡p Ã¡n Ä‘Ãºng theo A/B/C/D cho tá»«ng index
        List<BattleQuestionItem> questions;
        Map<Integer, String> correctChoiceByIndex = new ConcurrentHashMap<>();

        RoomState(String matchId, String p1, String p2) {
            this.matchId = matchId;
            this.p1 = p1;
            this.p2 = p2;
        }

        boolean isPlayer(String u) { return p1.equals(u) || p2.equals(u); }
        String other(String u) { return p1.equals(u) ? p2 : p1; }

        void openQuestion(int idx) {
            questionDeadlineMs = System.currentTimeMillis() + 10_000;
            answered.put(idx, ConcurrentHashMap.newKeySet());
        }

        int remainSec() {
            long ms = questionDeadlineMs - System.currentTimeMillis();
            return (int) Math.max(0, (ms + 999) / 1000);
        }

        boolean firstTimeAnswer(int idx, String username) {
            answered.putIfAbsent(idx, ConcurrentHashMap.newKeySet());
            return answered.get(idx).add(username);
        }

        void answer(String user, boolean correct) {
            if (!correct) return;
            if (p1.equals(user)) score1++;
            else if (p2.equals(user)) score2++;
        }
    }

    // âœ… rule battle point +10/-5/+5
    static class BattlePointRule {
        static int delta(int myScore, int oppScore) {
            if (myScore > oppScore) return 10;
            if (myScore < oppScore) return -5;
            return 5;
        }
    }
}

