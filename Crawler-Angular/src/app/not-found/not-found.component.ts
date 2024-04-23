import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TransferService } from '../service/transfer.service';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrl: './not-found.component.css'
})
export class NotFoundComponent {

  constructor(private route: ActivatedRoute,
    private transferService: TransferService) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.transferService.setShowModal(true);
      this.transferService.setShowModalNewPassword(true);
    });
  }
}
