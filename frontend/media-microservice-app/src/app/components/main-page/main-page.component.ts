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

  newFile: File | null = null;

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
  }

  ngOnInit(): void {
  }

  uploadFile(): void{
    console.log("main page upload file");
    this.fileService.uploadFile(this.newFile!).subscribe({
      next: (newFile: ProcessingItem) => {
        this.items.push(newFile);
        this.newFile = null;
        this.fileUploadInput.nativeElement.value = '';
        alert("File successfully uploaded.");
      },
      error: (err: HttpErrorResponse) => {
        alert("An error has occurred: " + err.status);
      }
    });
  }

  newFileChosen(event: Event): void{
    const target = event.target as HTMLInputElement;
    this.newFile = (target.files as FileList)[0];

    console.log(target.files as FileList);
  }
}
