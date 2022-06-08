import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Notification } from 'src/app/models/Notification';
import { FileService } from 'src/app/services/file.service';

@Component({
  selector: 'app-item-card',
  templateUrl: './item-card.component.html',
  styleUrls: ['./item-card.component.css']
})
export class ItemCardComponent implements OnInit {
  @Input() item!: Notification;
  @Output() fileRemoved: EventEmitter<Notification> = new EventEmitter<Notification>();

  constructor(private fileService: FileService) { }

  ngOnInit(): void {
  }

  download(): void{
    if(this.item.url){
      this.fileService.downloadFile(this.item.url).subscribe();
    }
  }

  removeFile(): void{
    this.fileRemoved.emit(this.item);
  }
}
