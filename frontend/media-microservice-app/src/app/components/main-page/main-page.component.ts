import { HttpErrorResponse } from '@angular/common/http';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ProcessingItem } from 'src/app/models/ProcessingItem';
import { ProcessingProgress } from 'src/app/models/ProcessingProgress';
import { FileService } from 'src/app/services/file.service';

@Component({
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  styleUrls: ['./main-page.component.css']
})
export class MainPageComponent implements OnInit {
  items: ProcessingItem[] = [];

  newFiles: FileList | null = null;
  presignedLinks: string[] | null = null;

  @ViewChild('fileUploadInput') fileUploadInput!: ElementRef;

  constructor(private fileService: FileService) {
    /*let item1: ProcessingItem = {
      fileName: "file 1",
      itemID: 1,
      progress: ProcessingProgress.PROCESSING,
      uploadTimestamp: new Date()
    };
    let item2: ProcessingItem = {
      fileName: "file 2",
      itemID: 2,
      progress: ProcessingProgress.FAILED,
      uploadTimestamp: new Date()
    };

    this.items.push(item1);
    this.items.push(item2);*/
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

  ngOnInit(): void {
  }

  private uploadFileToObjectStorage(index: number): void{
    if(this.presignedLinks && this.presignedLinks.length > index){
      this.fileService.uploadFile(this.presignedLinks[index], this.newFiles?.item(index)!).subscribe({
        error: (err: HttpErrorResponse) => {
          alert("Couldn't upload file " + this.newFiles?.item(index)?.name);
        },
        complete: () => {
          alert("Finished uploading the file: " + this.newFiles?.item(index)?.name);
          this.fileService.notifyUploadFinished(this.newFiles?.item(index)?.name!).subscribe();
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
