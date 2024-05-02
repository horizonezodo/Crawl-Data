import { Component } from '@angular/core';
import { TransferService } from './service/transfer.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  showModal: boolean = false;
  showModalConfig: boolean = false;
  showModalDelete: boolean = false;
  showModalSignup: boolean = false;
  showModalNewPassword: boolean = false;
  showModalCategory: boolean = false;

  constructor(private transferService: TransferService) {}

  ngOnInit(): void {
    this.transferService.sharedData$.subscribe((data) => {
      this.showModal = data.showModal;
      this.showModalConfig = data.showModalConfig;
      this.showModalDelete = data.showModalDelete;
      this.showModalSignup = data.showModalSignup;
      this.showModalNewPassword = data.showModalNewPassword;
      this.showModalCategory = data.showModalCategory;
    });
  }

  closeModal(): void {
    this.transferService.setShowModal(false);
    this.transferService.setShowModalConfig(false);
    this.transferService.setShowModalDelete(false);
    this.transferService.setShowModalSignup(false);
    this.transferService.setShowModalCategory(false);
    this.showModalNewPassword = false;
  }

  handleClose(event: MouseEvent): void {
    if (!(event.target as HTMLElement).closest('.modal-content') && !this.showModalDelete && !this.showModalNewPassword) {
      this.transferService.setShowModal(false);
      this.transferService.setShowModalConfig(false);
      this.transferService.setShowModalDelete(false);
      this.transferService.setShowModalSignup(false);
      this.transferService.setShowModalCategory(false);
      this.showModalNewPassword = false;
    }
  }
}
