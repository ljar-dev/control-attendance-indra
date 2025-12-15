import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendanceChartReportComponent } from './attendance-chart-report.component';

describe('AttendanceChartReportComponent', () => {
  let component: AttendanceChartReportComponent;
  let fixture: ComponentFixture<AttendanceChartReportComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AttendanceChartReportComponent]
    });
    fixture = TestBed.createComponent(AttendanceChartReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
