import React, { useState } from 'react';
import api from '../axiosConfig'; // Axios instance'ınız
import { useNavigate } from 'react-router-dom'; // React Router v6

const AddUserPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');
    try {
      // ADMIN endpoint'ine POST isteği
      const response = await api.post('/api/admin/users', { username, password });
      setMessage(response.data); // Backend'den dönen "Kullanıcı başarıyla eklendi." mesajı
      setUsername('');
      setPassword('');
      // navigate('/admin/users'); // İsterseniz başka bir sayfaya yönlendirin
    } catch (err) {
      console.error('Kullanıcı ekleme hatası:', err);
      if (err.response && err.response.data) {
        setError(err.response.data); // Backend'den dönen hata mesajı
      } else {
        setError('Kullanıcı eklenirken bir hata oluştu.');
      }
    }
  };

  return (
    <div>
      <h2>Yeni Kullanıcı Ekle</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Kullanıcı Adı:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div>
          <label>Şifre:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit">Kullanıcı Ekle</button>
      </form>
      {message && <p style={{ color: 'green' }}>{message}</p>}
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </div>
  );
};

export default AddUserPage;