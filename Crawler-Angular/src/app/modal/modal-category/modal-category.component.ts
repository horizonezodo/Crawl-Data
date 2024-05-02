import { Component, EventEmitter, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { AuthService } from '../../service/auth.service';
import { ApiService } from '../../service/api.service';
import { TransferService } from '../../service/transfer.service';

@Component({
  selector: 'app-modal-category',
  templateUrl: './modal-category.component.html'
})
export class ModalCategoryComponent {
  @Output() closeModal = new EventEmitter<void>();
  statusName: string = '';
  statusPath: string = '';
  idCategory: number = 0;
  formCategory: FormGroup = new FormGroup({
    categoryName: new FormControl(),
    path: new FormControl(),
  });
  arr: string[] = ['categoryName', 'path'];
  constructor(private authService: AuthService,
    private apiService: ApiService,
    private transferService: TransferService) { }

  ngOnInit(): void {
    this.transferService.sharedData$.subscribe((data) => {
      this.idCategory = data.idCategory;
    });
    if (!this.idCategory) return;
    this.apiService.getCategory(this.idCategory).subscribe(response => {
      this.formCategory.patchValue({
        'categoryName': response.categoryName,
        'path': response.path,
      });
    });
  }

  ngDoCheck(): void {
    this.arr.forEach(element => {
      const inputField = document.getElementById(element) as HTMLInputElement;
      const label = inputField.previousElementSibling as HTMLElement;

      if (this.formCategory.value[element] && label) {
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
    this.formatName();
    this.formatPath();
    if (this.statusName || this.statusPath) return;
    let api;
    if (this.idCategory) {
      api = this.apiService.updateCategory(this.formCategory.value, this.idCategory)
    } else {
      api = this.apiService.createCategory(this.formCategory.value)
    }
    api.subscribe(data => {
      this.onload();
      this.closeModal.emit();
    }, error => { })
  }

  formatName() {
    if (!this.formCategory.value.categoryName) {
      this.statusName = 'Category name is require';
    } else this.statusName = '';
  }

  formatPath() {
    const emailRegex = /^\/.+/i;
    if (!this.formCategory.value.path) {
      this.statusPath = 'Path is require';
    } else if (!emailRegex.test(this.formCategory.value.path)) {
      this.statusPath = 'Path format is not correct';
    } else this.statusPath = '';
  }

  back() {
    this.closeModal.emit();
  }

  onload() {
    this.transferService.callReload();
  }
}
