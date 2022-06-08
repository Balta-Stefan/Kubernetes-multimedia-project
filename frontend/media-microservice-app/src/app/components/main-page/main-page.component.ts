import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ProcessingProgress } from 'src/app/models/ProcessingProgress';
import { FileService } from 'src/app/services/file.service';
import { StompService } from 'src/app/services/stomp.service';
import { Message } from '@stomp/stompjs';
import { Subscription } from 'rxjs';
import { Notification } from 'src/app/models/Notification';
import { Resolution } from 'src/app/models/Resolution';
import { ProcessingRequest } from 'src/app/models/ProcessingRequest';

@Component({
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  styleUrls: ['./main-page.component.css']
})
export class MainPageComponent implements OnInit, OnDestroy {
  items: Notification[] = [];

  filesToUpload: File[] = [];
  presignedLinks: string[] | null = null;

  queueSubscription!: Subscription;

  extractAudio: boolean = false;
  targetResolutionWidth!: number;
  targetResolutionHeight!: number;

  processingStarted: boolean = false;

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
      this.items[index].progress = ProcessingProgress.UPLOADING;

      this.fileService.uploadFile(this.presignedLinks[index], this.filesToUpload[index])
      .then(() => {
        alert("Finished uploading the file: " + this.filesToUpload[index].name);

        const fileName: string = this.filesToUpload[index].name;

        const req: ProcessingRequest = {
          extractAudio: this.extractAudio,
          targetResolution: {
            width: this.targetResolutionWidth,
            height: this.targetResolutionHeight
          },
          file: fileName
        };

        this.fileService.notifyUploadFinished(req).subscribe(() => this.processingStarted = true);
        this.items[index].progress = ProcessingProgress.PENDING;
        this.uploadFileToObjectStorage(index + 1);
      }).catch((e) => {
        alert("Couldn't upload file to object storage");
        console.log(e);
        this.items[index].progress = ProcessingProgress.FAILED;
      });

      /*this.fileService.uploadFile(this.presignedLinks[index], this.newFiles?.item(index)!).subscribe({
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
      });*/
    }
    else{
      this.filesToUpload = [];
    }
  }

  uploadFiles(): void{
    if(this.items.length == 0){
      alert("You must select files first!");
      return;
    }
    else if(!this.extractAudio && !this.targetResolutionWidth && !this.targetResolutionHeight){
      alert("You must select an operation to perform!");
      return;
    }

    console.log("main page upload file");
    const files: string[] = [];
    for(let i = 0; i < this.filesToUpload.length; i++){
      files.push(this.filesToUpload[i].name!);
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

  newFilesChosen(event: Event): void{
    const target = event.target as HTMLInputElement;
    const fileList: FileList = target.files as FileList;
    for(let i = 0; i < fileList.length; i++){
      const tempFile: File = fileList.item(i)!;
      this.filesToUpload.push(tempFile);

      const newItem: Notification = {
        fileName: tempFile.name,
        progress: null,
        url: null
      };

      this.items.push(newItem);
    }
  }

  fileRemoved(item: Notification): void{
    if(item.progress != null){
      // the item has been uploaded, send a request to the server to delete it
      this.fileService.deleteFile(item.fileName).subscribe();
    }
    else{
      this.filesToUpload = this.filesToUpload.filter(i => i.name != item.fileName);
    }

    this.items = this.items.filter(i => i.fileName != item.fileName);
  }

  stopProcessing(): void{
    this.items.forEach(item => {
      this.fileService.stopProcessing(item.fileName).subscribe();
    });

    this.items = [];
  }
}
