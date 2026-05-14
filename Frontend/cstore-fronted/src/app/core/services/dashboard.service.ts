import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/enviroment';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {

  private apiUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient, private tokenService: TokenService) {}

  private get headers() {
    const rawToken = this.tokenService.getToken();
    const token = rawToken ? rawToken.replace(/^"|"$/g, '') : null;
    return token
      ? new HttpHeaders().set('Authorization', `Bearer ${token}`)
      : undefined;
  }

  // Endpoints existentes
  getMetrics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/Dashboard/Detalles`, {
      headers: this.headers
    });
  }

  
}





