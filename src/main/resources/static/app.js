const API_BASE = '/api';

const elements = {
    novelText: document.getElementById('novelText'),
    chapterHint: document.getElementById('chapterHint'),
    scriptType: document.getElementById('scriptType'),
    targetLanguage: document.getElementById('targetLanguage'),
    adaptationStyle: document.getElementById('adaptationStyle'),
    targetDuration: document.getElementById('targetDuration'),
    generateBtn: document.getElementById('generateBtn'),
    statusSection: document.getElementById('statusSection'),
    statusCard: document.getElementById('statusCard'),
    statusIcon: document.getElementById('statusIcon'),
    statusText: document.getElementById('statusText'),
    resultSection: document.getElementById('resultSection'),
    validationPanel: document.getElementById('validationPanel'),
    validationContent: document.getElementById('validationContent'),
    yamlOutput: document.getElementById('yamlOutput'),
    copyBtn: document.getElementById('copyBtn'),
    downloadBtn: document.getElementById('downloadBtn'),
    errorSection: document.getElementById('errorSection'),
    errorContent: document.getElementById('errorContent')
};

let currentYaml = '';
let currentTitle = 'script';

function countChapters(text) {
    const patterns = [
        /^(第[一二三四五六七八九十百千\d]+章)/gm,
        /^(Chapter\s+\d+)/gim,
        /^(CHAPTER\s+\d+)/gm,
        /^\d+\./gm
    ];

    let maxCount = 0;
    for (const pattern of patterns) {
        const matches = text.match(pattern);
        if (matches && matches.length > maxCount) {
            maxCount = matches.length;
        }
    }
    return maxCount;
}

function updateChapterHint() {
    const text = elements.novelText.value;
    if (!text.trim()) {
        elements.chapterHint.textContent = '';
        return;
    }

    const count = countChapters(text);
    if (count === 0) {
        elements.chapterHint.textContent = '未检测到章节标题，请确保文本包含章节标记（如：第一章、Chapter 1 等）';
        elements.chapterHint.style.color = '#fbbf24';
    } else if (count < 3) {
        elements.chapterHint.textContent = `检测到 ${count} 个章节，至少需要 3 个章节`;
        elements.chapterHint.style.color = '#f87171';
    } else {
        elements.chapterHint.textContent = `检测到 ${count} 个章节 ✓`;
        elements.chapterHint.style.color = '#4ade80';
    }
}

function showStatus(message, isLoading = false) {
    elements.statusSection.style.display = 'block';
    elements.statusIcon.innerHTML = isLoading ? '<span class="loading"></span>' : '⏳';
    elements.statusText.textContent = message;
    elements.statusCard.className = 'status-card' + (isLoading ? ' loading' : '');
}

function hideStatus() {
    elements.statusSection.style.display = 'none';
}

function showResult(response) {
    elements.resultSection.style.display = 'block';
    elements.errorSection.style.display = 'none';

    let validationHtml = '';
    if (response.validation) {
        const v = response.validation;
        validationHtml = '<div class="validation-item success">✓ 剧本生成成功</div>';

        if (v.errors && v.errors.length > 0) {
            v.errors.forEach(err => {
                validationHtml += `<div class="validation-item error">✗ ${err}</div>`;
            });
        }
        if (v.warnings && v.warnings.length > 0) {
            v.warnings.forEach(warn => {
                validationHtml += `<div class="validation-item warning">⚠ ${warn}</div>`;
            });
        }
    }
    elements.validationContent.innerHTML = validationHtml;

    currentYaml = response.yaml || '';
    elements.yamlOutput.textContent = currentYaml;

    if (response.chapters && response.chapters.length > 0) {
        currentTitle = response.chapters[0].title || 'script';
        currentTitle = currentTitle.replace(/[^\w一-龥]/g, '_');
    }
}

function showError(message) {
    elements.resultSection.style.display = 'none';
    elements.errorSection.style.display = 'block';
    elements.errorContent.innerHTML = `<p>${message}</p>`;
}

async function generateScript() {
    const novelText = elements.novelText.value.trim();
    if (!novelText) {
        showError('请输入小说文本');
        return;
    }

    const chapterCount = countChapters(novelText);
    if (chapterCount < 3) {
        showError(`检测到 ${chapterCount} 个章节，至少需要 3 个章节才能生成剧本`);
        return;
    }

    elements.generateBtn.disabled = true;
    showStatus('正在生成剧本，请稍候...', true);
    hideResult();
    hideError();

    const request = {
        novelText: novelText,
        scriptType: elements.scriptType.value,
        targetLanguage: elements.targetLanguage.value,
        adaptationStyle: elements.adaptationStyle.value,
        targetDurationMinutes: parseInt(elements.targetDuration.value) || 30
    };

    try {
        const response = await fetch(`${API_BASE}/scripts/generate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(request)
        });

        const data = await response.json();

        hideStatus();

        if (data.success) {
            showResult(data);
        } else {
            showError(data.message || '剧本生成失败');
        }
    } catch (error) {
        hideStatus();
        showError('网络错误: ' + error.message);
    } finally {
        elements.generateBtn.disabled = false;
    }
}

function hideResult() {
    elements.resultSection.style.display = 'none';
}

function hideError() {
    elements.errorSection.style.display = 'none';
}

async function copyYaml() {
    if (!currentYaml) return;

    try {
        await navigator.clipboard.writeText(currentYaml);
        const originalText = elements.copyBtn.textContent;
        elements.copyBtn.textContent = '已复制!';
        setTimeout(() => {
            elements.copyBtn.textContent = originalText;
        }, 2000);
    } catch (err) {
        const textarea = document.createElement('textarea');
        textarea.value = currentYaml;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        elements.copyBtn.textContent = '已复制!';
        setTimeout(() => {
            elements.copyBtn.textContent = '复制 YAML';
        }, 2000);
    }
}

function downloadYaml() {
    if (!currentYaml) return;

    const blob = new Blob([currentYaml], { type: 'text/yaml;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${currentTitle}.yaml`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

elements.novelText.addEventListener('input', updateChapterHint);
elements.generateBtn.addEventListener('click', generateScript);
elements.copyBtn.addEventListener('click', copyYaml);
elements.downloadBtn.addEventListener('click', downloadYaml);

document.addEventListener('DOMContentLoaded', () => {
    console.log('AI 小说转剧本工具已加载');
});
