import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JustificationModalComponent } from './justification-modal.component';

describe('JustificationModalComponent', () => {
  let component: JustificationModalComponent;
  let fixture: ComponentFixture<JustificationModalComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [JustificationModalComponent]
    });
    fixture = TestBed.createComponent(JustificationModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
