import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { TokenService } from './token.service';
import { environment } from '../../enviroments/enviroment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/usuario`;

  constructor(private http: HttpClient, private tokenService: TokenService) {}

  // ── LOGIN ────────────────────────────────────────────────────
  login(credentials: { email: string; password: string }): Observable<any> {
    const body = {
      email: credentials.email,
      password: credentials.password,
    };

    return this.http.post(`${this.apiUrl}/Login`, body, { responseType: 'text' }).pipe(
      tap((token: string) => {
        if (token && typeof token === 'string') {
          this.tokenService.saveToken(token);
        } else {
          console.error('Token inválido o vacío:', token);
        }
      })
    );
  }

  // ── SIGNUP (solo admin puede invocar esta pantalla) ──────────
  signup(data: {
    nombre: string;
    numerocontacto: string;
    email: string;
    contraseña: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/signup`, data, { responseType: 'text' });
  }

  // ── CAMBIAR CONTRASEÑA ───────────────────────────────────────
  changePassword(data: { oldPassword: string; newPassword: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/ChangePassword`, data);
  }

  // ── OLVIDAR CONTRASEÑA ───────────────────────────────────────
  forgotPassword(email: string) {
    return this.http.post(
      `${this.apiUrl}/ForgotPassword`,
      { email },
      { responseType: 'text' }
    );
  }

  // ── VALIDAR SESIÓN ───────────────────────────────────────────
  isLoggedIn(): boolean {
    return !!this.tokenService.getToken();
  }

  // ── OBTENER ROL DESDE EL JWT ─────────────────────────────────
  // El backend incluye el claim "rol" en el token al hacer login.
  // JwtUtil.generateToken() hace: claims.put("rol", role)
  getUserRole(): string {
    const token = this.tokenService.getToken();
    if (!token) return '';
    try {
      const decoded: any = jwtDecode(token);
      return decoded?.rol ?? '';
    } catch {
      return '';
    }
  }

  // ── VERIFICAR SI ES ADMIN ────────────────────────────────────
  isAdmin(): boolean {
    return this.getUserRole() === 'admin';
  }

  // ── CERRAR SESIÓN ────────────────────────────────────────────
  logout(): void {
    this.tokenService.removeToken();
  }

  // ── VERIFICAR AUTENTICACIÓN (alias) ─────────────────────────
  isAuthenticated(): boolean {
    return !!this.tokenService.getToken();
  }
}