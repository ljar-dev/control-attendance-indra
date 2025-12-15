import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttendanceRecordPageComponent } from './attendance-record-page.component';

describe('AttendanceRecordPageComponent', () => {
  let component: AttendanceRecordPageComponent;
  let fixture: ComponentFixture<AttendanceRecordPageComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AttendanceRecordPageComponent]
    });
    fixture = TestBed.createComponent(AttendanceRecordPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
