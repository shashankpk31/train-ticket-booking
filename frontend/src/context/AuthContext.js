import React, { createContext, useEffect, useState } from 'react';
import keycloak from '../keycloak';

export const AuthContext = createContext({
  isAuthenticated: false,
  token: null,
  login: () => {},
  logout: () => {},
});

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [token, setToken] = useState(null);

  useEffect(() => {
    keycloak.init({ onLoad: 'check-sso' }).then((authenticated) => {
      setIsAuthenticated(authenticated);
      setToken(keycloak.token || null);
    });
  }, []);

  const login = () => keycloak.login();
  const logout = () => keycloak.logout();

  return (
    <AuthContext.Provider value={{ isAuthenticated, token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};