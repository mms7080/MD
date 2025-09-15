/* /resources/static/js/folios/edit.js (최종 수정 완료) */
document.addEventListener('DOMContentLoaded', () => {
    const folioState = {
        skills: ["Java", "Spring Boot"],
        photos: [], // { fileId: '...', url: '...' }
    };

    const elements = {
        preview: {
            pic: document.getElementById('preview-profile-pic'),
            job: document.getElementById('preview-job'),
            skills: document.getElementById('preview-skills'),
        },
        form: {
            job: document.getElementById('folio-job'),
            introduction: document.getElementById('folio-introduction'),
            skillsWrapper: document.getElementById('skills-input-wrapper'),
            photosInput: document.getElementById('photos-input'),
            imagePreviewContainer: document.getElementById('image-preview-container'),
            formElement: document.getElementById('folio-edit-form'),
        },
    };

    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;

    const syncPreview = () => {
        elements.preview.job.textContent = elements.form.job.value || '희망 직무';
        const firstPhotoUrl = folioState.photos[0]?.url;
        elements.preview.pic.style.backgroundImage = firstPhotoUrl ? `url('${firstPhotoUrl}')` : 'none';
    };

    function setupSkillsInput() {
        const wrapper = elements.form.skillsWrapper;
        if (!wrapper) return;
        wrapper.innerHTML = `<div class="tags-input-container"><input type="text" id="skills-input" placeholder="스킬 입력 후 Enter..."></div>`;
        const container = wrapper.querySelector('.tags-input-container');
        const input = wrapper.querySelector('#skills-input');

        const renderSkills = () => {
            container.querySelectorAll('.tag').forEach(t => t.remove());
            folioState.skills.forEach(skill => {
                const tagEl = document.createElement('span');
                tagEl.className = 'tag';
                tagEl.textContent = skill;
                const removeBtn = document.createElement('button');
                removeBtn.textContent = '×';
                removeBtn.type = 'button';
                removeBtn.onclick = () => {
                    folioState.skills = folioState.skills.filter(s => s !== skill);
                    renderSkills();
                };
                tagEl.appendChild(removeBtn);
                container.insertBefore(tagEl, input);
            });
            elements.preview.skills.innerHTML = folioState.skills.map(s => `<span class="tag">${s}</span>`).join('');
        };

        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                const newSkill = input.value.trim();
                if (newSkill && !folioState.skills.includes(newSkill)) {
                    folioState.skills.push(newSkill);
                    renderSkills();
                }
                input.value = '';
            }
        });
        renderSkills();
    }

    function setupImageUpload() {
        const input = elements.form.photosInput;
        const container = elements.form.imagePreviewContainer;
        if (!input || !container) return;

        const renderImagePreviews = () => {
            container.innerHTML = '';
            folioState.photos.forEach((photo, index) => {
                const wrapper = document.createElement('div');
                wrapper.className = 'image-preview-wrapper';
                wrapper.innerHTML = `<img src="${photo.url}" alt="미리보기 ${index + 1}">`;
                const deleteBtn = document.createElement('button');
                deleteBtn.className = 'preview-delete-btn';
                deleteBtn.textContent = '×';
                deleteBtn.type = 'button';
                deleteBtn.onclick = () => {
                    folioState.photos.splice(index, 1);
                    renderImagePreviews();
                    syncPreview();
                };
                wrapper.appendChild(deleteBtn);
                container.appendChild(wrapper);
            });
        };

        input.addEventListener('change', async (event) => {
            const files = Array.from(event.target.files);
            if (files.length === 0) return;

            for (const file of files) {
                const formData = new FormData();
                formData.append('file', file);
                try {
                    const response = await fetch('/api/uploads/images', {
                        method: 'POST',
                        headers: { ...(csrfHeader && csrfToken && { [csrfHeader]: csrfToken }) },
                        body: formData,
                    });
                    if (!response.ok) throw new Error(`이미지 업로드 실패 (${response.status})`);
                    const result = await response.json();
                    folioState.photos.push(result);
                } catch (error) {
                    console.error('Upload error:', error);
                    alert(`이미지 업로드 중 오류가 발생했습니다: ${error.message}`);
                }
            }
            renderImagePreviews();
            syncPreview();
            input.value = '';
        });
    }

    elements.form.formElement?.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const selectedProjectIds = Array.from(document.querySelectorAll('input[name="projectIds"]:checked'))
                                    .map(input => input.value);

        const folioData = {
            introduction: elements.form.introduction.value,
            skills: folioState.skills,
            photos: folioState.photos.map(p => p.url),
            projectIds: selectedProjectIds
        };
        
        try {
            const response = await fetch('/api/folios', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(csrfHeader && csrfToken && { [csrfHeader]: csrfToken }),
                },
                body: JSON.stringify(folioData)
            });

            if (response.ok) {
                const result = await response.json();
                alert('Folio가 성공적으로 저장되었습니다!');
                window.location.href = `/folios/detail/${result.folioId}`;
            } else {
                // 서버가 403 같은 오류를 보내면 JSON이 없을 수 있음
                if (response.status === 403) {
                     alert(`저장 실패: 권한이 없습니다. 다시 로그인해주세요.`);
                } else {
                    const errorData = await response.json();
                    alert(`저장 실패: ${errorData.message || '서버 오류'}`);
                }
            }
        } catch (error) {
            console.error('Submit error:', error);
            alert('저장 중 네트워크 오류가 발생했습니다.');
        }
    });

    elements.form.job.addEventListener('input', syncPreview);
    setupSkillsInput();
    setupImageUpload();
    syncPreview();
});