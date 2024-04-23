import { Component, EventEmitter, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup } from '@angular/forms';
import { AuthService } from 'src/app/service/auth.service';

@Component({
  selector: 'app-modal-new-password',
  templateUrl: './modal-new-password.component.html',
  styleUrl: './modal-new-password.component.css'
})
export class ModalNewPasswordComponent {
  @Output() closeModal = new EventEmitter<void>();
  email: string = '';
  password: string = '';
  statusPassword: string = '';
  statusConfirmPassword: string = '';
  formNewPassword: FormGroup = new FormGroup({
    password: new FormControl(),
    confirmPassword: new FormControl(),
  });
  arr: string[] = ['password', 'confirmPassword'];
  constructor(private authService: AuthService,
    private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.email = params['email'];
      this.password = params['password'];
    });
  }

  ngDoCheck(): void {
    this.arr.forEach(element => {
      const inputField = document.getElementById(element) as HTMLInputElement;
      const label = inputField.previousElementSibling as HTMLElement;

      if (this.formNewPassword.value[element] && label) {
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

  save() {
    this.formatPassword();
    this.formatConfirmPassword();
    if (this.statusPassword || this.statusConfirmPassword) return;
    let obj = {
      'email': this.email,
      'old_pass': this.password,
      'new_pass': this.formNewPassword.value.password,
    };
    this.authService.changePassword(obj).subscribe(data => {
      let objSignin = {
        'email': data.mail,
        'password': data.newPass,
      };
      this.authService.loginEmail(objSignin).subscribe(data => {
        this.authService.signInSuccess(data);
        this.closeModal.emit();
      })
    }, error => { })
  }

  formatPassword() {
    const passwordRegex = /^(?=.*[A-Z]).{8,}$/;
    if (!this.formNewPassword.value.password) {
      this.statusPassword = 'Password is require';
    } else if (!passwordRegex.test(this.formNewPassword.value.password)) {
      this.statusPassword = 'Minimum is 8 characters with at least 1 upcase';
    } else this.statusPassword = '';
  }

  formatConfirmPassword() {
    const passwordRegex = /^(?=.*[A-Z]).{8,}$/;
    if (!this.formNewPassword.value.confirmPassword) {
      this.statusConfirmPassword = 'Password is require';
    } else if (!passwordRegex.test(this.formNewPassword.value.confirmPassword)) {
      this.statusConfirmPassword = 'Minimum is 8 characters with at least 1 upcase';
    } else if (this.formNewPassword.value.newPass !== this.formNewPassword.value.confirmPass) {
      this.statusConfirmPassword = 'Confirm Password must be the same with Password';
    } else this.statusConfirmPassword = '';
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
