import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { baseURL, jsonHeaders } from '../app.module';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private http: HttpClient) { }

  requestPresignURLs(files: string[]): Observable<string[]>{
    return this.http.post<any>(`${baseURL}/presign-urls`, files);
  }

  uploadFile(url: string, file: File): Observable<any>{
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);
    console.log("uploading file");

    return this.http.put<any>(url, formData);
  }

  listMyBucket(): Observable<string[]>{
    return this.http.get<any>(`${baseURL}/user/bucket`);
  }

  notifyUploadFinished(file: string): Observable<any>{
    return this.http.post<any>(`${baseURL}/upload-finished`, file);
  }
}
