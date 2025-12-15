import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EmployeeAddEditComponent } from './pages/employee-add-edit/employee-add-edit.component';
import { EmployeeListComponent } from './pages/employee-list/employee-list.component';
import { adminGuard } from 'src/app/core/guards/admin.guard';

const routes: Routes = [
  {
    path: 'list',
    canActivate: [adminGuard],
    component: EmployeeListComponent
  },
  {
    path: 'new',
    canActivate: [adminGuard],
    component: EmployeeAddEditComponent
  },
  {
    path: ':id',
    canActivate: [adminGuard],
    component: EmployeeAddEditComponent
  },
  {
    path: ':id/edit',
    canActivate: [adminGuard],
    component: EmployeeAddEditComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EmployeeRoutingModule { }
