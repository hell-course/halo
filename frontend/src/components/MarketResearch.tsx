import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import type { ProductHuntPost } from '../types/producthunt'; // Import the interface

const MarketResearch: React.FC = () => {
  const [query, setQuery] = useState('AI 기반 채식 식당 예약 플랫폼'); // Updated initial query
  const [productHuntPosts, setProductHuntPosts] = useState<ProductHuntPost[]>([]); // State for fetched posts
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchProductHuntPosts = async () => {
    if (!query.trim()) {
      setError('Please enter a query to search.');
      return;
    }

    setIsSearching(true);
    setError(null);
    setProductHuntPosts([]); // Clear previous results

    try {
      const response = await fetch(`/api/market-research/search?query=${encodeURIComponent(query)}&limit=10`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data: ProductHuntPost[] = await response.json();
      setProductHuntPosts(data);
    } catch (e: any) {
      setError(`Failed to fetch market research: ${e.message}`);
      console.error("Market research fetch error:", e);
    } finally {
      setIsSearching(false);
    }
  };

  const handleSearch = () => {
    fetchProductHuntPosts();
  };

  // Helper for badge colors based on similarity (0.0 to 1.0)
  const getSimilarityColor = (similarity: number) => {
    const score = similarity * 100; // Convert to percentage
    if (score >= 90) return '#ef4444'; // Red for high danger
    if (score >= 80) return '#f59e0b'; // Orange
    if (score >= 70) return '#10b981'; // Green
    return '#64748b'; // Gray for lower match
  };

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <h2>시장 유사 제품 탐색</h2>
        <p>AI가 전 세계 데이터를 분석하여 가장 유사한 서비스를 찾아드립니다.</p>
      </div>

      {/* Search Bar */}
      <div className="halo-card" style={{ marginBottom: '3rem', padding: '1rem', display: 'flex', gap: '1rem', alignItems: 'center' }}>
        <input
          type="text"
          className="halo-input"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => { // Allow searching with Enter key
            if (e.key === 'Enter') {
              handleSearch();
            }
          }}
          placeholder="분석할 아이디어 키워드를 입력하세요"
          style={{ border: 'none', boxShadow: 'none', background: 'transparent', fontSize: '1.2rem', fontWeight: 500 }}
        />
        <button onClick={handleSearch} className="btn primary" style={{ minWidth: '120px' }} disabled={isSearching}>
          {isSearching ? '분석 중...' : '시장 분석'}
        </button>
      </div>

      {error && <div className="text-red-500 mb-4">{error}</div>}

      {/* Results */}
      <div className="analysis-results">
        <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          📊 분석 리포트
          {productHuntPosts.length > 0 && (
            <span style={{ fontSize: '0.9rem', fontWeight: 400, color: '#64748b' }}>
              ({productHuntPosts.length}개의 유사 서비스 발견)
            </span>
          )}
        </h3>

        {isSearching && (
          <div className="text-center text-gray-500 mb-4">
            <p>Product Hunt에서 유사 서비스를 찾고 있습니다...</p>
          </div>
        )}

        {!isSearching && productHuntPosts.length === 0 && !error && (
          <div className="text-center text-gray-500 mb-4">
            <p>검색어를 입력하고 '시장 분석' 버튼을 눌러 유사한 제품을 찾아보세요.</p>
          </div>
        )}

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', marginBottom: '3rem' }}>
          {productHuntPosts.map((post) => (
            <div key={post.id} className="halo-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
                <h4 style={{ fontSize: '1.25rem' }}>
                  {post.thumbnail && (
                    <img src={post.thumbnail.url} alt={post.name} style={{ width: '24px', height: '24px', borderRadius: '4px', marginRight: '8px' }} />
                  )}
                  <a href={post.url} target="_blank" rel="noopener noreferrer" className="text-indigo-600 hover:underline">
                    {post.name}
                  </a>
                </h4>
                <span style={{
                  background: getSimilarityColor(post.similarity),
                  color: 'white',
                  padding: '0.25rem 0.75rem',
                  borderRadius: '999px',
                  fontSize: '0.8rem',
                  fontWeight: 600
                }}>유사도 {(post.similarity * 100).toFixed(1)}%</span>
              </div>
              <p style={{ color: '#64748b', fontSize: '0.95rem', marginBottom: '1rem', minHeight: '3rem' }}>
                {post.tagline}
              </p>
              {post.description && (
                <p style={{ color: '#64748b', fontSize: '0.85rem', marginBottom: '1.5rem', maxHeight: '4.5rem', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {post.description}
                </p>
              )}
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', fontSize: '0.85rem', color: '#94a3b8', borderTop: '1px solid #f1f5f9', paddingTop: '1rem' }}>
                <span>👍 {post.votesCount}</span>
                {post.topics && post.topics.edges.map((topicEdge, idx) => (
                  <span key={idx} className="px-2 py-1 bg-gray-100 rounded-full text-xs">
                    {topicEdge.node.name}
                  </span>
                ))}
                {post.websiteUrl && (
                  <a href={post.websiteUrl} target="_blank" rel="noopener noreferrer" className="text-blue-500 hover:underline ml-auto">
                    웹사이트 &rarr;
                  </a>
                )}
              </div>
            </div>
          ))}
        </div>

        {/* CTA */}
        <div style={{ textAlign: 'center', padding: '3rem', background: '#e0e7ff', borderRadius: '12px' }}>
          <h3 style={{ marginBottom: '1rem', color: '#4338ca' }}>💡 차별화 포인트 발견!</h3>
          <p style={{ marginBottom: '2rem', color: '#4b5563' }}>
            기존 서비스들은 '예약' 기능이 부족합니다. <br />
            <strong>예약 및 결제 편의성</strong>을 강화하여 프로토타입을 만들어보세요.
          </p>
          <Link to="/prototype" className="btn primary big">프로토타입 만들러 가기 &rarr;</Link>
        </div>
      </div>
    </div>
  );
};

export default MarketResearch;