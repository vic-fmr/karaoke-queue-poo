import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing'; 

import { ProfileComponent } from './profile'; 
describe('Profile', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
  ProfileComponent,
        RouterTestingModule // 3. Adicione aqui
      ]
    })
    .compileComponents();

  fixture = TestBed.createComponent(ProfileComponent);
  component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});