import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

const Register: React.FC = () => {
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const navigate = useNavigate();

    const handleRegister = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, email, password }),
            });

            if (response.ok) {
                setSuccess('Registration successful! Redirecting to login...');
                setTimeout(() => {
                    navigate('/login');
                }, 2000);
            } else {
                const errorText = await response.text();
                setError(errorText || 'Registration failed!');
            }
        } catch (error) {
            console.error('Registration error:', error);
            setError('An error occurred during registration.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="page-container" style={{ maxWidth: '480px' }}>
            <div className="halo-card" style={{ padding: '2rem' }}>
                <div className="page-header" style={{ marginBottom: '2rem' }}>
                    <h2>회원가입</h2>
                    <p>Halo와 함께 아이디어를 현실로 만드세요.</p>
                </div>
                <form onSubmit={handleRegister}>
                    <div className="halo-form-group">
                        <label className="halo-label" htmlFor="username">사용자 이름</label>
                        <input
                            type="text"
                            id="username"
                            className="halo-input"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            placeholder="username"
                        />
                    </div>
                    <div className="halo-form-group">
                        <label className="halo-label" htmlFor="email">이메일</label>
                        <input
                            type="email"
                            id="email"
                            className="halo-input"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            placeholder="you@example.com"
                        />
                    </div>
                    <div className="halo-form-group">
                        <label className="halo-label" htmlFor="password">비밀번호</label>
                        <input
                            type="password"
                            id="password"
                            className="halo-input"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            placeholder="••••••••"
                        />
                    </div>
                    {error && <p className="error-message">{error}</p>}
                    {success && <p className="success-message">{success}</p>}
                    <div className="halo-form-group">
                        <button type="submit" className="btn primary" style={{ width: '100%' }} disabled={loading}>
                            {loading ? '가입 중...' : '가입하기'}
                        </button>
                    </div>
                </form>
                <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                        이미 계정이 있으신가요?{' '}
                        <Link to="/login" style={{ color: 'var(--primary)', fontWeight: '600' }}>
                            로그인
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Register;
