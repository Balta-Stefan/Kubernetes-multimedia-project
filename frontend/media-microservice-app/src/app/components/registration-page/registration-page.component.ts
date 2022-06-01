import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RegistrationService } from 'src/app/services/registration.service';

@Component({
  selector: 'app-registration-page',
  templateUrl: './registration-page.component.html',
  styleUrls: ['./registration-page.component.css']
})
export class RegistrationPageComponent implements OnInit {
  formData: FormGroup;

  registrationStatus: string = "";
  registrationIsSuccessful: boolean = true;

  firstNameError: string = "";
  lastNameError: string = "";
  usernameError: string = "";
  passwordError: string = "";
  emailError: string = "";

  constructor(private fb: FormBuilder, private registrationService: RegistrationService) {
    this.formData = fb.group({
      username: [null, [Validators.required, Validators.minLength(12)]],
      password: [null, [Validators.required, Validators.minLength(15)]],
      password2: [null, Validators.required],
      email: [null, [Validators.required, Validators.email]]
    });
  }

  checkUsernameValid(): boolean{
    const username = this.formData.get('username');

    if(!username?.value){
      return true;
    }

    if(username?.invalid){
      return false;
    }

    const pattern = /(.*[#@\\/]{1,}.*)/;
    if(username.value.match(pattern)){
      return false;
    }

    if(username.invalid){
      return false;
    }

    return true;
  }

  checkEmailValid(): boolean{
    const mail = this.formData.get('email');

    if(!mail?.value){
      return true;
    }

    if(mail.invalid){
      return false;
    }

    return true;
  }

  isPassword1Valid(): boolean{
    const pass1 = this.formData.get('password');

    if(!pass1?.value){
      return true;
    }
    if(pass1.invalid){
      return false;
    }

    const pass1Value: string = pass1.value;
    // check for minimum strength
    const smallLettersRegex = /(.*[a-z]{1,}.*)/;
    const capitalLettersRegex = /(.*[A-Z]{1,}.*)/;
    const numbersRegex = /(.*[0-9]{1,}.*)/;

    if(!pass1Value.match(smallLettersRegex)){
      return false;
    }
    if(!pass1Value.match(capitalLettersRegex)){
      return false;
    }
    if(!pass1Value.match(numbersRegex)){
      return false;
    }


    return true;
  }

  isPassword2Valid(): boolean{
    const pass1 = this.formData.get('password');
    const pass2 = this.formData.get('password2');

    if(!pass2?.value){
      return true;
    }
    if(pass1?.value != pass2?.value){
      return false;
    }
    return true;
  }

  ngOnInit(): void {
  }

  register(): void{
    let userData = JSON.parse(JSON.stringify(this.formData.value));

    delete userData.password2;

    this.registrationService.register(userData).subscribe({
      error: (response: any) => {
        if(response?.status == 409){
          this.registrationStatus = "User with specified username and/or email already exists..";
          this.registrationIsSuccessful = false;
        }
        else{
          this.registrationStatus = "Registration unsuccessful";
          this.registrationIsSuccessful = false;  
        }

        
        if(response?.error?.password){
          this.passwordError = response.error.password;
        }
        if(response?.error?.email){
          this.emailError = response.error.email;
        }
        if(response?.error?.username){
          this.usernameError = response.error.username;
        }

        console.log(response);
      },
      complete: () => {
        this.registrationStatus = "Registration successful";
        this.registrationIsSuccessful = true;
      }
    });
  }
}