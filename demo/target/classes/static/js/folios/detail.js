(function () {
    const bar = document.querySelector(".fd-actions");
    if (!bar) return;

    const folioId = bar.getAttribute("data-folio-id") || "unknown";
    const LIKE_KEY = `folio_like_${folioId}`;
    const likeBtn = bar.querySelector('[data-action="like"]');
    const likeCnt = likeBtn?.querySelector(".fd-like-count");

    // --- 초기 좋아요 상태 (로컬 전용 임시) ---
    const saved = localStorage.getItem(LIKE_KEY);
    if (saved === "1") likeBtn?.classList.add("is-liked");
    updateLikeCount();

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
                // ✅ 새 탭 없이, 현재 탭에서 원본 크기로 인쇄
                printSlidesInPlace();
                break;

            case "like": {
                const isLiked = btn.classList.toggle("is-liked");
                localStorage.setItem(LIKE_KEY, isLiked ? "1" : "0");
                updateLikeCount();
                break;
            }

            case "top":
                window.scrollTo({ top: 0, behavior: "smooth" });
                break;
        }
    });

    function updateLikeCount() {
        if (!likeCnt) return;
        const base = 0;
        const isLiked = likeBtn?.classList.contains("is-liked");
        likeCnt.textContent = String(base + (isLiked ? 1 : 0));
    }

    // ============== 인쇄(현재 탭) ==============
    function pxToMm(px) {
        // 96dpi 기준: 1px = 25.4/96 mm
        return (px * 25.4) / 96;
    }

    function loadImage(url) {
        return new Promise((res, rej) => {
            const img = new Image();
            img.crossOrigin = "anonymous";
            img.onload = () => res(img);
            img.onerror = rej;
            img.src = url;
        });
    }

    async function printSlidesInPlace() {
        const urls = [...document.querySelectorAll(".fd-slide img")].map(
            (i) => i.src
        );
        if (!urls.length) {
            alert("인쇄할 슬라이드가 없습니다.");
            return;
        }

        // 1) 첫 장 크기(px → mm)
        const probe = await new Promise((res, rej) => {
            const im = new Image();
            im.onload = () => res(im);
            im.onerror = rej;
            im.src = urls[0];
        });
        const pxToMm = (px) => (px * 25.4) / 96;
        const wmm = pxToMm(probe.naturalWidth);
        const hmm = pxToMm(probe.naturalHeight);

        // 2) 홀더/스타일 생성 (보이지 않지만 로드되도록)
        const holderId = "__folio_print_holder__";
        const styleId = "__folio_print_style__";
        document.getElementById(holderId)?.remove();
        document.getElementById(styleId)?.remove();

        const holder = document.createElement("div");
        holder.id = holderId;
        // 화면에서는 차지하지 않게 숨기지만, display:none은 아님!
        Object.assign(holder.style, {
            position: "fixed",
            left: "0",
            top: "0",
            width: "1px",
            height: "1px",
            overflow: "hidden",
            opacity: "0",
            pointerEvents: "none",
            zIndex: "-1",
        });
        holder.innerHTML = urls
            .map((u) => `<div class="page"><img src="${u}" alt=""></div>`)
            .join("");

        const style = document.createElement("style");
        style.id = styleId;
        style.textContent = `
    @page { size: ${wmm}mm ${hmm}mm; margin: 0; }
    @media print {
      /* 일반 화면 숨기기 */
      body > *:not(#${holderId}) { display: none !important; }
      html, body { margin:0; padding:0; background:#fff !important; }
      #${holderId} { 
        position: static !important;
        width: auto !important; height: auto !important;
        opacity: 1 !important; pointer-events: auto !important; 
      }
      #${holderId} .page { page-break-after: always; break-after: page; }
      #${holderId} img {
        display:block;
        width:${wmm}mm; height:${hmm}mm;
        object-fit: contain;
      }
    }
  `;

        document.head.appendChild(style);
        document.body.appendChild(holder);

        // 3) 모든 이미지 로딩/디코딩 대기
        const imgs = [...holder.querySelectorAll("img")];
        await Promise.all(
            imgs.map(
                (img) =>
                    new Promise((resolve) => {
                        if (img.complete && img.naturalWidth) {
                            // decode()가 지원되면 디코딩까지 대기
                            if (img.decode)
                                img.decode().then(resolve).catch(resolve);
                            else resolve();
                        } else {
                            img.addEventListener(
                                "load",
                                () => {
                                    if (img.decode)
                                        img.decode()
                                            .then(resolve)
                                            .catch(resolve);
                                    else resolve();
                                },
                                { once: true }
                            );
                            img.addEventListener("error", resolve, {
                                once: true,
                            });
                        }
                    })
            )
        );

        // 4) 렌더링 한 틱 보장 후 print
        await new Promise((r) =>
            requestAnimationFrame(() => requestAnimationFrame(r))
        );

        const cleanup = () => {
            document.getElementById(holderId)?.remove();
            document.getElementById(styleId)?.remove();
            window.removeEventListener("afterprint", cleanup);
        };
        window.addEventListener("afterprint", cleanup, { once: true });

        window.print();
    }

    // ============== 토스트 ==============
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
