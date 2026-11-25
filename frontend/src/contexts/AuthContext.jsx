import { createContext, useContext, useState } from 'react';

const Ctx = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const s = sessionStorage.getItem('coc_session');
      return s ? JSON.parse(s) : null;
    } catch {
      sessionStorage.removeItem('coc_session');
      return null;
    }
  });

  const login = data => {
    sessionStorage.setItem('coc_session', JSON.stringify(data));
    setUser(data);
  };

  const logout = () => {
    sessionStorage.removeItem('coc_session');
    setUser(null);
  };

  return (
    <Ctx.Provider value={{ user, login, logout }}>
      {children}
    </Ctx.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(Ctx);
