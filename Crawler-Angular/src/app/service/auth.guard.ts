import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Observable, catchError, map, of } from 'rxjs';
import { TokenService } from './token.service';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private tokenService: TokenService,
    private authService: AuthService,
    private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {
    // Perform your authentication logic here
    // For example, check if the user is logged in

    const token = this.tokenService.getToken();
    const refreshToken = this.tokenService.getRefreshToken();
    const role = this.tokenService.getUserRole();
    const url = state.url;

    if (token && (role === 'Admin' || (url !== '/accounts' && url !== '/config'))) {
      // Gọi hàm kiểm tra tính hợp lệ của token và cập nhật giá trị của isTokenValid
      return this.authService.checkTokenValidity(token).pipe(
        map(data => {
          this.tokenService.setTokenValid(true);
          return true;
        }),
        catchError(error => {
          return this.authService.refreshToken(refreshToken).pipe(
            map(data => {
              this.tokenService.setToken(data.token);
              this.tokenService.setTokenValid(true);
              return true;
            }),
            catchError(refreshError => {
              localStorage.clear();
              this.router.navigate(['/']);
              return of(false);
            })
          );
        })
      );
    } else {
      localStorage.clear();
      this.router.navigate(['/']);
      return of(false);
    }
  }
}