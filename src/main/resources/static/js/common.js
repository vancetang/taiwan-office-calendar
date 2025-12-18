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

  // 監聽捲動事件，顯示或隱藏按鈕
  window.addEventListener("scroll", () => {
    if (window.scrollY > 100) {
      backToTopBtn.classList.add("show");
    } else {
      backToTopBtn.classList.remove("show");
    }
  });

  // 點擊平滑捲動回頂端
  backToTopBtn.addEventListener("click", () => {
    window.scrollTo({
      top: 0,
      behavior: "smooth"
    });
  });
}
