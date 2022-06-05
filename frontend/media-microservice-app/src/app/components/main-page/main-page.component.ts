import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ProcessingProgress } from 'src/app/models/ProcessingProgress';
import { FileService } from 'src/app/services/file.service';
import { StompService } from 'src/app/services/stomp.service';
import { Message } from '@stomp/stompjs';
import { Subscription } from 'rxjs';
import { Notification } from 'src/app/models/Notification';

@Component({
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  styleUrls: ['./main-page.component.css']
})
export class MainPageComponent implements OnInit, OnDestroy {
  items: Notification[] = [];

  newFiles: FileList | null = null;
  presignedLinks: string[] | null = null;

  queueSubscription!: Subscription;

  @ViewChild('fileUploadInput') fileUploadInput!: ElementRef;

  constructor(private fileService: FileService, private stompService: StompService) {
    this.fileService.listMyBucket().subscribe({
      error: (err: HttpErrorResponse) => {
        console.log("couldnt list my bucket");
      },
      next: (items: string[]) => {
        console.log("my bucket:");
        console.log(items);
      }
    });
  }
  ngOnDestroy(): void {
    this.queueSubscription.unsubscribe();
  }

  ngOnInit(): void {
    this.stompService.connected$.subscribe(val => {
      console.log("inside connected block, val=");
      console.log(val);
      console.log("Inside connected block, connected=" + this.stompService.connected());
      this.queueSubscription = this.stompService.watch("/topic/notifications").subscribe((msg: Message) => {
        console.log("received a message: ");
        console.log(msg);
        console.log("the body is:");

        const notification: Notification = JSON.parse(msg.body);
        console.log(notification);
        console.log("==================");

        for(let i = 0; i < this.items.length; i++){
          const item: Notification = this.items[i];
          if(item.fileName == notification.fileName){
            item.progress = notification.progress;
            item.url = notification.url;
          }
        }
      });
    });
    
  }

  private uploadFileToObjectStorage(index: number): void{
    if(this.presignedLinks && this.presignedLinks.length > index){
      const tmpItem: Notification = {
        progress: ProcessingProgress.UPLOADING,
        fileName: this.newFiles?.item(index)?.name!,
        url: null
      };
      this.items.push(tmpItem);

      this.fileService.uploadFile(this.presignedLinks[index], this.newFiles?.item(index)!).subscribe({
        error: (err: HttpErrorResponse) => {
          alert("Couldn't upload file " + this.newFiles?.item(index)?.name);
          this.items[index].progress = ProcessingProgress.FAILED;
        },
        complete: () => {
          alert("Finished uploading the file: " + this.newFiles?.item(index)?.name);
          this.fileService.notifyUploadFinished(this.newFiles?.item(index)?.name!).subscribe();
          this.items[index].progress = ProcessingProgress.PENDING;
          this.uploadFileToObjectStorage(index + 1);
        }
      });
    }
  }

  uploadFiles(): void{
    console.log("main page upload file");
    const files: string[] = [];
    if(this.newFiles){
      for(let i = 0; i < this.newFiles.length; i++){
        files.push(this.newFiles.item(i)?.name!);
      }
    }


    this.fileService.requestPresignURLs(files).subscribe({
      next: (links: string[]) => {
        console.log("received presigned URLs: ");
        console.log(links);
  
        this.presignedLinks = links;
  
        this.uploadFileToObjectStorage(0);
      },
      error: (err: HttpErrorResponse) => {
        alert("Couldn't get presigned URLs");
      }
    });
    /*this.fileService.uploadFile(this.newFiles!).subscribe({
      next: (newFile: ProcessingItem) => {
        this.items.push(newFile);
        this.newFiles = null;
        this.fileUploadInput.nativeElement.value = '';
        alert("File successfully uploaded.");
      },
      error: (err: HttpErrorResponse) => {
        alert("An error has occurred: " + err.status);
      }
    });*/
  }

  newFileChosen(event: Event): void{
    const target = event.target as HTMLInputElement;
    this.newFiles = target.files as FileList;
  }
}
