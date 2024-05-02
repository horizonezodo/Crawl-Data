import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

const API_URL = environment.apiUrl;

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private http: HttpClient) { }

  getBdsItem(id: number, page: number, size: number, key: string, sortBy: string): Observable<any> {
    return this.http.get<any>(`${API_URL}/search-with-id/${id}?page=${page}&size=${size}&keyword=${key}&sortBy=${sortBy}`);
  }

  getBdsAllItems(page: number, size: number, key: string, sortBy: string): Observable<any> {
    return this.http.get<any>(`${API_URL}/search?page=${page}&size=${size}&keyword=${key}&sortBy=${sortBy}`);
  }

  getAutoItem(id: number, page: number, size: number, key: string, sortBy: string): Observable<any> {
    return this.http.get<any>(`${API_URL}/search-car-with-id/${id}?page=${page}&size=${size}&keyword=${key}&sortBy=${sortBy}`);
  }

  getAutoAllItems(page: number, size: number, key: string, sortBy: string): Observable<any> {
    return this.http.get<any>(`${API_URL}/car-search?page=${page}&size=${size}&keyword=${key}&sortBy=${sortBy}`);
  }

  getStockAllItems(page: number, size: number, key: string, sortBy: string): Observable<any> {
    return this.http.get<any>(`${API_URL}/stock-search?page=${page}&size=${size}&keyword=${key}&sortBy=${sortBy}`);
  }

  // crawler
  crawlerBds(id: number): Observable<any> {
    return this.http.get<any>(`${API_URL}/getAllData/${id}`);
  }

  crawlerAuto(id: number): Observable<any> {
    return this.http.get<any>(`${API_URL}/crawlCarData/${id}`);
  }

  // category
  getAllCategories(): Observable<any> {
    return this.http.get<any>(`${API_URL}/category/all-category`);
  }

  getCategories(page: number, size: number): Observable<any> {
    return this.http.get<any>(`${API_URL}/category/all-category-page?page=${page}&size=${size}`);
  }

  getCategory(id: number): Observable<any> {
    return this.http.get<any>(`${API_URL}/category/get-category/${id}`);
  }

  createCategory(obj: any): Observable<any> {
    return this.http.post<any>(`${API_URL}/category/add-category`, obj);
  }

  updateCategory(obj: any, id: number): Observable<any> {
    return this.http.put<any>(`${API_URL}/category/update-category/${id}`, obj);
  }

  deleteCategory(id: number): Observable<any> {
    return this.http.delete<any>(`${API_URL}/category/delete-category/${id}`);
  }

  // configuration
  getPages(page: number, size: number): Observable<any> {
    return this.http.get<any>(`${API_URL}/getAll?page=${page}&size=${size}`);
  }

  getPage(id: number): Observable<any> {
    return this.http.get<any>(`${API_URL}/get/${id}`);
  }

  create(obj: any): Observable<any> {
    return this.http.post<any>(`${API_URL}/add-url`, obj);
  }

  update(obj: any, id: number): Observable<any> {
    return this.http.put<any>(`${API_URL}/update/${id}`, obj);
  }

  updateNoFile(obj: any, id: number): Observable<any> {
    return this.http.put<any>(`${API_URL}/update-no-file/${id}`, obj);
  }

  delete(id: number): Observable<any> {
    return this.http.delete<any>(`${API_URL}/delete/${id}`);
  }

  // accounts
  getAccounts(page: number, size: number): Observable<any> {
    return this.http.get<any>(`${API_URL}/list-user?page=${page}&size=${size}`);
  }

  getAccount(id: number): Observable<any> {
    return this.http.get<any>(`${API_URL}/user-info/${id}`);
  }

  editAccount(obj: any, id: number): Observable<any> {
    return this.http.put(`${API_URL}/user/${id}`, obj);
  }

  deleteAccount(id: number): Observable<any> {
    return this.http.delete<any>(`${API_URL}/delete-user/${id}`);
  }
}
