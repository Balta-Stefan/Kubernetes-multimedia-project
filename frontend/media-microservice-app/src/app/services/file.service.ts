import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { baseURL, jsonHeaders } from '../app.module';
import { ProcessingItem } from '../models/ProcessingItem';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private http: HttpClient) { }

  uploadFile(file: File): Observable<ProcessingItem>{
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);
    console.log("uploading file");

    return this.http.post<any>(`${baseURL}/submit`, formData);
  }
}
