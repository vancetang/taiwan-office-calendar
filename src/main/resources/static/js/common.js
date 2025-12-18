/**
 * 全域共用 JavaScript (common.js)
 * 包含「回到頂端」按鈕邏輯與其他未來可能的共用功能
 */

document.addEventListener("DOMContentLoaded", () => {
  initBackToTop();
});

/**
 * 初始化「回到頂端」按鈕
 */
function initBackToTop() {
  const backToTopBtn = document.getElementById("backToTop");
  if (!backToTopBtn) return;

  /**
   * 節流函式 (Throttle)
   * 限制函式在指定時間內只能執行一次，用於最佳化高頻率觸發的事件 (如 scroll)
   */
  function throttle(func, limit) {
    let inThrottle;
    return function() {
      const args = arguments;
      const context = this;
      if (!inThrottle) {
        func.apply(context, args);
        inThrottle = true;
        setTimeout(() => (inThrottle = false), limit);
      }
    };
  }

  // 監聽捲動事件，顯示或隱藏按鈕 (加入 100ms 節流處理)
  window.addEventListener(
    "scroll",
    throttle(() => {
      if (window.scrollY > 100) {
        backToTopBtn.classList.add("show");
      } else {
        backToTopBtn.classList.remove("show");
      }
    }, 100)
  );

  // 點擊平滑捲動回頂端
  backToTopBtn.addEventListener("click", () => {
    window.scrollTo({
      top: 0,
      behavior: "smooth"
    });
  });
}
