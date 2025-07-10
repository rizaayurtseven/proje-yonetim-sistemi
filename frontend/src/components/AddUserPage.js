import React from 'react';
import AddUser from './AddUser';

const AddUserPage = () => (
  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginTop: 40 }}>
    <h2>Kullanıcı Ekleme Sayfası</h2>
    <div style={{ width: '100%', maxWidth: 500 }}>
      <AddUser />
    </div>
  </div>
);

export default AddUserPage; 