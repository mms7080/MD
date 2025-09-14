document.addEventListener('DOMContentLoaded', () => {
    // --- 실시간 미리보기를 위한 요소들 ---
    const preview = {
        name: document.getElementById('preview-name'),
        engName: document.getElementById('preview-eng-name'),
        job: document.getElementById('preview-job'),
        email: document.getElementById('preview-email'),
        skills: document.getElementById('preview-skills'),
        picContainer: document.getElementById('preview-pic-container')
    };

    const form = {
        name: document.getElementById('name'),
        engName: document.getElementById('eng-name'),
        job: document.getElementById('job'),
        email: document.getElementById('email'),
        contact: document.getElementById('contact'),
        skillsInput: document.getElementById('skills'), // 실제 input은 동적으로 생성
        photosInput: document.getElementById('photos')
    };

    // --- 실시간 미리보기 기능 ---
    form.name.addEventListener('input', () => { preview.name.textContent = form.name.value || '이름'; });
    form.engName.addEventListener('input', () => { preview.engName.textContent = form.engName.value || 'English Name'; });
    form.job.addEventListener('input', () => { preview.job.textContent = form.job.value || '희망 직무'; });
    form.email.addEventListener('input', () => { preview.email.textContent = form.email.value || '이메일'; });

    // --- 기술 스택 태그 입력 기능 (미리보기 연동 포함) ---
    const skillsInputWrapper = document.getElementById('skills-input-wrapper');
    if (skillsInputWrapper) {
        const tagsContainer = document.createElement('div');
        tagsContainer.className = 'tags-input-container';
        const skillsInput = document.createElement('input');
        skillsInput.type = 'text';
        skillsInput.id = 'skills';
        skillsInput.placeholder = '스킬 추가...';

        skillsInputWrapper.appendChild(tagsContainer);
        tagsContainer.appendChild(skillsInput);

        let tags = ["Java"]; // 초기값

        const renderPreviewSkills = () => {
            preview.skills.innerHTML = '';
            tags.forEach(tagText => {
                const tagEl = document.createElement('span');
                tagEl.className = 'tag';
                tagEl.textContent = tagText;
                preview.skills.appendChild(tagEl);
            });
        };

        const renderFormSkills = () => {
            // 기존 태그 모두 제거 (input 제외)
            tagsContainer.querySelectorAll('.tag').forEach(t => t.remove());
            tags.forEach(tagText => {
                const tagElement = document.createElement('span');
                tagElement.className = 'tag';
                tagElement.textContent = tagText;
                const removeBtn = document.createElement('button');
                removeBtn.textContent = '×';
                removeBtn.onclick = () => {
                    tags = tags.filter(t => t !== tagText);
                    renderFormSkills();
                    renderPreviewSkills();
                };
                tagElement.appendChild(removeBtn);
                tagsContainer.insertBefore(tagElement, skillsInput);
            });
        };

        skillsInput.addEventListener('keyup', (e) => {
            if (e.key === ',' || e.key === 'Enter') {
                const newTag = skillsInput.value.replace(/,/g, '').trim();
                if (newTag && !tags.includes(newTag)) {
                    tags.push(newTag);
                    renderFormSkills();
                    renderPreviewSkills();
                }
                skillsInput.value = '';
            }
        });
        renderFormSkills();
        renderPreviewSkills();
    }

    // --- 이미지 업로드 미리보기 기능 (미리보기 카드 연동 포함) ---
    if (form.photosInput) {
        form.photosInput.addEventListener('change', (event) => {
            const files = Array.from(event.target.files);
            if (files.length > 0) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    // 왼쪽 프로필 사진 업데이트
                    preview.picContainer.style.backgroundImage = `url('${e.target.result}')`;
                };
                reader.readAsDataURL(files[0]);
            }
        });
    }
});