import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const IdeaInput: React.FC = () => {
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [idea, setIdea] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !author.trim() || !idea.trim()) {
      alert('모든 필드를 입력해주세요.');
      return;
    }
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/posts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          title: title,
          author: author,
          content: idea,
        }),
      });

      if (!response.ok) {
        throw new Error('서버에 데이터를 저장하는 데 실패했습니다.');
      }

      // 성공적으로 저장 후 다음 페이지로 이동
      navigate('/market');

    } catch (error) {
      console.error('Error submitting post:', error);
      alert(error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <h2>어떤 서비스를 만들고 싶으신가요?</h2>
        <p>머릿속에 있는 아이디어를 자유롭게 적어주세요. Halo가 분석을 시작합니다.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '3rem', alignItems: 'start' }}>

        {/* Main Input Area */}
        <div className="halo-card">
          <form onSubmit={handleSubmit}>
            <div className="halo-form-group">
              <label className="halo-label" htmlFor="idea-title">아이디어 제목</label>
              <input
                id="idea-title"
                type="text"
                className="halo-input"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="예: 채식주의자를 위한 맛집 지도"
              />
            </div>
            <div className="halo-form-group">
              <label className="halo-label" htmlFor="idea-author">작성자</label>
              <input
                id="idea-author"
                type="text"
                className="halo-input"
                value={author}
                onChange={(e) => setAuthor(e.target.value)}
                placeholder="예: 김준수"
              />
            </div>
            <div className="halo-form-group">
              <label className="halo-label" htmlFor="idea-content">아이디어 상세 설명</label>
              <textarea
                id="idea-content"
                className="halo-textarea"
                value={idea}
                onChange={(e) => setIdea(e.target.value)}
                placeholder="예: 위치 기반으로 주변의 채식 식당을 찾아주고, 예약까지 가능한 앱..."
                rows={10}
                style={{ resize: 'vertical' }}
              />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
              <button type="button" className="btn secondary" disabled={loading}>임시 저장</button>
              <button type="submit" className="btn primary" disabled={loading}>
                {loading ? '저장 중...' : '시장 분석 시작하기 →'}
              </button>
            </div>
          </form>
        </div>

        {/* Side Tips */}
        <div className="halo-card" style={{ background: '#f8fafc', borderStyle: 'dashed' }}>
          <h3 style={{ marginBottom: '1rem', color: '#6366f1' }}>💡 작성 팁</h3>
          <ul style={{ display: 'flex', flexDirection: 'column', gap: '1rem', fontSize: '0.95rem', color: '#64748b' }}>
            <li>
              <strong>대상 고객 (Who)</strong>
              <br />누가 이 서비스를 가장 필요로 할까요?
            </li>
            <li>
              <strong>해결하려는 문제 (Why)</strong>
              <br />현재 그들이 겪는 불편함은 무엇인가요?
            </li>
            <li>
              <strong>주요 기능 (What)</strong>
              <br />어떤 핵심 기능으로 문제를 해결하나요?
            </li>
          </ul>
        </div>

      </div>
    </div>
  );
};

export default IdeaInput;
