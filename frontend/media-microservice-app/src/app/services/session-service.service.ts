import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { baseURL } from '../app.module';
import { LoginDetails } from '../models/LoginDetails';
import { LoginRequest } from '../models/LoginRequest';

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  constructor(private http: HttpClient, private router: Router) { 
  }

  checkSessionStatus(): Observable<LoginDetails>{
    return this.http.get<any>(`${baseURL}/session/status`);
  }

  login(credentials: LoginRequest): Observable<LoginDetails>{
    return this.http.post<any>(`${baseURL}/session/login`, credentials);
  }

  logout(): Observable<any>{
    return this.http.post<any>(`${baseURL}/session/logout`, null);
  }
}
