import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

const API_URL = environment.apiUrl;

@Injectable({
  providedIn: 'root'
})
export class HistoryService {
  constructor(private http: HttpClient) { }

  getHistoryConfig(email: string, catId: number): Observable<any> {
    return this.http.get<any>(`${API_URL}/get-users-search-history?email=${email}&catId=${catId}`);
  }

  updateCategory(obj: any): Observable<any> {
    return this.http.put<any>(`${API_URL}/users-search-history`, obj);
  }
}