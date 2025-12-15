import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendanceReportPageComponent } from './attendance-report-page.component';

describe('AttendanceReportPageComponent', () => {
  let component: AttendanceReportPageComponent;
  let fixture: ComponentFixture<AttendanceReportPageComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AttendanceReportPageComponent]
    });
    fixture = TestBed.createComponent(AttendanceReportPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
