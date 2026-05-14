import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/enviroment';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private apiUrl = `${environment.apiUrl}/usuario`;

  constructor(private http: HttpClient, private tokenService: TokenService) {}

  private getHeaders(): HttpHeaders {
    const token = this.tokenService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  // Listar todos los usuarios
  getAllUsuarios(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/get`, {
      headers: this.getHeaders(),
    });
  }

  // Actualizar estado (activo/inactivo)
  updateUsuario(data: { id: number; estado: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/Update`, data, {
      headers: this.getHeaders(),
    });
  }

  // Actualizar rol (admin/usuario) — solo admin puede llamar esto
  updateRol(data: { id: string; rol: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/updateRol`, data, {
      headers: this.getHeaders(),
    });
  }
}
