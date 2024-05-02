import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent {
  isLogin: boolean = true;
  constructor(private route: ActivatedRoute) { }

  ngOnInit(): void {
    const path = this.route.snapshot.url[0].path;
    if (path === 'changePassword') {
      this.isLogin = false;
    }
  }
}