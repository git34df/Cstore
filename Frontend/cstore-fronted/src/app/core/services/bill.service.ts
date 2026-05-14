import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/enviroment';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root',
})
export class BillService {
  private apiUrl = `${environment.apiUrl}/Factura`;

  constructor(private http: HttpClient, private tokenService: TokenService) {}

  private getAuthHeaders(): HttpHeaders {
    const rawToken = this.tokenService.getToken();

    let headers = new HttpHeaders({ 'Content-Type': 'application/json' });

    if (rawToken) {
      const token = rawToken.replace(/^"|"$/g, '');
      headers = headers.set('Authorization', `Bearer ${token}`);
    } else {
      console.warn('No JWT found. Request will be sent without Authorization header.');
    }

    return headers;
  }

  // Generar factura
  generateReport(data: any): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.post(`${this.apiUrl}/generateReport`, data, { headers });
  }

  // Obtener todas las facturas
  getBills(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get(`${this.apiUrl}/getBills`, { headers });
  }

  // Obtener PDF
  getPdf(uuid: string): Observable<Blob> {
  const token = localStorage.getItem('token'); 
  let headers = new HttpHeaders();
  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }

  const payload = { uuid }; 

  return this.http.post(`${this.apiUrl}/getPdf`, payload, {
    responseType: 'blob',
    headers,
  });
}

  // Eliminar factura
  deleteBill(id: number): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.post(`${this.apiUrl}/delete/${id}`, {}, { headers });
  }
}

