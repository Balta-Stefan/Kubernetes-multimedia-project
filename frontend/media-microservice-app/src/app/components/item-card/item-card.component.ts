import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { Notification } from 'src/app/models/Notification';
import { ProcessingItem } from 'src/app/models/ProcessingItem';
import { ProcessingProgress } from 'src/app/models/ProcessingProgress';
import { ProcessingRequestReply } from 'src/app/models/ProcessingRequestReply';
import { FileService } from 'src/app/services/file.service';

@Component({
  selector: 'app-item-card',
  templateUrl: './item-card.component.html',
  styleUrls: ['./item-card.component.css']
})
export class ItemCardComponent implements OnInit, OnDestroy {
  @Input() item!: ProcessingItem;
  @Input() newNotificationHandler!: Observable<Notification>;
  @Input() processingRequestResponseHandler!: Observable<ProcessingRequestReply[]>;

  private notificationHandlerSubscription!: Subscription;
  private processingRequestResponseSubscription!: Subscription;

  progressEnum = ProcessingProgress;

  displayDeleteButton: boolean = false;
  disableDeleteButtonFlag: boolean = false;

  constructor(private fileService: FileService) { }

  ngOnDestroy(): void {
    this.notificationHandlerSubscription.unsubscribe();
    this.processingRequestResponseSubscription.unsubscribe();
  }

  ngOnInit(): void {
    this.processingRequestResponseSubscription = this.processingRequestResponseHandler.subscribe((replies: ProcessingRequestReply[]) => {
      if(replies.length == 0 || replies[0].file != this.item.file){
        return;
      }

      for(let i = 0; i < replies.length; i++){
        const reply: ProcessingRequestReply = replies[i];

        for(let j = 0; j < this.item.notifications.length; j++){
          const notification: Notification = this.item.notifications[j];
          if(reply.operation == notification.type){
            notification.processingID = reply.processingID;
            break;
          }
        }
      }
    });

    this.notificationHandlerSubscription = this.newNotificationHandler.subscribe(notification => {
      if(notification.fileName == this.item.file){
        let found: boolean = false;
        // two cases are possible: the user is already logged in and a notification exists in item.notifications.Another is when user logs out and logs in while processing is happening.
        const notifications: Notification[] = this.item.notifications;
        for(let i = 0; i < notifications.length; i++){
          if(notifications[i].type == notification.type){
            notifications[i].url = notification.url;
            notifications[i].progress = notification.progress;
            found = true;
            break;
          }
        }
        // if the processing type doesn't exist, add it
        if(found == false){
          notifications.push(notification);
        }

        // if all the processings are successful, send a request to delete the uploaded file
        for(let i = 0; i < notifications.length; i++){
          if(notifications[i].progress != ProcessingProgress.FINISHED){
            return;
          }
        }

        this.fileService.deleteFile(this.item.file).subscribe();
      }
    });
  }
}
