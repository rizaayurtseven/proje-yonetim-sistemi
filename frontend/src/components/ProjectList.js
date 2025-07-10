import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { ThemeContext } from '../ThemeContext';

const ProjectList = () => {
  const [projects, setProjects] = useState([]);
  const [error, setError] = useState('');
  const token = localStorage.getItem('token');
  const { theme } = useContext(ThemeContext);

  // Kullanıcı bilgisi ve admin kontrolü
  let username = '';
  let isAdmin = false;
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      username = payload.sub || payload.username || '';
      isAdmin = (payload.roles && (payload.roles.includes('ROLE_ADMIN') || payload.roles.includes('ADMIN')))
        || (payload.authorities && (payload.authorities.includes('ROLE_ADMIN') || payload.authorities.includes('ADMIN')))
        || (payload.role && (payload.role === 'ADMIN' || payload.role === 'ROLE_ADMIN'));
    } catch {}
  }

  useEffect(() => {
    if (!token) {
      setError('Lütfen giriş yapın.');
      setProjects([]);
      return;
    }
    setError('');
    axios.get('http://localhost:9000/api/projects', {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => setProjects(res.data))
      .catch(err => setError('Projeler alınamadı!'));
  }, [token]);

  if (!token) return <div style={{marginTop:40, textAlign:'center'}}>Lütfen giriş yapın.</div>;
  if (error) return <div style={{marginTop:40, color:'red', textAlign:'center'}}>{error}</div>;

  const handleDelete = (projectId) => {
    if (window.confirm('Bu projeyi silmek istediğinize emin misiniz?')) {
      axios.delete(`http://localhost:9000/api/projects/${projectId}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
        .then(() => {
          setProjects(projects.filter(p => p.id !== projectId));
          alert('Proje başarıyla silindi.');
        })
        .catch(err => {
          setError('Proje silinemedi!');
          console.error(err);
        });
    }
  };

  return (
    <div>
      <h2>Projeler</h2>
      <ul style={{listStyle:'none', padding:0}}>
        {projects.map(p => (
          <li key={p.id} style={{marginBottom:20, background:'var(--card)', borderRadius:8, padding:16, boxShadow:'0 2px 8px 0 rgba(59,130,246,0.08)'}}>
            <div>
              <span
                style={{
                  color: p.files && p.files.length > 0 ? 'var(--primary)' : 'var(--text)',
                  textDecoration: p.files && p.files.length > 0 ? 'underline' : 'none',
                  cursor: p.files && p.files.length > 0 ? 'pointer' : 'default',
                  fontWeight: 600,
                  fontSize: '1.1rem'
                }}
                onClick={() => {
                  if (p.files && p.files.length > 0) {
                    // Dosya backend'de uploads klasöründe tutuluyor, path'i filePath
                    // Bir endpoint ile dosya sunulmalı, şimdilik doğrudan filePath ile açmayı deniyoruz
                    const fileUrl = `http://localhost:9000/${p.files[0].filePath.replace('\\','/').replace('uploads','uploads')}`;
                    window.open(fileUrl, '_blank');
                  }
                }}
              >
                {p.name}
              </span>
              {/* Sil butonu: admin ise her projede, normal kullanıcı ise sadece owner olduğu projede */}
              {(isAdmin || (p.owner && p.owner.username === username)) && (
                <button onClick={() => handleDelete(p.id)} style={{ marginLeft: 16, background: '#ef4444', color: '#fff' }}>Sil</button>
              )}
            </div>
            <div style={{marginTop:6, color:'var(--text)'}}>{p.description}</div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ProjectList; 