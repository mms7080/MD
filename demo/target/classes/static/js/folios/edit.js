document.addEventListener('DOMContentLoaded', () => {
    // --- 미리보기 UI 요소 ---
    const preview = {
        name: document.getElementById('preview-name'),
        engName: document.getElementById('preview-eng-name'),
        job: document.getElementById('preview-job'),
        email: document.getElementById('preview-email'),
        skills: document.getElementById('preview-skills'),
        picContainer: document.getElementById('preview-pic-container')
    };

    // --- 폼 입력 요소 ---
    const form = {
        name: document.getElementById('folio-name'),
        engName: document.getElementById('folio-eng-name'),
        job: document.getElementById('folio-job'),
        email: document.getElementById('folio-email'),
        introduction: document.getElementById('folio-introduction'),
        photosInput: document.getElementById('photos'), 
        submitButton: document.querySelector('.btn-submit')
    };

    // --- 실시간 미리보기 이벤트 연결 ---
    function syncPreview() {
        preview.name.textContent = form.name.value || '이름';
        preview.engName.textContent = form.engName.value || 'English Name';
        preview.job.textContent = form.job.value || '희망 직무';
        preview.email.textContent = form.email.value || '이메일';
    }

    form.name.addEventListener('input', syncPreview);
    form.engName.addEventListener('input', syncPreview);
    form.job.addEventListener('input', syncPreview);
    form.email.addEventListener('input', syncPreview);
    
    syncPreview(); // 초기 로드 시 미리보기 동기화

    // --- 기술 스택 태그 관리 ---
    const skillsInputWrapper = document.getElementById('skills-input-wrapper');
    let tags = ["Java", "Spring Boot"]; // 태그 데이터를 저장할 배열
    
    if (skillsInputWrapper) {
        // ... (이전 단계의 기술 스택 코드는 여기에 그대로 유지됩니다) ...
        const tagsContainer = document.createElement('div');
        tagsContainer.className = 'tags-input-container';
        const skillsInput = document.createElement('input');
        skillsInput.type = 'text';
        skillsInput.id = 'skills';
        skillsInput.placeholder = '스킬 입력 후 Enter...';

        skillsInputWrapper.appendChild(tagsContainer);
        tagsContainer.appendChild(skillsInput);

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
            tagsContainer.querySelectorAll('.tag').forEach(t => t.remove());
            tags.forEach(tagText => {
                const tagElement = document.createElement('span');
                tagElement.className = 'tag';
                tagElement.textContent = tagText;
                const removeBtn = document.createElement('button');
                removeBtn.textContent = '×';
                removeBtn.type = 'button';
                removeBtn.onclick = () => {
                    tags = tags.filter(t => t !== tagText);
                    renderFormSkills();
                    renderPreviewSkills();
                };
                tagElement.appendChild(removeBtn);
                tagsContainer.insertBefore(tagElement, skillsInput);
            });
        };

        skillsInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                const newTag = skillsInput.value.trim();
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
    
    // --- 이미지 업로드 미리보기 (기존 코드 유지) ---
    if (form.photosInput) {
        // ... (이전 단계의 이미지 업로드 코드는 여기에 그대로 유지됩니다) ...
        form.photosInput.addEventListener('change', (event) => {
            const files = Array.from(event.target.files);
            if (files.length > 0) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    preview.picContainer.style.backgroundImage = `url('${e.target.result}')`;
                };
                reader.readAsDataURL(files[0]);
            }
        });
    }

    // --- ⬇️ [매우 중요] 저장(Submit) 기능 구현 ⬇️ ---
    form.submitButton.addEventListener('click', async (e) => {
        e.preventDefault(); // 기본 폼 제출 동작 방지

        // 1. DTO 형태로 보낼 데이터 객체 생성
        const folioData = {
            introduction: form.introduction.value,
            skills: tags,
            thumbnail: "https://picsum.photos/seed/newfolio/300" // 썸네일은 우선 임시값으로
            // 여기에 나중에 사진 업로드 후 대표 이미지 URL을 넣으면 됩니다.
        };

        // 2. CSRF 토큰 가져오기 (Thymeleaf를 통해 meta 태그에 저장된 값 사용)
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        try {
            // 3. fetch를 사용하여 서버에 POST 요청 보내기
            const response = await fetch('/api/folios', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken // CSRF 토큰을 헤더에 추가
                },
                body: JSON.stringify(folioData) // JavaScript 객체를 JSON 문자열로 변환
            });

            if (response.ok) {
                const result = await response.json();
                alert('Folio가 성공적으로 저장되었습니다!');
                // 4. 저장 성공 시, 새로 만들어진 상세 페이지로 이동
                window.location.href = `/folios/detail/${result.folioId}`;
            } else {
                // 서버에서 보낸 에러 메시지 표시
                const errorData = await response.json();
                alert(`저장 실패: ${errorData.message || '서버 오류가 발생했습니다.'}`);
            }
        } catch (error) {
            console.error('저장 중 오류 발생:', error);
            alert('저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        }
    });
});