import React, { useState } from 'react';
import { Link } from 'react-router-dom';

interface LoginProps {
    onLoginSuccess: (token: string) => void;
}

const Login: React.FC<LoginProps> = ({ onLoginSuccess }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('accessToken', data.accessToken);
                onLoginSuccess(data.accessToken);
            } else {
                const errorText = await response.text();
                setError(errorText || 'Login failed!');
            }
        } catch (error) {
            console.error('Login error:', error);
            setError('An error occurred during login. Is the backend server running?');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="page-container" style={{ maxWidth: '480px' }}>
            <div className="halo-card" style={{ padding: '2rem' }}>
                <div className="page-header" style={{ marginBottom: '2rem' }}>
                    <h2>로그인</h2>
                    <p>Halo에 다시 오신 것을 환영합니다.</p>
                </div>
                <form onSubmit={handleLogin}>
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
                    <div className="halo-form-group">
                        <button type="submit" className="btn primary" style={{ width: '100%' }} disabled={loading}>
                            {loading ? '로그인 중...' : '로그인'}
                        </button>
                    </div>
                </form>
                <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                        계정이 없으신가요?{' '}
                        <Link to="/register" style={{ color: 'var(--primary)', fontWeight: '600' }}>
                            가입하기
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Login;

