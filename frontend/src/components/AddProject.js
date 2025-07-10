import React, { useState } from 'react';
import axios from 'axios';

const AddProject = ({ onProjectAdded }) => {
  const [form, setForm] = useState({ name: '', description: '' });
  const [file, setFile] = useState(null);
  const [message, setMessage] = useState('');

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value });
  const handleFileChange = e => setFile(e.target.files[0]);

  const handleSubmit = async e => {
    e.preventDefault();
    setMessage('');
    try {
      const token = localStorage.getItem('token');
      // Önce projeyi ekle
      const res = await axios.post('http://localhost:9000/api/projects', form, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const projectId = res.data.id || res.data.projectId || res.data; // id dönen alan
      // Dosya seçildiyse yükle
      if (file && projectId) {
        const formData = new FormData();
        formData.append('file', file);
        await axios.post(`http://localhost:9000/api/projects/${projectId}/uploadFile`, formData, {
          headers: { 'Content-Type': 'multipart/form-data', Authorization: `Bearer ${token}` }
        });
      }
      setMessage('Proje ve dosya başarıyla eklendi!');
      if (onProjectAdded) onProjectAdded();
    } catch (err) {
      setMessage('Hata oluştu: ' + (err.response?.data || ''));
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{maxWidth:500, margin:'24px auto', background:'#fff', padding:24, borderRadius:8}}>
      <h3>Yeni Proje Ekle</h3>
      <input name="name" placeholder="Proje Adı" value={form.name} onChange={handleChange} required />
      <textarea name="description" placeholder="Açıklama" value={form.description} onChange={handleChange} required />
      <input type="file" onChange={handleFileChange} />
      <button type="submit">Ekle</button>
      {message && <div style={{marginTop:10, color: message.includes('başarı') ? 'green' : 'red'}}>{message}</div>}
    </form>
  );
};

export default AddProject; 