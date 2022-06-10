import { HttpClient, HttpHeaders, HttpParams, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { baseURL, jsonHeaders } from '../app.module';
import { ProcessingItem } from '../models/ProcessingItem';
import { ProcessingRequest } from '../models/ProcessingRequest';
import { ProcessingRequestReply } from '../models/ProcessingRequestReply';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private http: HttpClient) { }

  requestPresignURLs(files: string[]): Observable<string[]>{
    return this.http.post<any>(`${baseURL}/presign-urls`, files);
  }

  uploadFile(url: string, file: File): Promise<any>{
    console.log("Uploaded file type is: " + file.type);
    console.log("Upload URL is: ");
    console.log(url);
    /*const formData: FormData = new FormData();
    const headers: HttpHeaders = new HttpHeaders("Content-Type: " + file.type);

    formData.append('file', file, file.name);
    console.log("uploading file");

    return this.http.put<any>(url, formData, {
      headers: headers
    });*/

    return fetch(url, {
      method: 'PUT',
      body: file
    });
  }

  listMyBucket(): Observable<ProcessingItem[]>{
    return this.http.get<any>(`${baseURL}/user/bucket`);
  }

  notifyUploadFinished(request: ProcessingRequest): Observable<ProcessingRequestReply[]>{
    return this.http.post<any>(`${baseURL}/upload-finished`, request);
  }

  deleteFile(file: string): Observable<any>{
    return this.http.delete<any>(`${baseURL}/pending/` + file);
  }

  stopProcessing(file: string, processingID: string): Observable<any>{
    let params: HttpParams = new HttpParams();
    params = params.append("file", file);
    params = params.append("processingID", processingID);

    return this.http.delete(`${baseURL}/processing`, {
      params: params
    });
  }
}
