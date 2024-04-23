import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { TokenService } from './token.service';
import { Observable } from 'rxjs';
import { JwtResponse } from '../model/jwt-response';
import { TransferService } from './transfer.service';

const API_URL = environment.apiUrl + '/auth';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient,
    private tokenService: TokenService,
    private transferService: TransferService,
    private router: Router) { }

  signInSuccess(data: any) {
    if (data.token) {
      this.tokenService.setToken(data.token);
      this.tokenService.setRefreshToken(data.refreshToken);
      this.tokenService.setTokenValid(true);
      this.tokenService.setUsername(data.username);
      this.tokenService.setUserImage(data.user_image);
      this.tokenService.setUserRole(data.user_role);
      this.transferService.setShowModal(false);
      this.transferService.setShowModalSignin(false);

      this.router.navigate(['/home']);
    }
  }

  loginEmail(obj: any): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${API_URL}/login`, obj);
  }

  registerEmail(obj: any): Observable<any> {
    return this.http.post(`${API_URL}/register`, obj);
  }

  logout(): Observable<any> {
    return this.http.post(`${API_URL}/signout`, { });
  }

  checkTokenValidity(token: string): Observable<boolean> {
    // Gọi đến API để kiểm tra tính hợp lệ của token
    return this.http.post<boolean>(`${API_URL}/check-Token`, { token });
  }

  refreshToken(refreshToken: string): Observable<any> {
    return this.http.post<any>(`${API_URL}/refreshtoken`, { refreshToken });
  }

  changePassword(obj: any): Observable<any> {
    return this.http.put(`${API_URL}/changePassword`, obj);
  }
}
