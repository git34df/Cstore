import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/enviroment';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root',
})
export class VentaService {
  private apiUrl = `${environment.apiUrl}/Venta`;

  constructor(private http: HttpClient, private tokenService: TokenService) {}

  private getHeaders(): HttpHeaders {
    const token = this.tokenService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  getVentas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/getVentas`, {
      headers: this.getHeaders(),
    });
  }

  getVentaById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/getVenta/${id}`, {
      headers: this.getHeaders(),
    });
  }

  registrarVenta(payload: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/registrar`, payload, {
      headers: this.getHeaders(),
    });
  }

  anularVenta(id: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/anular/${id}`, {}, {
      headers: this.getHeaders(),
      responseType: 'text' as 'json',
    });
  }

  getPdf(uuid: string): Observable<Blob> {
    const token = this.tokenService.getToken();
    return this.http.post(
      `${this.apiUrl}/getPdf`,
      { uuid },
      {
        responseType: 'blob',
        headers: new HttpHeaders({ Authorization: `Bearer ${token}` }),
      }
    );
  }
}