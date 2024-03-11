import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';


@Injectable()
export class TokenInterceptorInterceptor implements HttpInterceptor {
  

  constructor(private router:Router,
    @Inject(PLATFORM_ID) private platformId: Object) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (isPlatformBrowser(this.platformId)) {
    const token = localStorage.getItem('token');

    if(token){
      request = request.clone({
        setHeaders: {Authorization: `Bearer ${token}`}
      });
    }
  }
    return next.handle(request).pipe(
      catchError((err)=>{
        if(err instanceof HttpResponse){
          console.log(err.url);
          if(err.status === 401 || err.status === 403){
            if(this.router.url === '/'){}
            else{
              if (isPlatformBrowser(this.platformId)) {
              localStorage.clear();
              this.router.navigate(['/']);
            }
          }
        }
      }
        return throwError(err);
      })
    )
  }
}
