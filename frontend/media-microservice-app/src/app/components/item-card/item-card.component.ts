import { Component, Input, OnInit } from '@angular/core';
import { Notification } from 'src/app/models/Notification';

@Component({
  selector: 'app-item-card',
  templateUrl: './item-card.component.html',
  styleUrls: ['./item-card.component.css']
})
export class ItemCardComponent implements OnInit {
  @Input() item!: Notification;

  constructor() { }

  ngOnInit(): void {
  }

  buttonClick(): void{
    alert("am clicked");
    // download the file
  }
}
