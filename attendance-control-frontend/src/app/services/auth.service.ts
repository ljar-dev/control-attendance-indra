import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { AuthControllerService, LoginRequestDto, TokenResponseDto } from '../generated/api';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';

interface User {
  username: string;
  roles: string[];
  employeeId?: number;
  employeeCode?: string;
  firstName?: string;
  lastName?: string;
  mustChangePassword?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly TOKEN_KEY = 'auth_token';
  private readonly TOKEN_EXPIRY_DAYS = 7;

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  // BehaviorSubject para el estado de autenticación
  public isAuthenticated$ = new BehaviorSubject<boolean>(false);

  constructor(
    private cookieService: CookieService,
    private authApiService: AuthControllerService,
    private router: Router,
    private http: HttpClient  // Agregar HttpClient
  ) {
    this.loadUserFromToken();
  }

  /**
   * Carga el usuario desde el token
   */
  private loadUserFromToken(): void {
    if (this.isAuthenticated()) {
      const user = this.buildUserFromToken();
      if (user) {
        this.currentUserSubject.next(user);
        this.isAuthenticated$.next(true);
      }
    }
  }

  /**
   * Construye el objeto User desde el token
   */
  private buildUserFromToken(): User | null {
    const payload = this.getTokenPayload();
    if (!payload) return null;

    return {
      username: payload.sub || '',
      roles: payload.roles || [],
      employeeId: payload.employeeId,
      employeeCode: payload.employeeCode,
      firstName: payload.firstName,
      lastName: payload.lastName,
      mustChangePassword: payload.mustChangePassword
    };
  }

  /**
   * Realiza el login y guarda el token en una cookie
   */
  login(credentials: LoginRequestDto): Observable<TokenResponseDto> {
    return this.authApiService.login(credentials).pipe(
      tap((response: TokenResponseDto) => {
        if (response.token) {
          this.setToken(response.token);
          
          // Actualizar usuario actual
          const user = this.buildUserFromToken();
          if (user) {
            this.currentUserSubject.next(user);
            this.isAuthenticated$.next(true);
          }
        }
      })
    );
  }

  /**
   * Guarda el token en una cookie segura
   */
  private setToken(token: string): void {
    const expires = new Date();
    expires.setDate(expires.getDate() + this.TOKEN_EXPIRY_DAYS);

    this.cookieService.set(
      this.TOKEN_KEY,
      token,
      {
        expires: expires,
        path: '/',
        secure: false,  // Cambiar a true en producción con HTTPS
        sameSite: 'Lax'
      }
    );
  }

  /**
   * Actualiza el token y el usuario actual
   * NUEVO MÉTODO
   */
  updateToken(token: string): void {
    this.setToken(token);
    
    // Actualizar usuario actual con el nuevo token
    const user = this.buildUserFromToken();
    if (user) {
      this.currentUserSubject.next(user);
      this.isAuthenticated$.next(true);
    }
  }

  /**
   * Cambia la contraseña en el first login
   * NUEVO MÉTODO
   */
  changePasswordFirstLogin(data: { newPassword: string }): Observable<TokenResponseDto> {
    return this.http.post<TokenResponseDto>('/api/auth/first-login/change-password', data).pipe(
      tap((response: TokenResponseDto) => {
        if (response.token) {
          this.updateToken(response.token);
        }
      })
    );
  }

  /**
   * Obtiene el token almacenado
   */
  getToken(): string | null {
    const token = this.cookieService.get(this.TOKEN_KEY);
    return token || null;
  }

  /**
   * Verifica si el usuario está autenticado
   */
  isAuthenticated(): boolean {
    console.log('Token actual:', this.getToken());
    return this.getToken() !== null;
  }

  /**
   * Cierra sesión eliminando el token
   */
  logout(): void {
    this.cookieService.delete(this.TOKEN_KEY, '/');
    
    // Limpiar usuario actual
    this.currentUserSubject.next(null);
    this.isAuthenticated$.next(false);
    
    this.router.navigate(['/login']);
  }

  /**
   * Decodifica el token JWT (sin validar firma)
   * SOLO para leer claims básicos
   */
  getTokenPayload(): any {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch (error) {
      console.error('Error al decodificar token:', error);
      return null;
    }
  }

  /**
   * Obtiene el usuario actual
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Obtiene el username del token
   */
  getCurrentUsername(): string | null {
    const payload = this.getTokenPayload();
    return payload?.sub || null;
  }

  /**
   * Obtiene los roles del token
   */
  getCurrentRoles(): string[] {
    const payload = this.getTokenPayload();
    return payload?.roles || [];
  }

  /**
   * Verifica si el usuario tiene un rol específico
   */
  hasRole(role: string): boolean {
    return this.getCurrentRoles().includes(role);
  }

  /**
   * Verifica si tiene alguno de los roles
   */
  hasAnyRole(roles: string[]): boolean {
    const userRoles = this.getCurrentRoles();
    return roles.some(role => userRoles.includes(role));
  }

  /**
   * Verifica si el token está expirado
   */
  isTokenExpired(): boolean {
    const payload = this.getTokenPayload();
    if (!payload || !payload.exp) return true;

    const expirationDate = new Date(payload.exp * 1000);
    return expirationDate < new Date();
  }

  /**
   * Verifica si el usuario debe cambiar su contraseña
   * NUEVO MÉTODO
   */
  mustChangePassword(): boolean {
    const user = this.getCurrentUser();
    return user?.mustChangePassword ?? false;
  }
}