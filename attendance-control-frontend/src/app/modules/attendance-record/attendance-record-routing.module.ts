import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AttendanceRecordPageComponent } from './pages/attendance-record-page/attendance-record-page.component';

const routes: Routes = [
  {
    path: '',
    component: AttendanceRecordPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AttendanceRecordRoutingModule { }
