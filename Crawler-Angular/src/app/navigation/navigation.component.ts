import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { TokenService } from '../service/token.service';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent {
  isTokenValid: boolean = false;
  count: number = 0;
  username: any;
  role: any;
  img: any;

  constructor(private authService: AuthService,
    private router: Router,
    private tokenService: TokenService) { }

  ngOnInit(): void {
    this.isTokenValid = this.tokenService.getTokenValid();
    if (this.isTokenValid) {
      this.username = this.tokenService.getUsername().toUpperCase();
      this.role = this.tokenService.getUserRole();
      let image = this.tokenService.getUserImage();
      this.img = image !== 'null' ? image : "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcThRSug_V2Rrhkaz0SHavzG-uqzh8M8fms_IzQH3rz5gMy9tyXZ";
    }
  }

  ngDoCheck(): void {
    this.isTokenValid = this.tokenService.getTokenValid();
    if (this.isTokenValid) {
      this.username = this.tokenService.getUsername().toUpperCase();
      this.role = this.tokenService.getUserRole();
      let image = this.tokenService.getUserImage();
      this.img = image !== 'null' ? image : "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcThRSug_V2Rrhkaz0SHavzG-uqzh8M8fms_IzQH3rz5gMy9tyXZ";
    }
  }

  logout() {
    this.authService.logout().subscribe(data => {
      localStorage.clear();
      this.router.navigate(['/']);
    }, () => { });
  }

  onClick(navi: string) {
    this.router.navigate(['/' + navi]);
  }

  toggleSetting() {
    let el = document.getElementById("nav-dropdown") as HTMLInputElement;
    if (this.count % 2 == 0) {
      el.style.display = 'block';
    } else {
      el.style.display = 'none';
    }
    this.count += 1
  }
}