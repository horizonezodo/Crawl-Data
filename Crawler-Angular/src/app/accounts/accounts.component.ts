import { Component, ElementRef, HostListener } from '@angular/core';
import { Subscription } from 'rxjs';
import { ApiService } from '../service/api.service';
import { TransferService } from '../service/transfer.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-accounts',
  templateUrl: './accounts.component.html'
})
export class AccountsComponent {
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
    this.apiService.getAccounts(this.currentPage - 1, this.amount).subscribe(response => {
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
  
  onClick(navi: string) {
    this.router.navigate(['/' + navi]);
  }

  refresh(curPage: number): void {
    this.currentPage = curPage;
    this.onload();
  }

  edit(id: string): void {
    this.transferService.setIdSignup(parseInt(id));
    this.transferService.setShowModal(true);
    this.transferService.setShowModalSignup(true);
  }

  editMobile(id: string): void {
    this.transferService.setIdSignup(parseInt(id));
    this.action = 'form';
    this.open();
  }

  create() {
    this.transferService.setIdSignup(0);
    this.transferService.setShowModal(true);
    this.transferService.setShowModalSignup(true);
  }

  createMobile(): void {
    this.transferService.setIdSignup(0);
    this.action = 'form';
    this.open();
  }

  delete(id: number) {
    this.transferService.setIdSignup(id);
    this.transferService.setShowModal(true);
    this.transferService.setShowModalDelete(true);
    this.transferService.setDeleteFor('account');
  }

  deleteMobile(id: number) {
    this.transferService.setIdSignup(id);
    this.transferService.setDeleteFor('account');
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
      this.apiService.getPages(this.currentPage - 1, this.amount).subscribe(response => {
        this.fullData = this.fullData.concat(response.content);
      }, () => { });

      scrollableDiv.scrollTo({
        top: currentScrollPosition,
        behavior: 'auto'
      });
    }
  }
}
