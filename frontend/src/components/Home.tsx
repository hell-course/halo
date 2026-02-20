import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

// Post 데이터 타입을 정의합니다.
interface Post {
  id: number;
  title: string;
  content: string;
  // 백엔드 Post 엔티티에 다른 필드가 있다면 여기에 추가할 수 있습니다.
}

const Home: React.FC = () => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 백엔드 API로부터 게시글 데이터를 가져옵니다.
    const fetchPosts = async () => {
      try {
        const response = await fetch('/api/posts');
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        const data: Post[] = await response.json();
        setPosts(data);
      } catch (error) {
        console.error("Failed to fetch posts:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, []); // 빈 배열을 전달하여 컴포넌트가 마운트될 때 한 번만 실행되도록 합니다.

  return (
    <div className="home fade-in">
      {/* 히어로 섹션 */}
      <section className="hero">
        <div className="page-container">
          <span style={{
            background: '#e0e7ff',
            color: '#4338ca',
            padding: '0.4rem 1rem',
            borderRadius: '999px',
            fontSize: '0.9rem',
            fontWeight: 600,
            display: 'inline-block',
            marginBottom: '1.5rem'
          }}>
            Beta v1.0
          </span>
          <h1>창업 전, 당신의 아이디어를<br />가장 완벽하게 검증하세요</h1>
          <p>
            <strong>Halo</strong>는 예비 창업가를 위한 올인원 프로토타이핑 도구입니다.
            <br />
            유사 서비스 탐색부터 시각화까지, 단 몇 번의 클릭으로 시작하세요.
          </p>
          <div className="cta-buttons">
            <Link to="/idea" className="btn primary big">Halo 시작하기</Link>
          </div>
        </div>
      </section>

      {/* 게시글 목록 섹션 */}
      <section className="posts-section" style={{ padding: '4rem 0', background: '#f9fafb' }}>
        <div className="page-container">
          <div className="page-header">
            <h2>최신 게시글 (From Backend)</h2>
          </div>
          {loading ? (
            <p>게시글을 불러오는 중...</p>
          ) : posts.length > 0 ? (
            <ul style={{ listStyle: 'none', padding: 0 }}>
              {posts.map(post => (
                <li key={post.id} className="halo-card" style={{ marginBottom: '1rem' }}>
                  <h4 style={{ marginBottom: '0.5rem' }}>{post.title || '제목 없음'}</h4>
                  <p>{post.content || '내용 없음'}</p>
                </li>
              ))}
            </ul>
          ) : (
            <p>표시할 게시글이 없습니다.</p>
          )}
        </div>
      </section>

      {/* 핵심 워크플로우 (Core Workflow) */}
      <section className="workflow-section">
        <div className="page-header">
          <h2>아이디어 실현 프로세스</h2>
          <p>Halo만의 체계적인 3단계 시스템을 경험해보세요.</p>
        </div>

        <div className="workflow-steps">
          {/* Step 1 */}
          <div className="halo-card" style={{ position: 'relative' }}>
            <div style={{
              position: 'absolute', top: '-1.5rem', left: '1.5rem',
              width: '3rem', height: '3rem', background: '#6366f1', color: 'white',
              borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontWeight: 'bold', fontSize: '1.2rem', boxShadow: '0 4px 6px rgba(99, 102, 241, 0.4)'
            }}>1</div>
            <h3 style={{ marginTop: '1rem', marginBottom: '0.5rem' }}>아이디어 입력</h3>
            <p style={{ color: '#64748b', fontSize: '0.95rem' }}>머릿속의 추상적인 아이디어를 구체적인 텍스트로 정리하세요.</p>
            <Link to="/idea" className="text-link" style={{ color: '#6366f1', fontWeight: 600, fontSize: '0.9rem' }}>Go &rarr;</Link>
          </div>

          {/* Step 2 */}
          <div className="halo-card" style={{ position: 'relative', border: '1px solid #6366f1', boxShadow: '0 10px 15px -3px rgba(99, 102, 241, 0.1)' }}>
            <div style={{
              position: 'absolute', top: '-1.5rem', left: '1.5rem',
              width: '3rem', height: '3rem', background: '#6366f1', color: 'white',
              borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontWeight: 'bold', fontSize: '1.2rem', boxShadow: '0 4px 6px rgba(99, 102, 241, 0.4)'
            }}>2</div>
            <h3 style={{ marginTop: '1rem', marginBottom: '0.5rem' }}>유사장 탐색</h3>
            <p style={{ color: '#64748b', fontSize: '0.95rem' }}>AI가 전 세계 앱 스토어와 웹을 검색하여 경쟁사를 분석합니다.</p>
            <Link to="/market" className="text-link" style={{ color: '#6366f1', fontWeight: 600, fontSize: '0.9rem' }}>Go &rarr;</Link>
          </div>

          {/* Step 3 */}
          <div className="halo-card" style={{ position: 'relative' }}>
            <div style={{
              position: 'absolute', top: '-1.5rem', left: '1.5rem',
              width: '3rem', height: '3rem', background: '#6366f1', color: 'white',
              borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontWeight: 'bold', fontSize: '1.2rem', boxShadow: '0 4px 6px rgba(99, 102, 241, 0.4)'
            }}>3</div>
            <h3 style={{ marginTop: '1rem', marginBottom: '0.5rem' }}>프로토타입</h3>
            <p style={{ color: '#64748b', fontSize: '0.95rem' }}>차별점을 반영한 UI 프로토타입을 즉시 생성하고 확인하세요.</p>
            <Link to="/prototype" className="text-link" style={{ color: '#6366f1', fontWeight: 600, fontSize: '0.9rem' }}>Go &rarr;</Link>
          </div>
        </div>
      </section>

      {/* 왜 이 서비스인가? */}
      <section className="why-us" style={{ background: 'white', padding: '5rem 0' }}>
        <div className="page-container">
          <div className="page-header">
            <h2>Why Halo?</h2>
          </div>
          <div className="reasons-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            <div className="halo-card" style={{ padding: '2rem' }}>
              <h4 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>⚡ 속도 혁신</h4>
              <p style={{ color: '#64748b' }}>몇 주가 걸리던 시장 조사와 프로토타입 제작을 단 10분 만에 끝내세요.</p>
            </div>
            <div className="halo-card" style={{ padding: '2rem' }}>
              <h4 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>💸 비용 절감</h4>
              <p style={{ color: '#64748b' }}>개발자와 디자이너를 고용하기 전에 아이디어의 가치를 먼저 증명하세요.</p>
            </div>
          </div>
        </div>
      </section>

      <footer className="footer">
        <p>Halo &copy; 2026. Empowering Builders.</p>
      </footer>
    </div>
  );
};

export default Home;
