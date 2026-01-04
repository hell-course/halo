import React, { useState, useEffect } from 'react';

// Post 객체의 타입을 정의합니다.
interface Post {
  id: number;
  title: string;
  content: string;
  author: string;
  createdAt: string;
}

const IdeaInput: React.FC = () => {
  // 폼 입력 상태 관리
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [author, setAuthor] = useState('');

  // 백엔드에서 받아온 포스트 목록 상태 관리
  const [posts, setPosts] = useState<Post[]>([]);

  // 백엔드 API로부터 포스트 목록을 가져오는 함수
  const fetchPosts = async () => {
    try {
      const response = await fetch('/api/posts');
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const data: Post[] = await response.json();
      setPosts(data);
    } catch (error) {
      console.error('There was a problem with your fetch operation:', error);
    }
  };

  // 컴포넌트가 처음 렌더링될 때 포스트 목록을 불러옵니다.
  useEffect(() => {
    fetchPosts();
  }, []);

  // 폼 제출 핸들러
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !content.trim() || !author.trim()) return;

    const newPost = { title, content, author };

    try {
      const response = await fetch('/api/posts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(newPost),
      });

      if (!response.ok) {
        throw new Error('Network response was not ok');
      }

      // 포스트 생성 후 목록을 다시 불러오고 폼을 초기화합니다.
      fetchPosts();
      setTitle('');
      setContent('');
      setAuthor('');
    } catch (error) {
      console.error('There was a problem with your fetch operation:', error);
    }
  };

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <h2>아이디어 공유하기</h2>
        <p>팀원들과 아이디어를 공유하고 발전시켜 보세요.</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '3rem', alignItems: 'start' }}>

        {/* Post Creation Form */}
        <div className="halo-card">
          <form onSubmit={handleSubmit}>
            <div className="halo-form-group">
              <label className="halo-label">제목</label>
              <input
                type="text"
                className="halo-input"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="아이디어의 핵심을 한 문장으로"
              />
            </div>
            <div className="halo-form-group">
              <label className="halo-label">상세 내용</label>
              <textarea
                className="halo-textarea"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="자유롭게 아이디어를 설명해주세요."
                rows={8}
              />
            </div>
            <div className="halo-form-group">
              <label className="halo-label">작성자</label>
              <input
                type="text"
                className="halo-input"
                value={author}
                onChange={(e) => setAuthor(e.target.value)}
                placeholder="이름을 입력해주세요"
              />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <button type="submit" className="btn primary">아이디어 제출하기</button>
            </div>
          </form>
        </div>

        {/* Posts List */}
        <div className="halo-card">
          <h3 style={{ marginBottom: '1rem' }}>공유된 아이디어 목록</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {posts.length > 0 ? (
              posts.map((post) => (
                <div key={post.id} style={{ border: '1px solid #e2e8f0', padding: '1rem', borderRadius: '8px' }}>
                  <h4 style={{ margin: '0 0 0.5rem 0' }}>{post.title}</h4>
                  <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.9rem' }}>{post.content}</p>
                  <small style={{ color: '#64748b' }}>작성자: {post.author} | {new Date(post.createdAt).toLocaleDateString()}</small>
                </div>
              ))
            ) : (
              <p>아직 공유된 아이디어가 없습니다.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default IdeaInput;