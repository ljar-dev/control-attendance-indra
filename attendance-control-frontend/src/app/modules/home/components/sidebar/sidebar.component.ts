import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ConfirmDialogService } from 'src/app/modules/shared/services/confirm-dialog/confirm-dialog.service';
import { AuthService } from 'src/app/services/auth.service';

interface MenuItem {
  name: string;
  icon: string;
  router?: string[];
  query?: any;
  roles?: string[];
  badge?: number;
}

interface MenuSection {
  title?: string;
  items: MenuItem[];
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  isCollapsed = false;
  currentUser: any = null;
  userRoles: string[] = [];

  mainMenu: MenuSection[] = [];
  adminMenu: MenuSection[] = [];
  userMenu: MenuSection[] = [];

  constructor(
    private router: Router,
    private authService: AuthService,
    private confirmDialogService: ConfirmDialogService
  ) {}

  ngOnInit(): void {
    this.loadUserData();
    this.buildMenu();
  }

  /**
   * Carga datos del usuario autenticado
   */
  loadUserData(): void {
    //Ajusta esto según tu servicio de auth
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.userRoles = user?.roles || [];
    });
  }

  /**
   * Construye el menú según los roles del usuario
   */
  buildMenu(): void {
    // Menú principal (visible para todos)
    this.mainMenu = [
      {
        title: 'Principal',
        items: [
          {
            name: 'Marcar Asistencia',
            icon: 'pi pi-clock',
            router: ['/home/attendance-record']
          },
          {
            name: 'Mis Asistencias',
            icon: 'pi pi-list-check',
            router: ['home/attendance-report/personal']
          }
        ]
      }
    ];

    // Menú de administración (solo para admins)
    if (this.hasRole(['ROLE_ADMIN'])) {
      this.adminMenu = [
        {
          title: 'Administración',
          items: [
            {
              name: 'Empleados',
              icon: 'pi pi-users',
              router: ['home/employee/list']
            },
            {
              name: 'Asistencias',
              icon: 'pi pi-list',
              router: ['home/attendance-report']
            },
            {
              name: 'Reportes',
              icon: 'pi pi-chart-pie',
              router: ['home/attendance-report/chart']
            }
          ]
        }
      ];
    }

    this.userMenu = [
      {
        title: 'Usuario',
        items: [
          {
            name: 'Mi Perfil',
            icon: 'pi pi-user',
            router: ['home/profile']
          }
        ]
      }
    ];
  }

  /**
   * Verifica si el usuario tiene alguno de los roles especificados
   */
  hasRole(roles: string[]): boolean {
    return roles.some(role => this.userRoles.includes(role));
  }

  /**
   * Navega a una ruta
   */
  navigate(item: MenuItem): void {
    if (item.router) {
      this.router.navigate(item.router, { queryParams: item.query });
    }
  }

  /**
   * Verifica si una ruta está activa
   */
  isActive(router: string[]): boolean {
    return this.router.url === router.join('/');
  }

  /**
   * Colapsa/Expande el sidebar
   */
  toggleSidebar(): void {
    this.isCollapsed = !this.isCollapsed;
  }

  /**
   * Cerrar sesión
   */
  async logout(): Promise<void> {
    const confirmed = await this.confirmDialogService.confirm({
      header: 'Cerrar Sesión',
      message: '¿Estás seguro de que deseas cerrar sesión?',
      acceptLabel: 'Sí, cerrar sesión',
      rejectLabel: 'Cancelar'
    });

    if (confirmed) {
      this.authService.logout();
      this.router.navigate(['/auth/login']);
    }
  }
}
