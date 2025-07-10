import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

function ProjectList() {
  const [projects, setProjects] = useState([]);
  const navigate = useNavigate();

  const fetchProjects = async () => {
    try {
      const res = await axios.get('http://localhost:8081/api/projects');
      setProjects(res.data);
    } catch (err) {
      alert('Projeler alınamadı!');
    }
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  const handleDelete = async (id) => {
    if (!window.confirm('Bu projeyi silmek istediğinize emin misiniz?')) return;
    try {
      await axios.delete(`http://localhost:8081/api/projects/${id}`);
      setProjects(projects.filter(p => p.id !== id));
    } catch (err) {
      alert('Silme işlemi başarısız!');
    }
  };

  return (
    <div>
      <h2>Projeler</h2>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Ad</th>
            <th>Açıklama</th>
            <th>Durum</th>
            <th>İşlemler</th>
          </tr>
        </thead>
        <tbody>
          {projects.map(project => (
            <tr key={project.id}>
              <td>{project.id}</td>
              <td>{project.name}</td>
              <td>{project.description}</td>
              <td>{project.status}</td>
              <td>
                <button onClick={() => navigate(`/edit/${project.id}`)} style={{ marginRight: 8 }}>Düzenle</button>
                <button onClick={() => handleDelete(project.id)} style={{ color: 'red' }}>Sil</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default ProjectList; 