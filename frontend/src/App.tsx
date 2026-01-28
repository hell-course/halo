import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useLocation, useNavigate } from 'react-router-dom';
import './App.css';
import Home from './components/Home';
import IdeaInput from './components/IdeaInput';
import PrototypeSimulator from './components/PrototypeSimulator';
import MarketResearch from './components/MarketResearch';
import Login from './components/Login';
import Register from './components/Register';

// Helper component to highlight active link
const NavLink = ({ to, children }: { to: string, children: React.ReactNode }) => {
  const location = useLocation();
  const isActive = location.pathname === to;
  return (
    <li>
      <Link to={to} className={isActive ? 'active' : ''}>
        {children}
      </Link>
    </li>
  );
};

function App() {
  const [token, setToken] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const storedToken = localStorage.getItem('accessToken');
    if (storedToken) {
      setToken(storedToken);
    }
  }, []);

  const handleLoginSuccess = (newToken: string) => {
    setToken(newToken);
    navigate('/');
  };

  const handleLogout = () => {
    setToken(null);
    localStorage.removeItem('accessToken');
    navigate('/login');
  };

  return (
    <div className="App">
      <header className="App-header">
        <Link to="/" style={{ textDecoration: 'none' }}>
          <div className="brand-logo">Halo</div>
        </Link>
        <nav>
          <ul>
            <NavLink to="/">홈</NavLink>
            <NavLink to="/idea">아이디어 입력</NavLink>
            <NavLink to="/market">시장 탐색</NavLink>
            <NavLink to="/prototype">프로토타입</NavLink>
            {token ? (
              <li><button onClick={handleLogout} className="logout-button">로그아웃</button></li>
            ) : (
              <NavLink to="/login">로그인</NavLink>
            )}
          </ul>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/idea" element={<IdeaInput />} />
          <Route path="/market" element={<MarketResearch />} />
          <Route path="/prototype" element={<PrototypeSimulator />} />
          <Route path="/login" element={<Login onLoginSuccess={handleLoginSuccess} />} />
          <Route path="/register" element={<Register />} />
        </Routes>
      </main>
    </div>
  );
}

// Wrap App with Router to use useNavigate
const AppWrapper = () => (
  <Router>
    <App />
  </Router>
);

export default AppWrapper;