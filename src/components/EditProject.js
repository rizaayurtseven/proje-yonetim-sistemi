import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

const statusOptions = [
  { value: 'YENI', label: 'Yeni' },
  { value: 'DEVAM_EDIYOR', label: 'Devam Ediyor' },
  { value: 'TAMAMLANDI', label: 'Tamamlandı' },
];

function EditProject() {
  const { id } = useParams();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState('YENI');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    axios.get(`http://localhost:8081/api/projects/${id}`)
      .then(res => {
        setName(res.data.name);
        setDescription(res.data.description);
        setStatus(res.data.status);
        setLoading(false);
      })
      .catch(() => {
        alert('Proje bulunamadı!');
        navigate('/');
      });
  }, [id, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.put(`http://localhost:8081/api/projects/${id}`, {
        name,
        description,
        status,
      });
      navigate('/');
    } catch (err) {
      alert('Güncelleme başarısız!');
    }
  };

  if (loading) return <div>Yükleniyor...</div>;

  return (
    <div>
      <h2>Proje Düzenle</h2>
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
        <button type="submit">Güncelle</button>
      </form>
    </div>
  );
}

export default EditProject; 