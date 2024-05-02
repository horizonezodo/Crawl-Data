import { Component, ElementRef, HostListener } from '@angular/core';
import { Subscription } from 'rxjs';
import { ApiService } from '../service/api.service';
import { TransferService } from '../service/transfer.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html'
})
export class CategoriesComponent {
  loading: boolean = false;
  fullData: any;
  amount: number = 10;

  totalPages: number = 0;
  totalElements: number = 0;
  currentPage: number = 1;

  visible = false;
  action: string = '';

  private subscription?: Subscription;

  constructor(private apiService: ApiService,
    private transferService: TransferService,
    private router: Router,
    private elementRef: ElementRef) { }

  ngOnInit(): void {
    this.onload();
    this.subscription = this.transferService.callReload$.subscribe(() => {
      this.currentPage = 1;
      this.onload();
    });
  }

  onload(): void {
    this.apiService.getCategories(this.currentPage - 1, this.amount).subscribe(response => {
      this.totalPages = response.totalPages;
      this.totalElements = response.totalElements;
      this.fullData = response.content;
    }, () => {
      this.fullData = Array.from({ length: this.amount }, () => ({}));
    });
  }

  open(): void {
    this.visible = true;
  }

  close(): void {
    this.visible = false;
  }

  handleSelected(): void {
    this.currentPage = 1;
    this.onload();
  }

  onClick(path: string) {
    this.router.navigate([path]);
  }

  refresh(curPage: number): void {
    this.currentPage = curPage;
    this.onload();
  }

  edit(id: number): void {
    this.transferService.setIdCategory(id);
    this.transferService.setShowModal(true);
    this.transferService.setShowModalCategory(true);
  }

  editMobile(id: number): void {
    this.transferService.setIdCategory(id);
    this.action = 'form';
    this.open();
  }

  create() {
    this.transferService.setIdCategory(0);
    this.transferService.setShowModal(true);
    this.transferService.setShowModalCategory(true);
  }

  createMobile(): void {
    this.transferService.setIdCategory(0);
    this.action = 'form';
    this.open();
  }

  delete(id: number) {
    this.transferService.setIdCategory(id);
    this.transferService.setShowModal(true);
    this.transferService.setShowModalDelete(true);
    this.transferService.setDeleteFor('category');
  }

  deleteMobile(id: number) {
    this.transferService.setIdCategory(id);
    this.transferService.setDeleteFor('category');
    this.action = 'delete';
    this.open();
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
      this.apiService.getCategories(this.currentPage - 1, this.amount).subscribe(response => {
        this.fullData = this.fullData.concat(response.content);
      }, () => { });

      scrollableDiv.scrollTo({
        top: currentScrollPosition,
        behavior: 'auto'
      });
    }
  }
}
