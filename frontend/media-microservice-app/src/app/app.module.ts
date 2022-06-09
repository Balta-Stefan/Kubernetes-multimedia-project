import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HttpHeaders } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SessionCheckComponent } from './components/session-check/session-check.component';
import { LoginPageComponent } from './components/login-page/login-page.component';
import { RegistrationPageComponent } from './components/registration-page/registration-page.component';
import { MainPageComponent } from './components/main-page/main-page.component';
import { ItemCardComponent } from './components/item-card/item-card.component';
import { StompService } from './services/stomp.service';
import { rxStompServiceFactory } from './rx-stomp-service-factory';

export const baseURL: string = "api/v1";//"http://localhost:8081/api/v1";
export const jsonHeaders: HttpHeaders = new HttpHeaders({
  'Accept': 'application/json', 
  'Content-Type': 'application/json'
});

@NgModule({
  declarations: [
    AppComponent,
    SessionCheckComponent,
    LoginPageComponent,
    RegistrationPageComponent,
    MainPageComponent,
    ItemCardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule
  ],
  providers: [
    {
      provide: StompService,
      useFactory: rxStompServiceFactory
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }