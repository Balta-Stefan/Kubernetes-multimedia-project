import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SessionCheckComponent } from './session-check.component';

describe('SessionCheckComponent', () => {
  let component: SessionCheckComponent;
  let fixture: ComponentFixture<SessionCheckComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SessionCheckComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SessionCheckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
