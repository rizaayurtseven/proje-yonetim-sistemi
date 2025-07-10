import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const statusOptions = [
  { value: 'YENI', label: 'Yeni' },
  { value: 'DEVAM_EDIYOR', label: 'Devam Ediyor' },
  { value: 'TAMAMLANDI', label: 'Tamamlandı' },
];

function AddProject() {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState('YENI');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8081/api/projects', {
        name,
        description,
        status,
      });
      navigate('/');
    } catch (err) {
      alert('Proje eklenemedi!');
    }
  };

  return (
    <div>
      <h2>Proje Ekle</h2>
      <form onSubmit={handleSubmit} style={{ maxWidth: 400 }}>
        <div style={{ marginBottom: 12 }}>
          <label>Ad:</label><br />
          <input value={name} onChange={e => setName(e.target.value)} required style={{ width: '100%' }} />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>Açıklama:</label><br />
          <input value={description} onChange={e => setDescription(e.target.value)} required style={{ width: '100%' }} />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>Durum:</label><br />
          <select value={status} onChange={e => setStatus(e.target.value)} style={{ width: '100%' }}>
            {statusOptions.map(opt => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>
        <button type="submit">Kaydet</button>
      </form>
    </div>
  );
}

export default AddProject; 