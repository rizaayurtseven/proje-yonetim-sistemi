import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import ProjectList from './components/ProjectList';
import AddProject from './components/AddProject';
import EditProject from './components/EditProject';

function App() {
  return (
    <Router>
      <div style={{ maxWidth: 800, margin: '0 auto', padding: 24 }}>
        <h1>Proje Yönetim Sistemi</h1>
        <nav style={{ marginBottom: 24 }}>
          <Link to="/" style={{ marginRight: 16 }}>Projeler</Link>
          <Link to="/add">Proje Ekle</Link>
        </nav>
        <Routes>
          <Route path="/" element={<ProjectList />} />
          <Route path="/add" element={<AddProject />} />
          <Route path="/edit/:id" element={<EditProject />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App; 