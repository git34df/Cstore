import { Injectable } from '@angular/core';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private tokenService: TokenService) {}

  getUserInfo(): any {
    const token = this.tokenService.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload;
    } catch (error) {
      console.error('Error decoding token', error);
      return null;
    }
  }

  getUserRole(): string | null {
    const user = this.getUserInfo();
    return user?.role || null;
  }
}
