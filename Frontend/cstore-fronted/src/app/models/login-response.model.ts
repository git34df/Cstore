export interface LoginResponse {
  token: string;
  usuario: {
    id: number;
    nombreUsuario: string;
    email: string;
    rol: 'admin' | 'usuario';
  };
}