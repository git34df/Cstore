import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/enviroment';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root',
})
export class ClienteService {
  private apiUrl = `${environment.apiUrl}/cliente`;

  constructor(
    private http: HttpClient,
    private tokenService: TokenService,
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.tokenService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  // Lista todos los clientes con resumen de compras
  getAllClientes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/getAll`, {
      headers: this.getHeaders(),
    });
  }

  getClienteResumen(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/resumen/${id}`, {
      headers: this.getHeaders(),
    });
  }
}
