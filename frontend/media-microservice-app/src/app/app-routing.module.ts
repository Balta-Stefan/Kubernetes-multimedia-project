import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginPageComponent } from './components/login-page/login-page.component';
import { MainPageComponent } from './components/main-page/main-page.component';
import { RegistrationPageComponent } from './components/registration-page/registration-page.component';
import { SessionCheckComponent } from './components/session-check/session-check.component';

const routes: Routes = [
  {
    path: '',
    component: MainPageComponent
  },
  {
    path: 'login',
    component: LoginPageComponent
  },
  {
  path: 'session_check',
  component: SessionCheckComponent
  },
  {
    path: 'register',
    component: RegistrationPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
