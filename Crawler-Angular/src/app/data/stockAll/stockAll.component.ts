import { Component, ElementRef, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../service/api.service';
import { TokenService } from '../../service/token.service';
import { HistoryService } from '../../service/history.service';

@Component({
  selector: 'app-stock-all',
  templateUrl: './stockAll.component.html'
})
export class StockAllComponent {
  loading: boolean = true;

  fullData: any;
  amount: number = 50;

  key: string = '';

  totalPages: number = 0;
  totalElements: number = 0;
  currentPage: number = 1;

  sortBy: string[] = [];
  colCode: boolean = false;
  colCompany: boolean = false;
  colCareer: boolean = false;
  colFloor: boolean = false;
  colDate: boolean = false;
  colPrice: boolean = false;

  visible = false;
  catName: string = '';
  catId: number = 3;
  categoriesList: any;
  email: string = '';

  constructor(private router: Router,
    private tokenService: TokenService,
    private apiService: ApiService,
    private historyService: HistoryService,
    private elementRef: ElementRef) { }

  ngOnInit(): void {
    this.apiService.getAllCategories().subscribe(response => {
      this.categoriesList = response;
    });
    this.email = this.tokenService.getEmail();
    this.historyService.getHistoryConfig(this.email, this.catId).subscribe(response => {
      if (response) {
        this.catName = response.categoryName;
        this.key = response.keyword;
        this.sortBy = response.sortBy.split(',');
        this.colDate = this.sortBy.includes('date');
        this.colPrice = this.sortBy.includes('price');
      } else {
        this.catName = this.router.url.split('/')[1];
      }
      this.onload();
    }, () => { });
  }

  onload(): void {
    this.loading = true;
    if (!this.sortBy.length) {
      this.colDate = true;
      this.sortBy.push('date');
    }
    this.apiService.getStockAllItems(this.currentPage - 1, this.amount, this.key, this.sortBy.join(",")).subscribe(response => {
      this.totalPages = response.totalPages;
      this.totalElements = response.totalElements;
      this.fullData = response.content;
      this.loading = false;
    }, () => {
      this.fullData = Array.from({ length: 5 }, () => ({}));
      this.loading = false;
    });
  }

  open(): void {
    this.visible = true;
  }

  close(): void {
    this.visible = false;
  }

  onClick(navi: string) {
    this.router.navigate([navi]);
  }

  refresh(curPage: number): void {
    this.currentPage = curPage;
    this.onload();
  }

  onInputChange(): void {
    this.currentPage = 1;
    this.onload();
    this.updateHistory();
  }

  handleSelected(): void {
    this.currentPage = 1;
    this.onload();
  }

  removeElementFromArray<T>(arr: T[], element: T): T[] {
    const index = arr.indexOf(element);
    if (index !== -1) {
      arr.splice(index, 1);
    }
    return arr;
  }

  sortCode(): void {
    if (!this.colCode) {
      this.colCode = true;
      this.sortBy.push('stockCode');
    } else {
      this.colCode = false;
      this.sortBy = this.removeElementFromArray(this.sortBy, 'stockCode');
    }
    this.currentPage = 1;
    this.onload();
    this.updateHistory();
  }

  sortCompany(): void {
    if (!this.colCompany) {
      this.colCompany = true;
      this.sortBy.push('companyName');
    } else {
      this.colCompany = false;
      this.sortBy = this.removeElementFromArray(this.sortBy, 'companyName');
    }
    this.currentPage = 1;
    this.onload();
    this.updateHistory();
  }

  sortCareer(): void {
    if (!this.colCareer) {
      this.colCareer = true;
      this.sortBy.push('career');
    } else {
      this.colCareer = false;
      this.sortBy = this.removeElementFromArray(this.sortBy, 'career');
    }
    this.currentPage = 1;
    this.onload();
    this.updateHistory();
  }

  sortFloor(): void {
    if (!this.colFloor) {
      this.colFloor = true;
      this.sortBy.push('floor');
    } else {
      this.colFloor = false;
      this.sortBy = this.removeElementFromArray(this.sortBy, 'floor');
    }
    this.currentPage = 1;
    this.onload();
    this.updateHistory();
  }

  sortDate(): void {
    if (!this.colDate) {
      this.colDate = true;
      this.sortBy.push('date');
    } else {
      this.colDate = false;
      this.sortBy = this.removeElementFromArray(this.sortBy, 'date');
    }
    this.currentPage = 1;
    this.onload();
    this.updateHistory();
  }

  sortPrice(): void {
    if (!this.colPrice) {
      this.colPrice = true;
      this.sortBy.push('price');
    } else {
      this.colPrice = false;
      this.sortBy = this.removeElementFromArray(this.sortBy, 'price');
    }
    this.currentPage = 1;
    this.onload();
    this.updateHistory();
  }

  @HostListener('window:scroll', ['$event'])
  onScroll(): void {
    const scrollableDiv = this.elementRef.nativeElement.querySelector('.scrollable-div');
    if (scrollableDiv.scrollTop + scrollableDiv.clientHeight >= scrollableDiv.scrollHeight) {
      // console.log('Bạn đã cuộn đến cuối cùng của thẻ div!');
      // Gọi hàm hoặc thực hiện hành động mong muốn ở đây
      const currentScrollPosition = scrollableDiv.scrollHeight;
      this.currentPage = this.currentPage + 1;
      // this.onload();
      if (!this.sortBy.length) {
        this.colDate = true;
        this.sortBy.push('date');
      }
      this.apiService.getStockAllItems(this.currentPage - 1, this.amount, this.key, this.sortBy.join(",")).subscribe(response => {
        this.fullData = this.fullData.concat(response.content);
      }, () => { });

      scrollableDiv.scrollTo({
        top: currentScrollPosition,
        behavior: 'auto'
      });
    }
  }

  chooseCategory(category: any): void {
    this.catId = category.id;
    this.onClick(category.path);
  }

  updateHistory(): void {
    const email = this.tokenService.getEmail();
    const obj = {
      "email": email,
      "categoryId": this.catId,
      "keyword": this.key,
      "sortBy": this.sortBy.join(',')
    }
    this.historyService.updateCategory(obj).subscribe(response => { }, () => { });
  }
}
