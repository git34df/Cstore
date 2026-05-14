export interface User {
  id?: number;
  nombreUsuario: string;
  email: string;
  rol: 'admin' | 'usuario';
  token?: string; // se llenará después del login
}