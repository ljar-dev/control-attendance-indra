import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AttendanceReportPageComponent } from './pages/attendance-report-page/attendance-report-page.component';
import { PersonalAttendanceReportComponent } from './pages/personal-attendance-report/personal-attendance-report.component';
import { AttendanceChartReportComponent } from './pages/attendance-chart-report/attendance-chart-report.component';
import { adminGuard } from 'src/app/core/guards/admin.guard';

const routes: Routes = [
  {
    path: '',
    canActivate: [adminGuard],
    component: AttendanceReportPageComponent
  },
  {
    path: 'chart',
    canActivate: [adminGuard],
    component: AttendanceChartReportComponent
  },
  {
    path: 'personal',
    component: PersonalAttendanceReportComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AttendanceReportRoutingModule { }
