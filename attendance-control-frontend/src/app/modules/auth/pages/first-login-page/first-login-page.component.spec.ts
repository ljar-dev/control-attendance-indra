import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FirstLoginPageComponent } from './first-login-page.component';

describe('FirstLoginPageComponent', () => {
  let component: FirstLoginPageComponent;
  let fixture: ComponentFixture<FirstLoginPageComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [FirstLoginPageComponent]
    });
    fixture = TestBed.createComponent(FirstLoginPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
