import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AttendanceRecordRoutingModule } from './attendance-record-routing.module';
import { AttendanceRecordPageComponent } from './pages/attendance-record-page/attendance-record-page.component';
import { SharedModule } from '../shared/shared.module';



@NgModule({
  declarations: [
    AttendanceRecordPageComponent
  ],
  imports: [
    CommonModule,
    AttendanceRecordRoutingModule,
    SharedModule
  ]
})
export class AttendanceRecordModule { }
