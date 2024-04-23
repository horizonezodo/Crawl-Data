import { Component, EventEmitter, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ApiService } from 'src/app/service/api.service';
import { AuthService } from 'src/app/service/auth.service';
import { TransferService } from 'src/app/service/transfer.service';

@Component({
  selector: 'app-modal-signup',
  templateUrl: './modal-signup.component.html',
  styleUrl: './modal-signup.component.css'
})
export class ModalSignupComponent {
  @Output() closeModal = new EventEmitter<void>();
  statusEmail: string = '';
  statusUsername: string = '';
  statusRole: string = '';
  idAccount: number = 0;
  formSignup: FormGroup = new FormGroup({
    email: new FormControl(),
    username: new FormControl(),
    user_role: new FormControl(),
  });
  arr: string[] = ['email', 'username', 'user_role'];
  constructor(private authService: AuthService,
    private apiService: ApiService,
    private transferService: TransferService) { }

  ngOnInit(): void {
    this.transferService.sharedData$.subscribe((data) => {
      this.idAccount = data.idSignup;
    });
    if (!this.idAccount) return;
    this.apiService.getAccount(this.idAccount).subscribe(response => {
      this.formSignup.patchValue({
        'email': response.email,
        'username': response.username,
        'user_role': response.user_role,
      });
    });
  }

  ngDoCheck(): void {
    this.arr.forEach(element => {
      const inputField = document.getElementById(element) as HTMLInputElement;
      const label = inputField.previousElementSibling as HTMLElement;

      if (this.formSignup.value[element] && label) {
        label.classList.add('input-label');
      }
    });
  }

  ngAfterViewInit(): void {
    this.arr.forEach(element => {
      const inputField = document.getElementById(element) as HTMLInputElement;
      const label = inputField.previousElementSibling as HTMLElement;

      inputField.addEventListener('focus', () => {
        if (label) {
          label.classList.add('input-label');
        }
      });

      inputField.addEventListener('blur', () => {
        if (inputField.value === '' && label) {
          label.classList.remove('input-label');
        }
      });
    });
  }

  signup() {
    this.formatEmail();
    this.formatUsername();
    this.formatRole();
    if (this.statusEmail || this.statusUsername || this.statusRole) return;
    this.authService.registerEmail(this.formSignup.value).subscribe(data => {
      this.closeModal.emit();
    }, error => { })
  }

  formatEmail() {
    const emailRegex = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
    if (!this.formSignup.value.email) {
      this.statusEmail = 'Email is require';
    } else if (!emailRegex.test(this.formSignup.value.email)) {
      this.statusEmail = 'Email format is not correct';
    } else this.statusEmail = '';
  }

  formatUsername() {
    if (!this.formSignup.value.username) {
      this.statusUsername = 'Username is require';
    } else this.statusUsername = '';
  }

  formatRole() {
    if (!this.formSignup.value.user_role) {
      this.statusRole = 'Role is require';
    } else this.statusRole = '';
  }

  back() {
    this.closeModal.emit();
  }
}