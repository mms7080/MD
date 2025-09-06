// common/theme.js

function applyTheme(theme){
    document.documentElement.setAttribute("data-theme", theme);
    const btn = document.getElementById("btnTheme"); 
    if (btn) btn.setAttribute("aria-pressed", theme === "dark");
  }
  
  function toggleTheme(){
    const current = localStorage.getItem("theme") || "light";
    const newTheme = current === "dark" ? "light" : "dark";
    localStorage.setItem("theme", newTheme);
    applyTheme(newTheme);
  }
  
  function initTheme() {
    const theme = localStorage.getItem("theme") || "light";
    applyTheme(theme);
  
    const themeBtn = document.getElementById("btnTheme");
    if (themeBtn) {
      themeBtn.addEventListener("click", toggleTheme);
    }
  }
  
  // 실행
  document.addEventListener("DOMContentLoaded", initTheme);
  