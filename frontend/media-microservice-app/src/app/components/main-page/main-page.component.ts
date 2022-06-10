import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ProcessingProgress } from 'src/app/models/ProcessingProgress';
import { FileService } from 'src/app/services/file.service';
import { StompService } from 'src/app/services/stomp.service';
import { Message } from '@stomp/stompjs';
import { Subject, Subscription } from 'rxjs';
import { Notification } from 'src/app/models/Notification';
import { ProcessingRequest } from 'src/app/models/ProcessingRequest';
import { ProcessingType } from 'src/app/models/ProcessingType';
import { ProcessingItem } from 'src/app/models/ProcessingItem';
import { ProcessingRequestReply } from 'src/app/models/ProcessingRequestReply';
import { SessionService } from 'src/app/services/session-service.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  styleUrls: ['./main-page.component.css']
})
export class MainPageComponent implements OnInit, OnDestroy {
  items: ProcessingItem[] = [];

  filesToUpload: File[] = [];
  presignedLinks: string[] | null = null;

  queueSubscription!: Subscription;

  extractAudio: boolean = false;
  targetResolutionWidth!: number;
  targetResolutionHeight!: number;

  newNotificationSubject: Subject<Notification> = new Subject<Notification>();
  processingRequestResponseSubject: Subject<ProcessingRequestReply[]> = new Subject<ProcessingRequestReply[]>();

  existingFilesMap: Map<string, boolean> = new Map<string, boolean>();

  stopUploadAndProcessing: boolean = false;

  @ViewChild('fileUploadInput') fileUploadInput!: ElementRef;

  constructor(private fileService: FileService, 
    private stompService: StompService,
    private sessionService: SessionService, 
    private router: Router) {

    this.fileService.listMyBucket().subscribe({
      error: (err: HttpErrorResponse) => {
        console.log("couldnt list my bucket");
      },
      next: (items: ProcessingItem[]) => {
        console.log("my bucket:");
        console.log(items);
        this.items = items;

        // add existing files to the map to ensure duplicates aren't added
        this.items.forEach(i => this.existingFilesMap.set(i.file, true));
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

        this.newNotificationSubject.next(notification);
      });
    });
    
  }

  private uploadFileToObjectStorage(index: number): void{
    if(this.stopUploadAndProcessing){
      return;
    }
    if(this.presignedLinks && this.presignedLinks.length > index){
      const itemToUpload: ProcessingItem = this.items[index];

      itemToUpload.notifications.forEach(n => n.progress = ProcessingProgress.UPLOADING);

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

        this.fileService.notifyUploadFinished(req).subscribe((replies: ProcessingRequestReply[]) => this.processingRequestResponseSubject.next(replies));

        itemToUpload.notifications.forEach(n => n.progress = ProcessingProgress.PENDING);
        this.uploadFileToObjectStorage(index + 1);
      }).catch((e) => {
        alert("Couldn't upload file to object storage");
        console.log(e);
        itemToUpload.notifications.forEach(n => n.progress = ProcessingProgress.FAILED);
      });
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
    this.filesToUpload.forEach(f => files.push(f.name));
    
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
  }

  newFilesChosen(event: Event): void{
    const target = event.target as HTMLInputElement;
    const fileList: FileList = target.files as FileList;
    for(let i = 0; i < fileList.length; i++){
      const tempFile: File = fileList.item(i)!;
      if(this.existingFilesMap.get(tempFile.name)){
        alert("You have already processed file " + tempFile.name);
        continue;
      }

      this.filesToUpload.push(tempFile);
      this.existingFilesMap.set(tempFile.name, true);

      const fileNotifications: Notification[] = [];
      if(this.extractAudio == true){
        const extractAudioItem: Notification = {
          processingID: null,
          fileName: tempFile.name,
          progress: null,
          url: null,
          type: ProcessingType.EXTRACT_AUDIO
        };
  
        fileNotifications.push(extractAudioItem);
      }
      if(this.targetResolutionWidth && this.targetResolutionHeight){
        const transcodeItem: Notification = {
          processingID: null,
          fileName: tempFile.name,
          progress: null,
          url: null,
          type: ProcessingType.TRANSCODE
        };
  
        fileNotifications.push(transcodeItem);
      }

      const processingItem: ProcessingItem = {
        file: tempFile.name,
        notifications: fileNotifications
      };

      this.items.unshift(processingItem);
    }
  }

  stopProcessing(): void{
    this.stopUploadAndProcessing = true;
    this.filesToUpload = [];

    this.items.forEach(item => {
      item.notifications.forEach((notification: Notification) => {
        if(notification.progress == ProcessingProgress.PENDING || notification.progress == ProcessingProgress.PROCESSING){
          this.fileService.stopProcessing(item.file, notification.processingID!).subscribe(() => notification.progress = ProcessingProgress.CANCELED);
        }
      });
    });

    this.stopUploadAndProcessing = false;
  }

  logout(): void{
    this.sessionService.logout().subscribe(() => this.router.navigateByUrl('/login'));
  }
}
