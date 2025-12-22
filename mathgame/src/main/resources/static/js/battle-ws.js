let stompClient = null;
let currentMatchId = null;

const $ = (id) => document.getElementById(id);

function log(msg) {
  const box = $("log");
  if (!box) return;
  box.textContent += msg + "\n";
  box.scrollTop = box.scrollHeight;
}

function setState({ status, showFind, showCancel, showConfirm, showPlay, showResult }) {
  $("status").textContent = status ?? "";
  $("btnFind").style.display = showFind ? "inline-block" : "none";
  $("btnCancel").style.display = showCancel ? "inline-block" : "none";
  $("confirmBox").style.display = showConfirm ? "block" : "none";
  $("playBox").style.display = showPlay ? "block" : "none";
  $("resultBox").style.display = showResult ? "block" : "none";
}

function setIdle(msg = "Sẵn sàng") {
  setState({ status: msg, showFind: true, showCancel: false, showConfirm: false, showPlay: false, showResult: false });
  currentMatchId = null;
}

function setQueuing(msg = "Đang tìm đối thủ...") {
  setState({ status: msg, showFind: false, showCancel: true, showConfirm: false, showPlay: false, showResult: false });
}

function setFound(msg = "Tìm thấy đối thủ!") {
  setState({ status: msg, showFind: false, showCancel: false, showConfirm: true, showPlay: false, showResult: false });
}

function setPlaying(msg = "Đang thi đấu") {
  setState({ status: msg, showFind: false, showCancel: false, showConfirm: false, showPlay: true, showResult: false });
}

function setResult(msg = "Trận đấu kết thúc!") {
  setState({ status: msg, showFind: true, showCancel: false, showConfirm: false, showPlay: false, showResult: true });
}

function connectWs() {
  const socket = new SockJS("/ws");
  stompClient = Stomp.over(socket);
  stompClient.debug = null;

  setIdle("Đang kết nối...");

  stompClient.connect({}, () => {
    log("Connected.");
    setIdle("Sẵn sàng");

    stompClient.subscribe("/user/queue/system", (m) => {
      const evt = JSON.parse(m.body);
      log("[SYS] " + m.body);

      if (evt.type === "QUEUING") setQueuing(evt.message);
      else if (evt.type === "IDLE") setIdle(evt.message);
      else if (evt.type === "PLAYING") setPlaying(evt.message);
      else if (evt.type === "CONFIRM") $("status").textContent = evt.message;
      else if (evt.type === "RESULT") $("status").textContent = evt.message;
    });

    stompClient.subscribe("/user/queue/match", (m) => {
      const evt = JSON.parse(m.body);
      log("[MATCH] " + m.body);

      currentMatchId = evt.matchId;
      $("matchInfo").textContent = `MatchId: ${evt.matchId} | ${evt.player1} vs ${evt.player2}`;
      setFound("Tìm thấy đối thủ!");
    });

    stompClient.subscribe("/user/queue/result", (m) => {
      const r = JSON.parse(m.body);
      log("[RESULT] " + m.body);

      let title = "HÒA";
      if (r.myDelta > 0) title = "CHIẾN THẮNG";
      else if (r.myDelta < 0) title = "THẤT BẠI";

      $("resultTitle").textContent = title;
      $("resultScore").textContent = `${r.score1} - ${r.score2}`;
      $("resultDelta").textContent = `Điểm nhận: ${r.myDelta > 0 ? "+" : ""}${r.myDelta}`;

      setResult("Trận đấu kết thúc!");
    });

  }, (err) => {
    console.error(err);
    setIdle("Không kết nối được WebSocket");
  });
}

function findMatch() {
  if (!stompClient) return;
  setQueuing("Đang tìm đối thủ...");
  stompClient.send("/app/battle/queue/join", {}, JSON.stringify({}));
  log("Join queue...");
}

function cancelFind() {
  if (!stompClient) return;
  stompClient.send("/app/battle/queue/cancel", {}, JSON.stringify({}));
  setIdle("Đã hủy tìm trận");
  log("Cancel queue...");
}

function acceptMatch() {
  if (!stompClient || !currentMatchId) return;

  stompClient.subscribe(`/topic/match/${currentMatchId}`, (m) => {
    const evt = JSON.parse(m.body);

    if (evt.questionText) {
      setPlaying("Đang thi đấu");
      renderQuestion(evt);
    }

    if (evt.p1 && evt.p2 && evt.score1 !== undefined && evt.score2 !== undefined) {
      $("score").textContent = `Score: ${evt.p1} ${evt.score1} - ${evt.score2} ${evt.p2}`;
      if (evt.remainSec !== undefined) $("timer").textContent = `Còn ${evt.remainSec}s`;
    }
  });

  stompClient.send("/app/battle/match/accept", {}, JSON.stringify({ matchId: currentMatchId }));
  $("status").textContent = "Bạn đã xác nhận. Đang chờ đối thủ...";
  log("Accept match: " + currentMatchId);
}

function declineMatch() {
  if (!stompClient || !currentMatchId) return;
  stompClient.send("/app/battle/match/decline", {}, JSON.stringify({ matchId: currentMatchId }));
  log("Decline match: " + currentMatchId);
  setIdle("Bạn đã từ chối trận.");
}

function renderQuestion(q) {
  $("question").textContent = `Câu ${q.index + 1}: ${q.questionText}`;
  $("timer").textContent = `Còn ${(q.timeLimitSec || 10)}s`;

  $("options").innerHTML = "";
  const letters = ["A", "B", "C", "D"];

  (q.options || []).forEach((opt, i) => {
    const btn = document.createElement("button");
    btn.className = "optBtn";
    btn.type = "button";
    btn.textContent = opt;
    btn.onclick = () => submitAnswer(letters[i], q.index);
    $("options").appendChild(btn);
  });
}

function submitAnswer(choice, index) {
  if (!stompClient || !currentMatchId) return;
  stompClient.send("/app/battle/answer", {}, JSON.stringify({
    matchId: currentMatchId,
    choice,
    index
  }));
  log(`Answer: ${choice} (index=${index})`);
}

function playAgain() {
  setIdle("Sẵn sàng");
  $("question").textContent = "Chưa có câu hỏi";
  $("options").innerHTML = "";
  $("timer").textContent = "0s";
  $("score").textContent = "Score: -";
  log("Play again.");
}

document.addEventListener("DOMContentLoaded", () => {
  $("btnFind").addEventListener("click", findMatch);
  $("btnCancel").addEventListener("click", cancelFind);
  $("btnAccept").addEventListener("click", acceptMatch);
  $("btnDecline").addEventListener("click", declineMatch);
  $("btnPlayAgain").addEventListener("click", playAgain);

  const me = $("me")?.textContent?.trim();
  if (!me) {
    $("status").textContent = "Bạn cần đăng nhập để chơi battle";
    window.location.href = "/login";
    return;
  }

  connectWs();
});
