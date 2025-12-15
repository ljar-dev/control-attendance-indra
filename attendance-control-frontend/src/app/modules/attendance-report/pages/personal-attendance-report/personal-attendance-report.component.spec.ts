import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonalAttendanceReportComponent } from './personal-attendance-report.component';

describe('PersonalAttendanceReportComponent', () => {
  let component: PersonalAttendanceReportComponent;
  let fixture: ComponentFixture<PersonalAttendanceReportComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PersonalAttendanceReportComponent]
    });
    fixture = TestBed.createComponent(PersonalAttendanceReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
