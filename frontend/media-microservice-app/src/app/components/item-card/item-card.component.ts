import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { Notification } from 'src/app/models/Notification';
import { ProcessingItem } from 'src/app/models/ProcessingItem';
import { FileService } from 'src/app/services/file.service';

@Component({
  selector: 'app-item-card',
  templateUrl: './item-card.component.html',
  styleUrls: ['./item-card.component.css']
})
export class ItemCardComponent implements OnInit, OnDestroy {
  @Input() item!: ProcessingItem;
  @Input() newNotificationHandler!: Observable<Notification>;
  @Output() fileRemoved: EventEmitter<Notification> = new EventEmitter<Notification>();

  private notificationHandlerSubscription!: Subscription;

  constructor(private fileService: FileService) { }

  ngOnDestroy(): void {
    this.notificationHandlerSubscription.unsubscribe();
  }

  ngOnInit(): void {
   this.notificationHandlerSubscription = this.newNotificationHandler.subscribe(notification => {
    if(notification.fileName == this.item.file){
      // two cases are possible: the user is already logged in and a notification exists in item.notifications.Another is when user logs out and logs in while processing is happening.

      const notifications: Notification[] = this.item.notifications;
      for(let i = 0; i < notifications.length; i++){
        if(notifications[i].type == notification.type){
          notifications[i].url = notification.url;
          notifications[i].progress = notification.progress;
          return;
        }
      }

      // if the processing type doesn't exist, add it
      notifications.push(notification);
    }
   });
  }
}
