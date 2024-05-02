import { Component, EventEmitter, Output } from '@angular/core';
import { ApiService } from '../../service/api.service';
import { TransferService } from '../../service/transfer.service';

@Component({
  selector: 'app-modal-delete',
  templateUrl: './modal-delete.component.html'
})
export class ModalDeleteComponent {
  id: number = 0;
  idAccount: number = 0;
  idCategory: number = 0;
  deleteFor: string = '';
  @Output() closeModal = new EventEmitter<void>();

  constructor(private apiService: ApiService,
    private transferService: TransferService) { }

  ngOnInit(): void {
    this.transferService.sharedData$.subscribe((data) => {
      this.deleteFor = data.deleteFor;
      this.id = data.id;
      this.idAccount = data.idSignup;
      this.idCategory = data.idCategory;
    });
  }

  yes() {
    if (this.id && this.deleteFor === 'config') {
      this.apiService.delete(this.id).subscribe(data => {
        this.onload();
        this.closeModal.emit();
      }, () => {
        // this.showModalFailed = true;
      });
    } else if (this.idAccount && this.deleteFor === 'account') {
      this.apiService.deleteAccount(this.idAccount).subscribe(response => {
        this.onload();
        this.closeModal.emit();
      }, () => {
        // this.showModalFailed = true;
      });
    } else if (this.idCategory && this.deleteFor === 'category') {
      this.apiService.deleteCategory(this.idCategory).subscribe(response => {
        this.onload();
        this.closeModal.emit();
        // window.location.reload();
      }, () => {
        // this.showModalFailed = true;
      });
    }
  }

  onload() {
    this.transferService.callReload();
  }

  no() {
    this.closeModal.emit();
  }
}