(function () {
    const bar = document.querySelector(".fd-actions");
    if (!bar) return;

    bar.addEventListener("click", async (e) => {
        const btn = e.target.closest(".fd-action");
        if (!btn) return;

        const action = btn.getAttribute("data-action");
        switch (action) {
            case "share":
                try {
                    const url = location.href;
                    if (navigator.clipboard?.writeText) {
                        await navigator.clipboard.writeText(url);
                        toast("링크가 복사됐어요!");
                    } else {
                        prompt("아래 URL을 복사하세요", url);
                    }
                } catch {
                    alert("복사에 실패했어요. 수동으로 복사해 주세요.");
                }
                break;

            case "save":
                // 현재 탭에서 원본 비율로 인쇄
                printSlidesInPlace();
                break;

            case "top":
                window.scrollTo({ top: 0, behavior: "smooth" });
                break;
        }
    });

    // ============== 인쇄(현재 탭) ==============
    async function printSlidesInPlace() {
        const urls = [...document.querySelectorAll(".fd-slide img")].map(
            (i) => i.src
        );
        if (!urls.length) {
            alert("인쇄할 슬라이드가 없습니다.");
            return;
        }

        // 슬라이드 원본 비율로 용지 크기 결정
        const probe = await new Promise((res, rej) => {
            const im = new Image();
            im.onload = () => res(im);
            im.onerror = rej;
            im.src = urls[0];
        });
        const { naturalWidth: w, naturalHeight: h } = probe;
        const ratio = w / h;
        const base = 210; // 기준 한 변(mm)
        const W = ratio >= 1 ? base * ratio : base;
        const H = ratio >= 1 ? base : base / ratio;

        const frame = document.createElement("iframe");
        Object.assign(frame.style, {
            position: "fixed",
            right: "0",
            bottom: "0",
            width: "0",
            height: "0",
            border: "0",
            visibility: "hidden",
        });
        document.body.appendChild(frame);

        const doc = frame.contentDocument;
        doc.open();
        doc.write(`
<!doctype html><html><head><meta charset="utf-8">
<style>
  @page { size: ${W.toFixed(1)}mm ${H.toFixed(1)}mm; margin: 0; }
  html, body { margin:0; padding:0; background:#fff; }
  .page {
    page-break-after: always;
    display:flex; justify-content:center; align-items:center;
    width:${W.toFixed(1)}mm; height:${H.toFixed(1)}mm;
  }
  .page img { width:100%; height:100%; object-fit:contain; }
</style>
</head><body>
  ${urls.map((u) => `<div class="page"><img src="${u}" alt=""></div>`).join("")}
  <script>
    (function(){
      async function readyToPrint(){
        const imgs = Array.from(document.images);
        await Promise.all(imgs.map(img => img.decode ? img.decode().catch(()=>{}) : Promise.resolve()));
        window.focus(); window.print();
      }
      if (document.readyState === 'complete') readyToPrint();
      else window.addEventListener('load', readyToPrint);
      window.addEventListener('afterprint', ()=> parent.postMessage('___folio_iframe_done___','*'), {once:true});
    })();
  <\/script>
</body></html>`);
        doc.close();

        const cleanup = (e) => {
            if (e.data === "___folio_iframe_done___") {
                window.removeEventListener("message", cleanup);
                setTimeout(() => frame.remove(), 100);
            }
        };
        window.addEventListener("message", cleanup);
    }

    // 토스트 알림
    let toastTimer;
    function toast(msg) {
        clearTimeout(toastTimer);
        let el = document.querySelector(".fd-toast");
        if (!el) {
            el = document.createElement("div");
            el.className = "fd-toast";
            Object.assign(el.style, {
                position: "fixed",
                bottom: "24px",
                left: "50%",
                transform: "translateX(-50%)",
                padding: "10px 14px",
                borderRadius: "10px",
                background: "rgba(0,0,0,.75)",
                color: "#fff",
                fontSize: "14px",
                zIndex: "1001",
                transition: "opacity .2s",
            });
            document.body.appendChild(el);
        }
        el.textContent = msg;
        el.style.opacity = "1";
        toastTimer = setTimeout(() => (el.style.opacity = "0"), 1400);
    }
})();
