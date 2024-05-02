import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { TokenService } from './token.service';

@Injectable()
export class Auth_interceptor implements HttpInterceptor {
  private isRefreshing = false;

  constructor(private router: Router,
    private tokenService: TokenService,
    private authService: AuthService
  ) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.tokenService.getToken();
    if (token && !request.url.includes('/auth')) {
      request = request.clone({
        setHeaders: { Authorization: 'Bearer ' + token }
      });
    }
    return next.handle(request).pipe(
      catchError((res: HttpErrorResponse) => {
        if (res.status === 401) {
          return this.handleUnauthorizedError(request, next);
        }
        return throwError(() => res);
      })
    );
  }

  private handleUnauthorizedError(request: HttpRequest<any>, next: HttpHandler) {
    if (!this.isRefreshing) {
      // this.isRefreshing = true;
      const refreshToken = this.tokenService.getRefreshToken();
      return this.authService.refreshToken(refreshToken).pipe(
        switchMap((res) => {
          this.isRefreshing = false;
          this.tokenService.setToken(res.token)
          this.tokenService.setRefreshToken(res.refreshToken)
          request = request.clone({
            setHeaders: { Authorization: 'Bearer ' + res.token }
          });
          return next.handle(request);
        }),
        catchError((error) => {
          this.isRefreshing = false;
          if (error.status == '403') {
            localStorage.clear();
            this.router.navigate(['/login']);
          }
          return throwError(() => error);
        })
      )
    }
    return next.handle(request);
  }
}