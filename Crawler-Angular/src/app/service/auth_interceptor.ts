import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class Auth_interceptor implements HttpInterceptor {
  constructor(private router: Router) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem("Token_Key");
    if (token) {
      request = request.clone({
        setHeaders: { Authorization: 'Bearer ' + token }
      });
    }
    return next.handle(request).pipe(
      catchError((res: HttpErrorResponse) => {
        if (res.error) {
          let errorCode;
          if (res.error.errorCode) errorCode = res.error.errorCode;
          else if (res.error.status) errorCode = res.error.status;
          if(errorCode === 401) {
            localStorage.clear();
            this.router.navigate(['/']);
          }
        }

        // Pass the error through to the next error handler
        return throwError(res);
      })
    );
  }
}