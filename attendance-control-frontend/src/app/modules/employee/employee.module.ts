import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { EmployeeRoutingModule } from './employee-routing.module';
import { EmployeeListComponent } from './pages/employee-list/employee-list.component';
import { EmployeeAddEditComponent } from './pages/employee-add-edit/employee-add-edit.component';
import { SharedModule } from '../shared/shared.module';


@NgModule({
  declarations: [
    EmployeeListComponent,
    EmployeeAddEditComponent
  ],
  imports: [
    CommonModule,
    EmployeeRoutingModule,
    SharedModule
  ]
})
export class EmployeeModule { }
