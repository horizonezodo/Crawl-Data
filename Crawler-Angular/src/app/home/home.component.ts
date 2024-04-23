import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../service/api.service';
import { TokenService } from '../service/token.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  loading: boolean = true;

  fullDataOrigin: any;
  fullData: any;
  views: any;
  amount: number = 5;
  condition: boolean = false;

  input: string = '';

  constructor(private router: Router,
    private apiService: ApiService,
    private tokenService: TokenService) { }

  ngOnInit(): void {
    this.onload();
  }

  onload(): void {
    this.loading = true;
    this.fullDataOrigin = [];
    let role = this.tokenService.getUserRole();
    let apiCall;

    if (role === 'Admin') {
      apiCall = this.apiService.getAll();
    } else {
      apiCall = this.apiService.getAllByUser();
    }
    apiCall.subscribe(response => {
      this.fullDataOrigin = response.flatMap(item => {
        return item.websiteDescription.map((description: any) => {
          return {
            websiteName: item.websiteName,
            websiteDescription: description,
            websiteId: item.websiteId
          };
        });
      }).sort((a, b) => {
        // Chuyển đổi chuỗi ngày thành đối tượng Date để so sánh
        const dateA = new Date(a.websiteDescription.date);
        const dateB = new Date(b.websiteDescription.date);
    
        // Sắp xếp theo thứ tự tăng dần
        return dateB.getTime() - dateA.getTime();
    });

      let elToAdd = this.fullDataOrigin.length % this.amount ? this.amount - (this.fullDataOrigin.length % this.amount) : 0;
      this.fullDataOrigin = [
        ...this.fullDataOrigin,
        ...Array.from({ length: elToAdd }, () => ({}))
      ]
      this.condition = true;
      this.fullData = this.fullDataOrigin;
      this.refresh(1);
      this.loading = false;
    }, () => {
      this.fullDataOrigin = Array.from({ length: this.amount }, () => ({}));
      this.fullData = this.fullDataOrigin;
      this.refresh(1);
      this.loading = false;
    });
  }

  refresh(curPage: number): void {
    let start = (curPage - 1) * this.amount;
    let end = start + this.amount;
    this.views = this.fullData.slice(start, end);
  }

  onInputChange(): void {
    if (!this.input) {
      this.fullData = this.fullDataOrigin;
      this.refresh(1);
    } else {
      this.fullData = this.fullDataOrigin.filter((item: { websiteDescription: { title: string | string[]; detail: string | string[]; square: string | string[]; price: string | string[]; date: string | any[]; }; }) => {
        if (item.websiteDescription && item.websiteDescription.title && item.websiteDescription.detail && item.websiteDescription.square && item.websiteDescription.price && item.websiteDescription.date) {
          return (
            item.websiteDescription.date.slice(0, 10).includes(this.input) ||
            item.websiteDescription.title.includes(this.input) ||
            item.websiteDescription.detail.includes(this.input) ||
            item.websiteDescription.square.includes(this.input) ||
            item.websiteDescription.price.includes(this.input)
          );
        }
        return false; // Nếu không thỏa mãn điều kiện, loại bỏ phần tử
      });
    }
    let elToAdd = this.fullData.length ? (this.fullData.length % this.amount ? this.amount - (this.fullData.length % this.amount) : 0) : this.amount;
    this.fullData = [
      ...this.fullData,
      ...Array.from({ length: elToAdd }, () => ({}))
    ]
    this.condition = this.fullData.length ? true : false;
    this.refresh(1);
  }

  navi(id: string): void {
    this.router.navigate(['/page', id]);
  }
}
