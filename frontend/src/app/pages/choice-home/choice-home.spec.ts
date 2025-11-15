import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChoiceHome } from './choice-home';

describe('ChoiceHome', () => {
  let component: ChoiceHome;
  let fixture: ComponentFixture<ChoiceHome>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChoiceHome]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChoiceHome);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
