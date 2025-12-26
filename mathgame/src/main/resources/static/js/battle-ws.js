let stompClient = null;
let currentMatchId = null;
let countdownTimer = null;
let countdownRemaining = 0;
let currentUser = "";
const answeredIndex = new Set();

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
  const footerActions = $("battleFooterActions");
  if (footerActions) footerActions.style.display = showResult ? "none" : "flex";
}

function setIdle(msg = "Sẵn sàng") {
  setState({ status: msg, showFind: true, showCancel: false, showConfirm: false, showPlay: false, showResult: false });
  currentMatchId = null;
  clearCountdown();
  answeredIndex.clear();
}

function setQueuing(msg = "Đang tìm đối thủ...") {
  setState({ status: msg, showFind: false, showCancel: true, showConfirm: false, showPlay: false, showResult: false });
}

function setFound(msg = "Tìm thấy đối thủ!") {
  setState({ status: msg, showFind: false, showCancel: false, showConfirm: true, showPlay: false, showResult: false });
}

function setPlaying(msg = "Đang thi đấu") {
  setState({ status: msg, showFind: false, showCancel: false, showConfirm: false, showPlay: true, showResult: false });
  resetForfeitButton();
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

      if (evt.type === "QUEUING") setQueuing();
      else if (evt.type === "IDLE") setIdle();
      else if (evt.type === "PLAYING") setPlaying();
      else if (evt.type === "CONFIRM") $("status").textContent = "Bạn đã xác nhận. Đang chờ đối thủ...";
      else if (evt.type === "RESULT") $("status").textContent = "Trận đấu kết thúc!";
    });

    stompClient.subscribe("/user/queue/match", (m) => {
      const evt = JSON.parse(m.body);
      log("[MATCH] " + m.body);

      currentMatchId = evt.matchId;
      $("matchInfo").textContent = `${evt.player1} vs ${evt.player2}`;
      setFound("Tìm thấy đối thủ!");
    });

    stompClient.subscribe("/user/queue/result", (m) => {
      const r = JSON.parse(m.body);
      log("[RESULT] " + m.body);

      const winner = r.score1 === r.score2
        ? null
        : (r.score1 > r.score2 ? r.player1 : r.player2);

      let title = "HÒA";
      if (winner) {
        title = winner === currentUser ? "CHIẾN THẮNG" : "THẤT BẠI";
      }

      $("resultTitle").textContent = title;
      $("resultWinner").textContent = winner ?? "Không có (Hòa)";
      $("resultScore").textContent = `${r.score1} : ${r.score2}`;
      $("resultDelta").textContent = `Điểm nhận: ${r.myDelta > 0 ? "+" : ""}${r.myDelta}`;
      $("resultPlayer1").textContent = r.player1 ?? "-";
      $("resultPlayer2").textContent = r.player2 ?? "-";

      const titleEl = $("resultTitle");
      titleEl.classList.remove("result-win", "result-lose", "result-draw");
      if (!winner) titleEl.classList.add("result-draw");
      else if (winner === currentUser) titleEl.classList.add("result-win");
      else titleEl.classList.add("result-lose");

      clearCountdown();
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

function forfeitMatch() {
  if (!stompClient || !currentMatchId) return;
  const btn = $("btnForfeit");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Đang hủy...";
  }
  $("status").textContent = "Bạn đã hủy trận. Đang xử lý kết quả...";
  stompClient.send("/app/battle/match/forfeit", {}, JSON.stringify({ matchId: currentMatchId }));
  log("Forfeit match: " + currentMatchId);
}

function resetForfeitButton() {
  const btn = $("btnForfeit");
  if (!btn) return;
  btn.disabled = false;
  btn.textContent = "Hủy trận đấu";
}

function acceptMatch() {
  if (!stompClient || !currentMatchId) return;

  stompClient.subscribe(`/topic/match/${currentMatchId}`, (m) => {
    const evt = JSON.parse(m.body);

    if (evt.questionText) {
      setPlaying("Đang thi đấu");
      renderQuestion(evt);
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
  $("options").innerHTML = "";
  $("optionLabels").innerHTML = "";

  answeredIndex.delete(q.index);
  startCountdown(q.timeLimitSec || 10, q.index);

  const letters = ["A", "B", "C", "D"];
  (q.options || []).forEach((opt, i) => {
    const label = document.createElement("span");
    label.className = "battle-option-label";
    label.textContent = `${letters[i]}) ${opt}`;
    $("optionLabels").appendChild(label);

    const btn = document.createElement("button");
    btn.className = "battle-option";
    btn.type = "button";
    btn.textContent = `${letters[i]}${opt}`;
    btn.onclick = () => submitAnswer(letters[i], q.index);
    $("options").appendChild(btn);
  });
}

function submitAnswer(choice, index) {
  if (!stompClient || !currentMatchId) return;
  if (answeredIndex.has(index)) return;
  answeredIndex.add(index);

  clearCountdown();
  disableOptions();

  stompClient.send("/app/battle/answer", {}, JSON.stringify({
    matchId: currentMatchId,
    choice,
    index
  }));
  log(`Answer: ${choice || "(timeout)"} (index=${index})`);
}

function disableOptions() {
  const buttons = document.querySelectorAll("#options button");
  buttons.forEach((btn) => {
    btn.disabled = true;
    btn.style.opacity = "0.6";
    btn.style.cursor = "not-allowed";
  });
}

function startCountdown(seconds, index) {
  clearCountdown();
  countdownRemaining = seconds;
  updateTimer();

  countdownTimer = setInterval(() => {
    countdownRemaining -= 1;
    updateTimer();

    if (countdownRemaining <= 0) {
      clearCountdown();
      if (!answeredIndex.has(index)) {
        submitAnswer("", index);
      }
    }
  }, 1000);
}

function updateTimer() {
  $("timer").textContent = `Còn ${Math.max(0, countdownRemaining)}s`;
}

function clearCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
}

function playAgain() {
  setIdle("Sẵn sàng");
  $("question").textContent = "Chưa có câu hỏi";
  $("options").innerHTML = "";
  $("optionLabels").innerHTML = "";
  $("timer").textContent = "0s";
  log("Play again.");
}

document.addEventListener("DOMContentLoaded", () => {
  $("btnFind").addEventListener("click", findMatch);
  $("btnCancel").addEventListener("click", cancelFind);
  $("btnAccept").addEventListener("click", acceptMatch);
  $("btnDecline").addEventListener("click", declineMatch);
  $("btnForfeit").addEventListener("click", forfeitMatch);
  $("btnPlayAgain").addEventListener("click", playAgain);

  currentUser = $("me")?.textContent?.trim() || "";
  if (!currentUser) {
    $("status").textContent = "Bạn cần đăng nhập để chơi battle";
    window.location.href = "/login";
    return;
  }

  connectWs();
});
