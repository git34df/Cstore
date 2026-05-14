import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/enviroment';

@Injectable({
  providedIn: 'root',
})
export class VentaService {
  private apiUrl = `${environment.apiUrl}/Venta`;

  constructor(private http: HttpClient) {}

  // GET /Venta/getVentas
  // Admin → todas las ventas | Usuario → solo las propias (el backend discrimina por JWT)
  getVentas(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/getVentas`);
  }

  // GET /Venta/getVenta/{id}
  getVentaById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/getVenta/${id}`);
  }

  // POST /Venta/registrar
  // Body: { detalle:[{productoId, cantidad, precioUnitario}], total,
  //         nombreCliente, emailCliente, telefonoCliente, metodoPago, tipoComprobante }
  registrarVenta(payload: {
    detalle: { productoId: number; cantidad: number; precioUnitario: number }[];
    total: number;
    nombreCliente: string;
    emailCliente: string;
    telefonoCliente: string;
    metodoPago: string;
    tipoComprobante?: string;
  }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/registrar`, payload);
  }

  // POST /Venta/anular/{id}
  anularVenta(id: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/anular/${id}`, {}, { responseType: 'text' as 'json' });
  }

  // POST /Venta/getPdf   Body: { uuid: "..." }
  getPdf(uuid: string): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/getPdf`, { uuid }, { responseType: 'blob' });
  }
}