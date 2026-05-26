import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { TokenService } from '../services/token.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private tokenService: TokenService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.tokenService.getToken();
    let clonedRequest = req;

    if (token) {
      clonedRequest = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(clonedRequest).pipe(
      catchError((error: HttpErrorResponse) => {

        if (error.status === 401) {
          // ✅ Solo desloguear si el token expiró/es inválido (Spring Security lo rechaza)
          // NO desloguear si es un 401 de permisos insuficientes (usuario sin rol admin)
          const token = this.tokenService.getToken();
          const url = error.url ?? '';

          const isAuthFailure =
            !token ||                              // sin token → expiró
            url.includes('/usuario/Login') ||      // fallo en el login mismo
            error.error?.message === 'JWT expired' // token expirado explícito
          ;

          if (isAuthFailure) {
            this.tokenService.removeToken();
            this.router.navigate(['/login']);
          }
          // Si tiene token válido pero no tiene permisos → se queda en la página,
          // el componente manejará el error (ej: no mostrar el dropdown de clientes)
        }

        if (error.status === 403) {
          // 403 = Spring Security rechazó el token completamente → sí desloguear
          this.tokenService.removeToken();
          this.router.navigate(['/login']);
        }

        return throwError(() => error);
      })
    );
  }
}