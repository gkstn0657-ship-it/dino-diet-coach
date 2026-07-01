// AI 답변(마크다운) → HTML 렌더링
import { marked } from 'marked';

marked.setOptions({
  breaks: true, // 줄바꿈을 <br>로
  gfm: true,
});

export const renderMarkdown = (text) => marked.parse(text ?? '');
