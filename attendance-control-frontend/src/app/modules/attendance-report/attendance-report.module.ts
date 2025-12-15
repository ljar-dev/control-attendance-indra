import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AttendanceReportRoutingModule } from './attendance-report-routing.module';
import { AttendanceReportPageComponent } from './pages/attendance-report-page/attendance-report-page.component';
import { PersonalAttendanceReportComponent } from './pages/personal-attendance-report/personal-attendance-report.component';
import { SharedModule } from '../shared/shared.module';
import { JustificationModalComponent } from './components/justification-modal/justification-modal.component';
import { AttendanceChartReportComponent } from './pages/attendance-chart-report/attendance-chart-report.component';


@NgModule({
  declarations: [
    AttendanceReportPageComponent,
    PersonalAttendanceReportComponent,
    JustificationModalComponent,
    AttendanceChartReportComponent
  ],
  imports: [
    CommonModule,
    AttendanceReportRoutingModule,
    SharedModule
  ]
})
export class AttendanceReportModule { }
