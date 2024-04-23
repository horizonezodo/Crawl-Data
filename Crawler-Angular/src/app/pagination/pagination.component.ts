import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css'
})
export class PaginationComponent {
  // @ts-ignore
  @Input() fullData;
  // @ts-ignore
  @Input() amount;
  @Output() refresh = new EventEmitter<number>();

  curPage: number = 1;
  numOfPages: number = 1;
  pageNumbers: any[] = [];
  amountDisplay: number = 5; // 5 phần tử ở khoảng giữa - phải là số lẻ

  ngOnInit(): void {
    this.numOfPages = Math.ceil(this.fullData.length / this.amount);
    // this.pageNumbers = Array.from({ length: this.numOfPages < this.amountDisplay ? (this.numOfPages - 1) : this.amountDisplay }, (_, index) => index + 2);
    this.sub();
  }

  ngOnChanges(): void {
    this.numOfPages = Math.ceil(this.fullData.length / this.amount);
    // this.pageNumbers = Array.from({ length: this.numOfPages < this.amountDisplay ? (this.numOfPages - 1) : this.amountDisplay }, (_, index) => index + 2);
    this.sub();
  }

  sub(): void {
    const amountNum = this.amountDisplay - 1;
    const totalPages = this.numOfPages;
    let currentPage = this.curPage;

    // Tạo mảng số trang
    this.pageNumbers = [];

    // Đảm bảo không bị vượt quá trang hiện tại hoặc trang cuối cùng
    let startPage = Math.max(2, currentPage - (amountNum / 2));
    let endPage = Math.min(totalPages - 1, currentPage + (amountNum / 2));

    // Xử lý trường hợp đặc biệt khi trang hiện tại là một trong ba phần tử đầu hoặc cuối của mảng
    if (startPage === 2) {
      endPage = Math.min(totalPages - 1, startPage + amountNum);
    } else if (endPage === totalPages - 1) {
      startPage = Math.max(2, endPage - amountNum);
    }

    // Đảm bảo luôn có 5 phần tử ở khoảng giữa
    while (endPage - startPage < amountNum && (startPage > 2 || endPage < totalPages - 1)) {
      if (startPage > 2) {
        startPage--;
      } else {
        endPage++;
      }
    }

    // Thêm các trang vào mảng
    for (let i = startPage; i <= endPage; i++) {
      this.pageNumbers.push(i);
    }
  }

  firstPage() {
    this.curPage = 1;
    this.refresh.emit(this.curPage);

    this.sub();
  }

  prevPage() {
    this.curPage -= 1;
    this.refresh.emit(this.curPage);
    
    this.sub();
  }

  choosePage(num: number) {
    this.curPage = num;
    this.refresh.emit(this.curPage);
    
    this.sub();
  }

  nextPage() {
    this.curPage += 1;
    this.refresh.emit(this.curPage);
    
    this.sub();
  }

  lastPage() {
    this.curPage = this.numOfPages;
    this.refresh.emit(this.curPage);
    
    this.sub();
  }
}
