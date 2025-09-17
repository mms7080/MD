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


// 이미지 추가
document.getElementById("image-input").addEventListener("change", function(event) {
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

        // 삭제 버튼 (×)
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

