import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  private readonly TOKEN_KEY = 'auth_token';

  /**
   * Guarda el token JWT limpio en localStorage
   * tokenBackend: el token que recibes del backend, puede venir como JSON {"token":"..."}
   */
  saveToken(tokenBackend: string): void {
    if (!tokenBackend) return;

    // Si viene como JSON {"token":"..."} extraemos solo el token
    let cleanToken = tokenBackend;
    try {
      const parsed = JSON.parse(tokenBackend);
      if (parsed.token) cleanToken = parsed.token;
    } catch {
      // No es JSON, se guarda tal cual
    }

    // Eliminar posibles comillas sobrantes
    cleanToken = cleanToken.replace(/^"|"$/g, '');
    localStorage.setItem(this.TOKEN_KEY, cleanToken);
  }

  /**
   * Obtiene el token limpio
   */
  getToken(): string | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    return token ? token.replace(/^"|"$/g, '') : null;
  }

  /**
   * Elimina el token
   */
  removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }
}


