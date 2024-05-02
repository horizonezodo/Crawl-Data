import { Component, ElementRef, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { TokenService } from '../service/token.service';
import { ApiService } from '../service/api.service';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {
  count: number = 0;
  username: any;
  role: any;
  img: any;
  categoriesList: any;

  constructor(private authService: AuthService,
    private apiService: ApiService,
    private router: Router,
    private tokenService: TokenService,
    private elementRef: ElementRef) { }

  ngOnInit(): void {
    this.username = this.tokenService.getUsername().toUpperCase();
    this.role = this.tokenService.getUserRole();
    let image = this.tokenService.getUserImage();
    this.img = image !== 'null' ? image : "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcThRSug_V2Rrhkaz0SHavzG-uqzh8M8fms_IzQH3rz5gMy9tyXZ";
    this.apiService.getAllCategories().subscribe(response => {
      this.categoriesList = response;
    });
  }

  // ngDoCheck(): void {
  //   this.username = this.tokenService.getUsername().toUpperCase();
  //   this.role = this.tokenService.getUserRole();
  //   let image = this.tokenService.getUserImage();
  //   this.img = image !== 'null' ? image : "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcThRSug_V2Rrhkaz0SHavzG-uqzh8M8fms_IzQH3rz5gMy9tyXZ";
  // }

  logout() {
    this.authService.logout().subscribe(data => {
      localStorage.clear();
      this.router.navigate(['/login']);
    }, () => { });
  }

  onClick(navi: string) {
    this.router.navigate([navi]);
  }

  dropdownOpen: boolean = false;

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
  }

  @HostListener('document:click', ['$event'])
  @HostListener('document:touchmove', ['$event'])
  @HostListener('document:touchstart', ['$event'])
  @HostListener('document:touchend', ['$event'])
  @HostListener('document:mousewheel', ['$event'])
  @HostListener('document:wheel', ['$event'])
  onInteraction(event: Event) {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      // Nếu người dùng click ra ngoài dropdown, đóng dropdown
      this.dropdownOpen = false;
    }
  }
}