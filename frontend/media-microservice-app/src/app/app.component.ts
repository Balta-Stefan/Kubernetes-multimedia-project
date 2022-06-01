import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { LoginDetails } from './models/LoginDetails';
import { SessionService } from './services/session-service.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'media-microservice-app';

  constructor(sessionService: SessionService, private router: Router){
    this.router.navigateByUrl('/login');
    /*this.router.navigateByUrl('/session_check');

    sessionService.checkSessionStatus().subscribe({
      error: (err: any) => {
        this.router.navigateByUrl('/login');
      },
      next: (receivedValue: LoginDetails) => {
        this.router.navigateByUrl('');
      }
    });*/
  }
}
