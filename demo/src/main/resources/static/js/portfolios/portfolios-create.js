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
// 태그 관리
// ===========================
let tags = [];

// 태그 추가
function addTag() {
  const input = document.getElementById("tag-input");
  const value = input.value.trim();

  if (value && !tags.includes(value)) {
    tags.push(value);
    renderTags();
    input.value = "";
  }
}

// 태그 제거
function removeTag(tag) {
  tags = tags.filter(t => t !== tag);
  renderTags();
}

// 태그 렌더링 & hidden input 생성
function renderTags() {
  const list = document.getElementById("tag-list");
  const hiddenBox = document.getElementById("tags-hidden-box");
  list.innerHTML = "";
  hiddenBox.innerHTML = "";

  tags.forEach(tag => {
    // 보여주는 UI
    const div = document.createElement("div");
    div.className = "tag-chip";
    div.innerHTML = `
      ${tag} <span class="remove-tag" onclick="removeTag('${tag}')">&times;</span>
    `;
    list.appendChild(div);

    // DTO로 넘길 hidden input 생성
    const hidden = document.createElement("input");
    hidden.type = "hidden";
    hidden.name = "tags";
    hidden.value = tag;
    hiddenBox.appendChild(hidden);
  });
}

// ===========================
// 프로젝트 이미지 (여러 장) + 미리보기
// ===========================
document.getElementById("image-input")?.addEventListener("change", function(event) {
  const files = event.target.files;
  const preview = document.getElementById("image-preview");
  preview.innerHTML = ""; // 선택할 때마다 초기화

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
// Cover 이미지 업로드 (1장만) + 미리보기
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
