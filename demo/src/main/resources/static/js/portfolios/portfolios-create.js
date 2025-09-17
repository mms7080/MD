let teamIndex = 1;
function addTeam() {
  const list = document.getElementById("team-list");
  const div = document.createElement("div");
  div.className = "team-item";
  div.innerHTML = `
    <input type="text" name="team[${teamIndex}].member" placeholder="팀원 이름">
    <select name="team[${teamIndex}].role">
      <option value="팀장">팀장</option>
      <option value="팀원">팀원</option>
    </select>
    <input type="text" name="team[${teamIndex}].task" placeholder="담당 기능/페이지">
  `;
  list.appendChild(div);
  teamIndex++;
}

// 취소 버튼 → 리스트 페이지로 이동
function goToList() {
  window.location.href = "/portfolios";  // list.html을 보여주는 경로
}


let tags = [];

function addTag() {
  const input = document.getElementById("tag-input");
  const value = input.value.trim();

  if (value && !tags.includes(value)) {
    tags.push(value);
    renderTags();
    input.value = "";
  }
}

function removeTag(tag) {
  tags = tags.filter(t => t !== tag);
  renderTags();
}

function renderTags() {
  const list = document.getElementById("tag-list");
  list.innerHTML = "";

  tags.forEach(tag => {
    const div = document.createElement("div");
    div.className = "tag-chip";
    div.innerHTML = `
      ${tag} <span class="remove-tag" onclick="removeTag('${tag}')">&times;</span>
    `;
    list.appendChild(div);
  });

  // hidden input에 콤마로 join해서 값 넣기
  document.getElementById("tags-hidden").value = tags.join(",");
}

function validateFile(file) {
  const validTypes = ["image/jpeg", "image/png", "image/webp"];
  const maxSize = 10 * 1024 * 1024; // 10MB

  if (!validTypes.includes(file.type)) {
    alert("JPEG, PNG, WebP 형식만 업로드 가능합니다.");
    return false;
  }
  if (file.size > maxSize) {
    alert("파일 크기는 최대 10MB까지 가능합니다.");
    return false;
  }
  return true;
}

// ===========================
// 이미지 추가 + 미리보기 + 삭제
// ===========================
document.getElementById("image-input")?.addEventListener("change", function(event) {
  const files = event.target.files;
  const preview = document.getElementById("image-preview");

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
// 아이콘 업로드 (1장만 미리보기)
// ===========================
document.getElementById("icon-input")?.addEventListener("change", function(event) {
  const file = event.target.files[0]; // 첫 번째 파일만
  const preview = document.getElementById("icon-preview");
  preview.innerHTML = ""; // 기존 미리보기 초기화

  if (file && file.type.startsWith("image/")) {
    const reader = new FileReader();
    reader.onload = e => {
      const imgWrapper = document.createElement("div");
      imgWrapper.className = "icon-wrapper";

      const img = document.createElement("img");
      img.src = e.target.result;
      img.className = "icon-thumb";

      // 삭제 버튼
      const removeBtn = document.createElement("span");
      removeBtn.className = "remove-thumb";
      removeBtn.innerHTML = "&times;";
      removeBtn.onclick = () => {
        preview.innerHTML = "";
        document.getElementById("icon-input").value = ""; // 파일 선택 초기화
      };

      imgWrapper.appendChild(img);
      imgWrapper.appendChild(removeBtn);
      preview.appendChild(imgWrapper);
    };
    reader.readAsDataURL(file);
  }
});


// ===========================
// 취소 버튼 → 리스트로 이동
// ===========================
function goToList() {
  window.location.href = "/portfolios";
}




