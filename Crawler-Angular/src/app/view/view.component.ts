import { Component } from '@angular/core';
import { ApiService } from '../service/api.service';
import { ActivatedRoute, Params } from '@angular/router';
import { TokenService } from '../service/token.service';

@Component({
  selector: 'app-view',
  templateUrl: './view.component.html',
  styleUrl: './view.component.css'
})
export class ViewComponent {
  views: any;
  fullDataOrigin: any;
  fullData: any;
  amount: number = 5;
  condition: boolean = false;
  currentPage: number = 1;

  namePage: any;
  id: number = 0;

  loading: boolean = true;

  input: string = '';

  constructor(private apiService: ApiService,
    private route: ActivatedRoute,
    private tokenService: TokenService) { }

  ngOnInit(): void {
    this.route.params.subscribe((params: Params) => {
      this.id = parseInt(params['id']);
    });
    this.onload();
  }

  onload(): void {
    this.loading = true;
    this.fullDataOrigin = [];
    let role = this.tokenService.getUserRole();
    let apiCall;

    if (role === 'Admin') {
      apiCall = this.apiService.getView(this.id);
    } else {
      apiCall = this.apiService.getViewByUser(this.id);
    }
    apiCall.subscribe(response => {
      this.fullDataOrigin = response.map((item: any, index: number) => {
        return {
          index: index + 1,
          ...item
        };
      }).sort((a, b) => {
        // Chuyển đổi chuỗi ngày thành đối tượng Date để so sánh
        const dateA = new Date(a.date);
        const dateB = new Date(b.date);
    
        // Sắp xếp theo thứ tự tăng dần
        return dateB.getTime() - dateA.getTime();
    });

      let elToAdd = this.fullDataOrigin.length % this.amount ? this.amount - (this.fullDataOrigin.length % this.amount) : 0;
      this.fullDataOrigin = [
        ...this.fullDataOrigin,
        ...Array.from({ length: elToAdd - 1 }, () => ({}))
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
    this.apiService.getNamePage(this.id).subscribe(response => {
      this.namePage = response.name;
    });
  }

  refresh(curPage: number): void {
    let start = (curPage - 1) * this.amount;
    let end = start + this.amount;
    this.views = this.fullData.slice(start, end);

    // Riêng
    this.currentPage = curPage;
  }

  back() {
    window.history.back();
  }

  onInputChange(): void {
    if (!this.input) {
      this.fullData = this.fullDataOrigin;
      this.refresh(1);
    } else {
      this.fullData = this.fullDataOrigin.filter((item: { title: string | string[]; detail: string | string[]; square: string | string[]; price: string | string[]; date: string | any[]; }) => {
        if (item.title && item.detail && item.square && item.price && item.date) {
          return (
            item.date.slice(0, 10).includes(this.input) ||
            item.title.includes(this.input) ||
            item.detail.includes(this.input) ||
            item.square.includes(this.input) ||
            item.price.includes(this.input)
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
}
