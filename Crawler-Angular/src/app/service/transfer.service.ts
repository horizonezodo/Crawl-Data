import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TransferService {
  private functionCallSource = new Subject<void>();
  private _sharedData = new BehaviorSubject<any>(
    {
      'id': 0,
      'showModal': false,
      'showModalConfig': false,
      'showModalSignin': false,
      'idSignup': 0,
      'showModalSignup': false,
      'showModalNewPassword': false
    }
  );

  callReload$ = this.functionCallSource.asObservable();

  callReload() {
    this.functionCallSource.next();
  }

  sharedData$ = this._sharedData.asObservable();

  setId(i: number) {
    let val = this._sharedData.value;
    val.id = i;
    this._sharedData.next(val);
  }

  setIdSignup(i: number) {
    let val = this._sharedData.value;
    val.idSignup = i;
    this._sharedData.next(val);
  }

  setShowModal(boo: boolean) {
    let val = this._sharedData.value;
    val.showModal = boo;
    this._sharedData.next(val);
  }

  setShowModalConfig(boo: boolean) {
    let val = this._sharedData.value;
    val.showModalConfig = boo;
    this._sharedData.next(val);
  }

  setShowModalSignin(boo: boolean) {
    let val = this._sharedData.value;
    val.showModalSignin = boo;
    this._sharedData.next(val);
  }

  setShowModalSignup(boo: boolean) {
    let val = this._sharedData.value;
    val.showModalSignup = boo;
    this._sharedData.next(val);
  }

  setShowModalNewPassword(boo: boolean) {
    let val = this._sharedData.value;
    val.showModalNewPassword = boo;
    this._sharedData.next(val);
  }
}
