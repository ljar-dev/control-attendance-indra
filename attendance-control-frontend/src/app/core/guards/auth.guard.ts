import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { AuthService } from 'src/app/services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.currentUser$.pipe(
    take(1),
    map(user => {
      if (user) {
        // Usuario autenticado, permitir acceso
        return true;
      }

      // No autenticado, redirigir al login
      console.warn('Usuario no autenticado. Redirigiendo al login...');
      router.navigate(['/auth/login'], {
        queryParams: { returnUrl: state.url } 
      });
      return false;
    })
  );
};