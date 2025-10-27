document.addEventListener("DOMContentLoaded", () => {
    const btnExit = document.getElementById("btnExit");
    if (!btnExit) return;

    // ✅ 클릭하면 무조건 목록(/folios)으로 이동
    btnExit.addEventListener("click", (e) => {
        e.preventDefault();
        location.href = "/folios";
    });
    window.addEventListener("keydown", (e) => {
        if (e.key === "Escape") location.href = "/folios";
    });
});
