import React, { useState } from 'react';
import axios from 'axios';

const Login = ({ onLogin }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const response = await axios.post('http://localhost:9000/auth/login', {
        username,
        password
      });
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        if (onLogin) onLogin();
      } else {
        setError(response.data.error || 'Giriş başarısız!');
      }
    } catch (err) {
      setError('Sunucu hatası veya bağlantı sorunu!');
    }
  };

  return (
    <div className="login-container">
      <h2>Giriş Yap</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Kullanıcı Adı"
          value={username}
          onChange={e => setUsername(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Şifre"
          value={password}
          onChange={e => setPassword(e.target.value)}
          required
        />
        <button type="submit">Giriş Yap</button>
      </form>
      {error && <div style={{color:'red', marginTop:10}}>{error}</div>}
    </div>
  );
};

export default Login;
