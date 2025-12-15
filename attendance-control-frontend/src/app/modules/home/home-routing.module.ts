import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomePageComponent } from './pages/home-page/home-page.component';
import { EmployeeModule } from '../employee/employee.module';

const routes: Routes = [
  {
    path: '',
    component: HomePageComponent,
    children: [
      {
        path: 'profile',
        loadChildren: () => import('../profile/profile.module').then(m => m.ProfileModule)
      },
      {
        path: 'attendance-record',
        loadChildren: () => import('../attendance-record/attendance-record.module').then(m => m.AttendanceRecordModule)
      },
      {
        path: 'attendance-report',
        loadChildren: () => import('../attendance-report/attendance-report.module').then(m => m.AttendanceReportModule)
      },
      {
        path: 'employee',
        loadChildren: () => import('../employee/employee.module').then(m => m.EmployeeModule)
      },
      {
        path: '**',
        redirectTo: 'attendance-record'
      }
      // {
      //   path: 'favorites',
      //   loadChildren: () => import('../favorites/favorites.module').then(m => m.FavoritesModule)
      // },
      // {
      //   path: 'history',
      //   loadChildren: () => import('../history/history.module').then(m => m.HistoryModule)
      // },
      // {
      //   path: 'orders',
      //   loadChildren: () => import('../orders/orders.module').then(m => m.OrdersModule)
      // },
      // {
      //   path: '**',
      //   redirectTo: 'tracks'
      // }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class HomeRoutingModule { }
