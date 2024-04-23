import { Component, EventEmitter, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { AuthService } from 'src/app/service/auth.service';

@Component({
  selector: 'app-modal-signin',
  templateUrl: './modal-signin.component.html',
  styleUrl: './modal-signin.component.css'
})
export class ModalSigninComponent {
  @Output() closeModal = new EventEmitter<void>();
  statusEmail: string = '';
  statusPassword: string = '';
  formSignin: FormGroup = new FormGroup({
    email: new FormControl(),
    password: new FormControl(),
  });
  arr: string[] = ['email', 'password'];
  constructor(private authService: AuthService) { }

  ngDoCheck(): void {
    this.arr.forEach(element => {
      const inputField = document.getElementById(element) as HTMLInputElement;
      const label = inputField.previousElementSibling as HTMLElement;

      if (this.formSignin.value[element] && label) {
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

  signin() {
    this.formatEmail();
    this.formatPassword();
    if (this.statusEmail || this.statusPassword) return;
    this.authService.loginEmail(this.formSignin.value).subscribe(data => {
      this.authService.signInSuccess(data);
      this.closeModal.emit();
    }, error => { })
  }

  formatEmail() {
    const emailRegex = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
    if (!this.formSignin.value.email) {
      this.statusEmail = 'Email is require';
    } else if (!emailRegex.test(this.formSignin.value.email)) {
      this.statusEmail = 'Email format is not correct';
    } else this.statusEmail = '';
  }

  formatPassword() {
    const passwordRegex = /^(?=.*[A-Z]).{8,}$/;
    if (!this.formSignin.value.password) {
      this.statusPassword = 'Password is require';
    } else if (!passwordRegex.test(this.formSignin.value.password)) {
      this.statusPassword = 'Minimum is 8 characters with at least 1 upcase';
    } else this.statusPassword = '';
  }

  toggleEye(event: any) {
    const target = event.target || event.srcElement;
    const inputField = target.closest('.relative').querySelector('.input-field');
    const eyeClosed = target.querySelector('.eye-closed');
    const eyeOpen = target.querySelector('.eye-open');
  
    if (inputField && eyeClosed && eyeOpen) {
      // Đảo ngược giá trị của thuộc tính 'type'
      inputField.type = (inputField.type === 'password') ? 'text' : 'password';
  
      // Đảo ngược hiển thị của biểu tượng mắt
      eyeClosed.style.display = (inputField.type === 'password') ? 'initial' : 'none';
      eyeOpen.style.display = (inputField.type === 'password') ? 'none' : 'initial';
    }
  }
}