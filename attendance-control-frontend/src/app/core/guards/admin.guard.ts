import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { AuthService } from 'src/app/services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.currentUser$.pipe(
    take(1),
    map(user => {
      // Verificar si el usuario existe y tiene el rol ROLE_ADMIN
      const isAdmin = user?.roles?.includes('ROLE_ADMIN') || false;

      if (isAdmin) {
        // Es admin, permitir acceso
        return true;
      }

      // No es admin, redirigir a página principal
      console.warn('Acceso denegado: Se requiere rol de administrador');
      router.navigate(['/home/attendance-record'], {
        queryParams: { error: 'unauthorized' }
      });
      return false;
    })
  );
};