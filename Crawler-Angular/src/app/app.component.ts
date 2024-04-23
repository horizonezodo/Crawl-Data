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
  showModalSignin: boolean = false;
  showModalSignup: boolean = false;
  showModalNewPassword: boolean = false;

  constructor(private transferService: TransferService) {}

  ngOnInit(): void {
    this.transferService.sharedData$.subscribe((data) => {
      this.showModal = data.showModal;
      this.showModalConfig = data.showModalConfig;
      this.showModalSignin = data.showModalSignin;
      this.showModalSignup = data.showModalSignup;
      this.showModalNewPassword = data.showModalNewPassword;
    });
  }

  closeModal(): void {
    this.transferService.setShowModal(false);
    this.transferService.setShowModalConfig(false);
    this.transferService.setShowModalSignin(false);
    this.transferService.setShowModalSignup(false);
    this.showModalNewPassword = false;
  }

  handleClose(event: MouseEvent): void {
    if (!(event.target as HTMLElement).closest('.modal-content') && !this.showModalSignin && !this.showModalNewPassword) {
      this.transferService.setShowModal(false);
      this.transferService.setShowModalConfig(false);
      this.transferService.setShowModalSignin(false);
      this.transferService.setShowModalSignup(false);
      this.showModalNewPassword = false;
    }
  }
}
