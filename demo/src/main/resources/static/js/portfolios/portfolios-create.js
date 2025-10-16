// ===========================
// 팀원 추가
// ===========================
function addTeam() {
  const list = document.getElementById("team-list");
  const index = list.children.length; // 현재 팀원 개수 기준 index
  const div = document.createElement("div");
  div.className = "team-item";
  div.innerHTML = `
    <input type="text" name="team[${index}].memberName" placeholder="팀원 이름" required>
    <select name="team[${index}].memberRole">
      <option value="팀장">팀장</option>
      <option value="팀원">팀원</option>
    </select>
    <input type="text" name="team[${index}].parts" placeholder="담당 기능/페이지">
  `;
  list.appendChild(div);
}

// ===========================
// 취소 버튼 → 리스트 페이지로 이동
// ===========================
function goToList() {
  window.location.href = "/portfolios";  
}

// ===========================
// ✅ 태그 관리 (추가 / 삭제 / hidden input 자동 반영)
// ===========================
document.addEventListener("DOMContentLoaded", () => {
  const tagInput = document.getElementById("tag-input");
  const tagList = document.getElementById("tag-list");
  const tagsHiddenBox = document.getElementById("tags-hidden-box");

  if (!tagInput || !tagList || !tagsHiddenBox) return;

  // ✅ 초기화: 기존 태그 불러오기
  let tags = Array.from(tagList.querySelectorAll(".tag-item")).map(div => div.textContent.trim());

  // ✅ 태그 추가
  window.addTag = function () {
    const value = tagInput.value.trim();
    if (!value || tags.includes(value)) {
      tagInput.value = "";
      return;
    }

    tags.push(value);
    renderTags();
    tagInput.value = "";
  };

  // ✅ 태그 삭제
  window.removeTag = function (tagToRemove) {
    tags = tags.filter(tag => tag !== tagToRemove);
    renderTags();
  };

  // ✅ 렌더링 (UI + hidden input 갱신)
  function renderTags() {
    tagList.innerHTML = "";
    tagsHiddenBox.innerHTML = "";

    tags.forEach(tag => {
      // 표시용 태그
      const div = document.createElement("div");
      div.className = "tag-item";
      div.textContent = tag;
      div.onclick = () => removeTag(tag);
      tagList.appendChild(div);

      // 서버 전송용 hidden input
      const hidden = document.createElement("input");
      hidden.type = "hidden";
      hidden.name = "tags";
      hidden.value = tag;
      tagsHiddenBox.appendChild(hidden);
    });
  }

  // ✅ 페이지 로드시 기존 태그 hidden input 세팅
  renderTags();
});

// ===========================
// 프로젝트 이미지 (여러 장) + 미리보기
// ===========================
document.getElementById("image-input")?.addEventListener("change", function(event) {
  const files = event.target.files;
  const preview = document.getElementById("image-preview");
  preview.innerHTML = ""; // 초기화

  Array.from(files).forEach(file => {
    if (file.type.startsWith("image/")) {
      const reader = new FileReader();
      reader.onload = e => {
        const imgWrapper = document.createElement("div");
        imgWrapper.className = "thumb-wrapper";

        const img = document.createElement("img");
        img.src = e.target.result;
        img.className = "thumb";

        // 삭제 버튼
        const removeBtn = document.createElement("span");
        removeBtn.className = "remove-thumb";
        removeBtn.innerHTML = "&times;";
        removeBtn.onclick = () => imgWrapper.remove();

        imgWrapper.appendChild(img);
        imgWrapper.appendChild(removeBtn);
        preview.appendChild(imgWrapper);
      };
      reader.readAsDataURL(file);
    }
  });
});

// ===========================
// 아이콘 업로드 (1장만) + 미리보기
// ===========================
document.getElementById("icon-input")?.addEventListener("change", function(event) {
  const file = event.target.files[0]; 
  const preview = document.getElementById("icon-preview");
  preview.innerHTML = ""; 

  if (file && file.type.startsWith("image/")) {
    const reader = new FileReader();
    reader.onload = e => {
      const imgWrapper = document.createElement("div");
      imgWrapper.className = "icon-wrapper";

      const img = document.createElement("img");
      img.src = e.target.result;
      img.className = "icon-thumb";

      const removeBtn = document.createElement("span");
      removeBtn.className = "remove-thumb";
      removeBtn.innerHTML = "&times;";
      removeBtn.onclick = () => {
        preview.innerHTML = "";
        document.getElementById("icon-input").value = ""; 
      };

      imgWrapper.appendChild(img);
      imgWrapper.appendChild(removeBtn);
      preview.appendChild(imgWrapper);
    };
    reader.readAsDataURL(file);
  }
});

// ===========================
// Cover 이미지 (1장만) + 미리보기
// ===========================
document.getElementById("cover-input")?.addEventListener("change", function(event) {
  const file = event.target.files[0];
  const preview = document.getElementById("cover-preview");
  preview.innerHTML = "";

  if (file && file.type.startsWith("image/")) {
    const reader = new FileReader();
    reader.onload = e => {
      const img = document.createElement("img");
      img.src = e.target.result;
      img.className = "cover-thumb";
      preview.appendChild(img);
    };
    reader.readAsDataURL(file);
  }
});

// ===========================
// 수정용 팀원 관리
// ===========================
function addTeam() {
  const list = document.getElementById("team-list");
  const index = list.children.length;
  const div = document.createElement("div");
  div.className = "team-item";

  div.innerHTML = `
    <input type="hidden" name="team[${index}].id" value="">
    <input type="text" name="team[${index}].memberName" placeholder="팀원 이름" required>
    <select name="team[${index}].memberRole">
      <option value="팀장">팀장</option>
      <option value="팀원">팀원</option>
    </select>
    <input type="text" name="team[${index}].parts" placeholder="담당 기능/페이지">
    <button type="button" class="btn-mini danger" onclick="removeTeam(this)">삭제</button>
  `;
  list.appendChild(div);
}

function removeTeam(btn) {
  btn.parentElement.remove();
}
