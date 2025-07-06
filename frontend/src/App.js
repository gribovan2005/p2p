import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

const API = '/api';

function App() {
  const [token, setToken] = useState(localStorage.getItem('jwt') || '');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [accounts, setAccounts] = useState([]);
  const [balance, setBalance] = useState(null);
  const [newBalance, setNewBalance] = useState('');
  const [fromId, setFromId] = useState('');
  const [toId, setToId] = useState('');
  const [amount, setAmount] = useState('');
  const [transfers, setTransfers] = useState([]);
  const [view, setView] = useState(token ? 'accounts' : 'login');
  const [message, setMessage] = useState('');
  const [accountsChanged, setAccountsChanged] = useState(0);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [selectedTransfers, setSelectedTransfers] = useState([]);
  const [profilePhotoFile, setProfilePhotoFile] = useState(null);
  const [currentPhotoData, setCurrentPhotoData] = useState('');
  const [age, setAge] = useState('');
  const [profileName, setProfileName] = useState('');
  const [toCardNumber, setToCardNumber] = useState('');

  useEffect(() => {
    if (token && view === 'accounts') fetchAccounts();
    if (token && view === 'dashboard') {
      axios.get(`${API}/auth/profile`, { headers: { Authorization: `Bearer ${token}` } })
        .then(res => {
          setProfileName(res.data.name || '');
          setAge(res.data.age || '');
          setCurrentPhotoData(res.data.photoUrl || '');
          setProfilePhotoFile(null); 
        })
        .catch(() => {
          setProfileName('');
          setAge('');
          setCurrentPhotoData('');
          setProfilePhotoFile(null);
        });
    }
  }, [token, view, accountsChanged]);

  function saveToken(t) {
    setToken(t);
    localStorage.setItem('jwt', t);
    setView('dashboard');
  }

  async function register(e) {
    e.preventDefault();
    try {
      await axios.post(`${API}/auth/register`, { username, password });
      setMessage('Регистрация успешна! Теперь войдите.');
      setView('login');
    } catch (e) {
      setMessage('Ошибка регистрации: ' + (e.response?.data?.message || e.message));
    }
  }

  async function login(e) {
    e.preventDefault();
    try {
      const res = await axios.post(`${API}/auth/login`, { username, password });
      saveToken(res.data.token);
      setMessage('');
    } catch (e) {
      setMessage('Ошибка входа: ' + (e.response?.data?.message || e.message));
    }
  }

  async function fetchAccounts() {
    try {
      const res = await axios.get(`${API}/accounts`, { headers: { Authorization: `Bearer ${token}` } });
      setAccounts(res.data);
    } catch (e) {
      setMessage('Ошибка загрузки счетов');
    }
  }

  async function createAccount(e) {
    e.preventDefault();
    const sum = Number(newBalance);
    if (!Number.isFinite(sum) || sum <= 0) {
      setMessage('Введите положительную сумму для открытия счета');
      return;
    }
    try {
      await axios.post(`${API}/accounts`, { initialBalance: sum }, { headers: { Authorization: `Bearer ${token}` } });
      setNewBalance('');
      setAccountsChanged(c => c + 1);
      setMessage('');
    } catch (e) {
      setMessage('Ошибка создания счета');
    }
  }

  async function getBalance(id) {
    try {
      const res = await axios.get(`${API}/accounts/${id}/balance`, { headers: { Authorization: `Bearer ${token}` } });
      setBalance(res.data);
    } catch (e) {
      setMessage('Ошибка получения баланса');
    }
  }

  async function closeAccount(id) {
    try {
      await axios.post(`${API}/accounts/${id}/close`, {}, { headers: { Authorization: `Bearer ${token}` } });
      setAccountsChanged(c => c + 1);
    } catch (e) {
      setMessage('Ошибка закрытия счета');
    }
  }

  async function makeTransfer(e) {
    e.preventDefault();
    const fromIdx = Number(fromId) - 1;
    const toIdx = Number(toId) - 1;
    const amt = Number(amount);
    if (!Number.isFinite(fromIdx) || fromIdx < 0 || fromIdx >= accounts.length) {
      setMessage('Некорректный номер счета-отправителя');
      return;
    }
    if (!Number.isFinite(toIdx) || toIdx < 0 || toIdx >= accounts.length) {
      setMessage('Некорректный номер счета-получателя');
      return;
    }
    if (!Number.isFinite(amt) || amt <= 0) {
      setMessage('Введите положительную сумму для перевода');
      return;
    }
    const fromAccountId = accounts[fromIdx].id;
    const toAccountId = accounts[toIdx].id;
    try {
      await axios.post(`${API}/transfers`, { fromAccountId, toAccountId, amount: amt }, { headers: { Authorization: `Bearer ${token}` } });
      setMessage('Перевод выполнен!');
      setFromId(''); setToId(''); setAmount('');
      setAccountsChanged(c => c + 1);
    } catch (e) {
      setMessage('Ошибка перевода: ' + (e.response?.data?.message || e.message));
    }
  }

  async function fetchTransfers(id) {
    try {
      const res = await axios.get(`${API}/transfers/account/${id}`, { headers: { Authorization: `Bearer ${token}` } });
      setTransfers(res.data);
    } catch (e) {
      setMessage('Ошибка истории переводов');
    }
  }

  function logout() {
    setToken('');
    localStorage.removeItem('jwt');
    setView('login');
  }

  async function saveProfile(e) {
    e.preventDefault();
    
    let photoDataToSend = null;
    
    if (profilePhotoFile) {
      try {
        const base64 = await convertFileToBase64(profilePhotoFile);
        if (base64.length > 500000) { 
          setMessage('Файл слишком большой (максимум 500KB)');
          return;
        }
        photoDataToSend = base64;
      } catch (error) {
        setMessage('Ошибка обработки файла');
        return;
      }
    }
    
    try {
      await axios.post(`${API}/auth/profile`, {
        name: profileName,
        age: age ? Number(age) : null,
        photoUrl: photoDataToSend
      }, { headers: { Authorization: `Bearer ${token}` } });
      setMessage('Профиль успешно сохранён!');
    } catch (e) {
      const errorMsg = e.response?.data?.message || e.response?.data || 'Ошибка сохранения профиля';
      setMessage('Ошибка: ' + errorMsg);
    }
  }

  function convertFileToBase64(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  async function makeTransferByCard(e) {
    e.preventDefault();
    const fromIdx = Number(fromId) - 1;
    const amt = Number(amount);
    if (!Number.isFinite(fromIdx) || fromIdx < 0 || fromIdx >= accounts.length) {
      setMessage('Некорректный номер счета-отправителя');
      return;
    }
    if (!/^[0-9]{16}$/.test(toCardNumber)) {
      setMessage('Введите корректный 16-значный номер карты');
      return;
    }
    if (!Number.isFinite(amt) || amt <= 0) {
      setMessage('Введите положительную сумму для перевода');
      return;
    }
    const fromAccountId = accounts[fromIdx].id;
    try {
      await axios.post(`${API}/transfers/by-card`, { fromAccountId, toCardNumber, amount: amt }, { headers: { Authorization: `Bearer ${token}` } });
      setMessage('Перевод по карте выполнен!');
      setFromId(''); setToCardNumber(''); setAmount('');
      setAccountsChanged(c => c + 1);
    } catch (e) {
      setMessage('Ошибка перевода: ' + (e.response?.data || e.message));
    }
  }

  function Header() {
    return (
      <div className="header">
        <div className="header-title">P2P Банк</div>
        <button className="header-btn" onClick={() => setView('dashboard')}>Личный кабинет</button>
      </div>
    );
  }

  if (!token && view !== 'register') {
    return (
      <div className="auth-page">
        <div className="auth-container">
          <h2>Вход</h2>
          <form onSubmit={login}>
            <input placeholder="Логин" value={username} onChange={e => setUsername(e.target.value)} />
            <input placeholder="Пароль" type="password" value={password} onChange={e => setPassword(e.target.value)} />
            <div className="auth-btns">
              <button type="submit" className="auth-btn">Войти</button>
              <button type="button" className="auth-btn" onClick={() => setView('register')}>Регистрация</button>
            </div>
          </form>
          <div className="message">{message}</div>
        </div>
      </div>
    );
  }

  if (view === 'register') {
    return (
      <div className="auth-page">
        <div className="auth-container">
          <h2>Регистрация</h2>
          <form onSubmit={register}>
            <input placeholder="Логин" value={username} onChange={e => setUsername(e.target.value)} />
            <input placeholder="Пароль" type="password" value={password} onChange={e => setPassword(e.target.value)} />
            <div className="auth-btns">
              <button type="submit" className="auth-btn">Зарегистрироваться</button>
              <button type="button" className="auth-btn" onClick={() => setView('login')}>Назад</button>
            </div>
          </form>
          <div className="message">{message}</div>
        </div>
      </div>
    );
  }

  if (view === 'dashboard') {
    return (
      <>
        <Header />
        <div className="auth-page">
          <div className="auth-container" style={{ width: '100%', maxWidth: 420 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, width: '100%' }}>
              <h2 style={{ margin: 0 }}>Личный кабинет</h2>
              <button style={{ background: '#e11d48', width: 90 }} onClick={logout}>Выйти</button>
            </div>
            <form className="profile-form" onSubmit={saveProfile}>
              <label>Имя:</label>
              <input value={profileName} onChange={e => setProfileName(e.target.value)} placeholder="Введите имя" />
              
              <label>Фото профиля:</label>
              {(profilePhotoFile || currentPhotoData) && (
                <img 
                  src={profilePhotoFile ? URL.createObjectURL(profilePhotoFile) : currentPhotoData} 
                  alt="profile" 
                  className="profile-photo" 
                  onError={e => e.target.style.display = 'none'} 
                />
              )}
              
              <input 
                type="file" 
                accept="image/*" 
                onChange={e => {
                  const file = e.target.files[0];
                  if (file) {
                    setProfilePhotoFile(file);
                  }
                }}
              />
              
              <label>Возраст:</label>
              <input type="number" min="0" max="120" value={age} onChange={e => setAge(e.target.value)} placeholder="Введите возраст" />
              <button type="submit">Сохранить профиль</button>
            </form>
            <div style={{ marginBottom: 24 }}>
              <b>Пользователь:</b> {username || 'Вы'}
            </div>
            <div >
              <button className="nav-btn" onClick={() => setView('accounts')}>Мои счета</button>
              <button className="nav-btn" onClick={() => setView('transfer')}>Перевод</button>
            </div>
            <div className="message">{message}</div>
          </div>
        </div>
      </>
    );
  }

  if (view === 'accounts') {
    return (
      <>
        <Header />
        <div className="main-container dashboard-bg">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <h2 style={{ margin: 0 }}>Ваши счета</h2>
            <button style={{ background: '#64748b', width: 90 }} onClick={() => setView('dashboard')}>Назад</button>
          </div>
          <ul>
            {accounts.map((acc, idx) => (
              <li key={acc.id}>
                <div>
                  <b>Счет №{idx + 1}</b> | Баланс: <b>{acc.balance}</b> | <span style={{ color: acc.closed ? '#e11d48' : '#059669' }}>{acc.closed ? 'Закрыт' : 'Открыт'}</span><br/>
                  <span style={{ fontSize: '0.95em', color: '#64748b' }}>Карта: <b>{acc.cardNumber}</b></span>
                </div>
                <div className="account-actions">
                  <button onClick={() => { setSelectedAccount(acc); setView('account'); }} style={{ background: '#0ea5e9' }}>Детали</button>
                  <button onClick={() => closeAccount(acc.id)} disabled={acc.closed} style={{ background: '#f59e42' }}>Закрыть</button>
                  <button onClick={async () => { await fetchTransfers(acc.id); setSelectedTransfers(transfers); setView('transfers'); }} style={{ background: '#6366f1' }}>История</button>
                </div>
              </li>
            ))}
          </ul>
          <form onSubmit={createAccount} style={{ marginTop: 24 }}>
            <input placeholder="Начальный баланс" value={newBalance} onChange={e => setNewBalance(e.target.value)} />
            <button type="submit">Открыть счет</button>
          </form>
          <div className="message">{message}</div>
        </div>
      </>
    );
  }

  if (view === 'transfer') {
    return (
      <>
        <Header />
        <div className="main-container dashboard-bg centered-form">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, width: '100%' }}>
            <h2 style={{ margin: 0 }}>Перевод</h2>
            <button style={{ background: '#64748b', width: 90 }} onClick={() => setView('dashboard')}>Назад</button>
          </div>
          <form onSubmit={makeTransfer} style={{ width: '100%', maxWidth: 400 }}>
            <input placeholder="Счет-отправитель (№)" value={fromId} onChange={e => setFromId(e.target.value)} />
            <input placeholder="Счет-получатель (№)" value={toId} onChange={e => setToId(e.target.value)} />
            <input placeholder="Сумма" value={amount} onChange={e => setAmount(e.target.value)} />
            <button type="submit" className="nav-btn">Перевести по номеру счета</button>
          </form>
          <form onSubmit={makeTransferByCard} style={{ width: '100%', maxWidth: 400, marginTop: 16 }}>
            <input placeholder="Счет-отправитель (№)" value={fromId} onChange={e => setFromId(e.target.value)} />
            <input placeholder="Карта получателя (16 цифр)" value={toCardNumber} onChange={e => setToCardNumber(e.target.value)} />
            <input placeholder="Сумма" value={amount} onChange={e => setAmount(e.target.value)} />
            <button type="submit" className="nav-btn">Перевести по карте</button>
          </form>
          <div className="message">{message}</div>
        </div>
      </>
    );
  }

  if (view === 'account' && selectedAccount) {
    const idx = accounts.findIndex(a => a.id === selectedAccount.id);
    return (
      <>
        <Header />
        <div className="main-container dashboard-bg">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <h2 style={{ margin: 0 }}>Счет №{idx + 1}</h2>
            <button style={{ background: '#64748b', width: 90 }} onClick={() => setView('accounts')}>Назад</button>
          </div>
          <div style={{ marginBottom: 12 }}>
            <b>Баланс:</b> {selectedAccount.balance}
          </div>
          <div style={{ marginBottom: 12 }}>
            <b>Статус:</b> <span style={{ color: selectedAccount.closed ? '#e11d48' : '#059669' }}>{selectedAccount.closed ? 'Закрыт' : 'Открыт'}</span>
          </div>
          <button style={{ background: '#6366f1', width: '100%' }} onClick={async () => { await fetchTransfers(selectedAccount.id); setSelectedTransfers(transfers); setView('transfers'); }}>История переводов</button>
          <button style={{ background: '#f59e42', width: '100%', marginTop: 8 }} onClick={() => closeAccount(selectedAccount.id)} disabled={selectedAccount.closed}>Закрыть счет</button>
        </div>
      </>
    );
  }

  if (view === 'transfers') {
    return (
      <>
        <Header />
        <div className="main-container dashboard-bg">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <h2 style={{ margin: 0 }}>История переводов</h2>
            <button style={{ background: '#64748b', width: 90 }} onClick={() => setView(selectedAccount ? 'account' : 'accounts')}>Назад</button>
          </div>
          <ul>
            {selectedTransfers.length === 0 && <li>Нет переводов</li>}
            {selectedTransfers.map(t => (
              <li key={t.id}>
                <div>#{t.id}: <b>{t.fromAccountId}</b> → <b>{t.toAccountId}</b> | <b>{t.amount}</b> | <span style={{ color: '#64748b' }}>{t.createdAt}</span></div>
              </li>
            ))}
          </ul>
        </div>
      </>
    );
  }

  return (
    <div className="main-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>Ваши счета</h2>
        <button style={{ background: '#e11d48', width: 90 }} onClick={logout}>Выйти</button>
      </div>
      <form onSubmit={createAccount}>
        <input placeholder="Начальный баланс" value={newBalance} onChange={e => setNewBalance(e.target.value)} />
        <button type="submit">Открыть счет</button>
      </form>
      <ul>
        {accounts.map(acc => (
          <li key={acc.id}>
            <div>
              <b>Счет #{acc.id}</b> | Баланс: <b>{acc.balance}</b> | <span style={{ color: acc.closed ? '#e11d48' : '#059669' }}>{acc.closed ? 'Закрыт' : 'Открыт'}</span>
            </div>
            <div className="account-actions">
              <button onClick={() => getBalance(acc.id)} style={{ background: '#0ea5e9' }}>Баланс</button>
              <button onClick={() => closeAccount(acc.id)} disabled={acc.closed} style={{ background: '#f59e42' }}>Закрыть</button>
              <button onClick={() => { fetchTransfers(acc.id); setView('transfers'); }} style={{ background: '#6366f1' }}>История</button>
            </div>
          </li>
        ))}
      </ul>
      {balance !== null && <div className="balance-info">Баланс выбранного счета: <b>{balance}</b></div>}
      <h3>Перевод</h3>
      <form onSubmit={makeTransfer}>
        <input placeholder="Счет-отправитель" value={fromId} onChange={e => setFromId(e.target.value)} />
        <input placeholder="Счет-получатель" value={toId} onChange={e => setToId(e.target.value)} />
        <input placeholder="Сумма" value={amount} onChange={e => setAmount(e.target.value)} />
        <button type="submit">Перевести</button>
      </form>
      <div className="message">{message}</div>
      {view === 'transfers' && (
        <div>
          <h3>История переводов</h3>
          <button style={{ background: '#64748b', marginBottom: 8 }} onClick={() => setView('accounts')}>Назад к счетам</button>
          <ul>
            {transfers.map(t => (
              <li key={t.id}>
                <div>#{t.id}: <b>{t.fromAccountId}</b> → <b>{t.toAccountId}</b> | <b>{t.amount}</b> | <span style={{ color: '#64748b' }}>{t.createdAt}</span></div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

export default App;
