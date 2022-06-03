import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LoginDetails } from 'src/app/models/LoginDetails';
import { LoginRequest } from 'src/app/models/LoginRequest';
import { SessionService } from 'src/app/services/session-service.service';

@Component({
  selector: 'app-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.css']
})
export class LoginPageComponent implements OnInit {
  formData: FormGroup;

  loginMessage: string = "";

  constructor(private fb: FormBuilder, private sessionService: SessionService, private router: Router) { 
    this.formData = fb.group({
      username: [null, Validators.required],
      password: [null, Validators.required]
    });
  }

  ngOnInit(): void {
  }

  login(): void{
    const loginRequest: LoginRequest = {username: this.formData.value['username'], password: this.formData.value['password']};

    this.sessionService.login(loginRequest).subscribe({
      error: (err: any) => {
        console.log(err);
        this.loginMessage = "Login unsuccessful";
      },
      next: (receivedObject: LoginDetails) => {
        this.router.navigateByUrl('');
      }
    });
  }
}
