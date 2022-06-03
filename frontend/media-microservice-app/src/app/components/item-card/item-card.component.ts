import { Component, Input, OnInit } from '@angular/core';
import { ProcessingItem } from 'src/app/models/ProcessingItem';

@Component({
  selector: 'app-item-card',
  templateUrl: './item-card.component.html',
  styleUrls: ['./item-card.component.css']
})
export class ItemCardComponent implements OnInit {
  @Input() item!: ProcessingItem;

  constructor() { }

  ngOnInit(): void {
  }

  buttonClick(): void{
    alert("am clicked");
    // download the file
  }
}
