// ===========================
// 팀원 관리 (추가/삭제)
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
  
  // ===========================
  // 태그 관리
  // ===========================
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
    const hiddenBox = document.getElementById("tags-hidden-box");
    list.innerHTML = "";
    hiddenBox.innerHTML = "";
  
    tags.forEach(tag => {
      const span = document.createElement("span");
      span.className = "tag-item";
      span.innerHTML = `${tag} <span class="remove-tag" onclick="removeTag('${tag}')">&times;</span>`;
      list.appendChild(span);
  
      const hidden = document.createElement("input");
      hidden.type = "hidden";
      hidden.name = "tags";
      hidden.value = tag;
      hiddenBox.appendChild(hidden);
    });
  }
  
  // ===========================
  // Cover 이미지 (1장만 교체)
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
  // 아이콘 (1장만 교체)
  // ===========================
  document.getElementById("icon-input")?.addEventListener("change", function(event) {
    const file = event.target.files[0];
    const preview = document.getElementById("icon-preview");
    preview.innerHTML = "";
  
    if (file && file.type.startsWith("image/")) {
      const reader = new FileReader();
      reader.onload = e => {
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
  
        preview.appendChild(img);
        preview.appendChild(removeBtn);
      };
      reader.readAsDataURL(file);
    }
  });
  
  // ===========================
  // 프로젝트 이미지 (여러 장, 교체 가능)
  // ===========================
  document.getElementById("image-input")?.addEventListener("change", function(event) {
    const files = event.target.files;
    const preview = document.getElementById("image-preview");
    preview.innerHTML = "";
  
    Array.from(files).forEach(file => {
      if (file.type.startsWith("image/")) {
        const reader = new FileReader();
        reader.onload = e => {
          const wrapper = document.createElement("div");
          wrapper.className = "thumb-wrapper";
  
          const img = document.createElement("img");
          img.src = e.target.result;
          img.className = "thumb";
  
          const removeBtn = document.createElement("span");
          removeBtn.className = "remove-thumb";
          removeBtn.innerHTML = "&times;";
          removeBtn.onclick = () => wrapper.remove();
  
          wrapper.appendChild(img);
          wrapper.appendChild(removeBtn);
          preview.appendChild(wrapper);
        };
        reader.readAsDataURL(file);
      }
    });
  });
  
  // ===========================
  // 취소 버튼
  // ===========================
  function goToList() {
    window.location.href = "/portfolios";
  }
  