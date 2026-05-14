import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/enviroment';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  // Ruta base del backend
  private apiUrl = `${environment.apiUrl}/Categoria`;

  constructor(private http: HttpClient, private tokenService: TokenService) {}

  /**
   * Genera los headers con token JWT si existe
   */
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
  
  /**
   * Obtener todas las categorías
   */
  getAll(filterValue: boolean = false): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/get?filterValue=${filterValue}`, { headers });
  }

  /**
   * Agregar una nueva categoría
   */
  addCategoria(categoria: { nombre: string }): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.post(`${this.apiUrl}/add`, categoria, {
      headers,
      responseType: 'text',
    });
  }

  updateCategoria(categoria: { IdCategoria: number; nombre: string }): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.post(`${this.apiUrl}/update`, categoria, {
      headers,
      responseType: 'text',
    });
  }
}
