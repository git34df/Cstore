import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/enviroment';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root',
})
export class ProductoService {
  private apiUrl = `${environment.apiUrl}/Producto`;

  constructor(private http: HttpClient, private tokenService: TokenService) {}

  private getHeaders(): HttpHeaders {
    const rawToken = this.tokenService.getToken();
    const token = rawToken ? rawToken.replace(/^"|"$/g, '') : '';
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  getAll(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/get`, { headers: this.getHeaders() });
  }

  addProducto(data: any): Observable<string> {
    const headers = this.getHeaders();
    console.log('Token enviado:', headers.get('Authorization'));
    return this.http.post<string>(`${this.apiUrl}/add`, data, { headers });
  }

  updateProducto(data: any): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/update`, data, { headers: this.getHeaders() });
  }

  deleteProducto(id: number): Observable<string> {
    return this.http.post<string>(
      `${this.apiUrl}/delete/${id}`,
      {},
      { headers: this.getHeaders() }
    );
  }

  getByCategory(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/getByCategory/${id}`, {
      headers: this.getHeaders(),
    });
  }

  getById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/getById/${id}`, { headers: this.getHeaders() });
  }

  updateStatus(id: number, status: string): Observable<any> {
    const token = this.tokenService.getToken();
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });

    const body = { id_producto: id, status };
    console.log('Body enviado:', body); // 👀 Para verificar
    return this.http.post(`${this.apiUrl}/updateStatus`, body, { headers, responseType: 'text' });
  }

  getProductos(): Observable<any[]> {
    return this.getAll();
  }
}
