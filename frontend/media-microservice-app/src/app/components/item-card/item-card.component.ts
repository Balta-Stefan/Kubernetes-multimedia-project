import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Notification } from 'src/app/models/Notification';

@Component({
  selector: 'app-item-card',
  templateUrl: './item-card.component.html',
  styleUrls: ['./item-card.component.css']
})
export class ItemCardComponent implements OnInit {
  @Input() item!: Notification;
  @Output() deleteItemEvent = new EventEmitter<Notification>();

  constructor() { }

  ngOnInit(): void {
  }

  download(): void{
    
  }
}
