import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  // URLs que NO necesitan token
  private readonly SKIP_TOKEN_URLS = [
    '/auth/login',
    '/users/register',
    '/v3/api-docs',
    '/swagger-ui'
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    
    // Verificar si la URL necesita token
    const shouldSkipToken = this.SKIP_TOKEN_URLS.some(url => 
      request.url.includes(url)
    );

    // Si no necesita token, enviar la petición sin modificar
    if (shouldSkipToken) {
      return next.handle(request);
    }

    // Obtener el token
    const token = this.authService.getToken();

    // Clonar la petición y agregar el header Authorization
    let clonedRequest = request;
    if (token) {
      clonedRequest = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    // Enviar la petición y manejar errores
    return next.handle(clonedRequest).pipe(
      catchError((error: HttpErrorResponse) => {
        return this.handleError(error);
      })
    );
  }

  /**
   * Maneja los errores HTTP
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    if (error.status === 401) {
      // Token inválido o expirado
      console.error('No autorizado - Token inválido o expirado');
      this.authService.logout();
      // Opcional: mostrar mensaje al usuario
    } else if (error.status === 403) {
      // Forbidden - Usuario no tiene permisos
      console.error('Acceso prohibido - Sin permisos suficientes');
      this.router.navigate(['/forbidden']);
    } else if (error.status === 0) {
      // Error de red o CORS
      console.error('Error de conexión - Backend no disponible');
    }

    return throwError(() => error);
  }
}