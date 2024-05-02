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
      'idSignup': 0,
      'idCategory': 0,
      'showModal': false,
      'showModalConfig': false,
      'showModalDelete': false,
      'showModalCategory': false,
      'showModalSignup': false,
      'deleteFor': ''
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

  setIdCategory(i: number) {
    let val = this._sharedData.value;
    val.idCategory = i;
    this._sharedData.next(val);
  }

  setShowModal(boo: boolean) {
    if (!boo) {
      this.setShowModalConfig(boo);
      this.setShowModalDelete(boo);
      this.setShowModalSignup(boo);
      this.setShowModalCategory(boo);
    }
    let val = this._sharedData.value;
    val.showModal = boo;
    this._sharedData.next(val);
  }

  setShowModalConfig(boo: boolean) {
    let val = this._sharedData.value;
    val.showModalConfig = boo;
    this._sharedData.next(val);
  }

  setShowModalDelete(boo: boolean) {
    let val = this._sharedData.value;
    val.showModalDelete = boo;
    this._sharedData.next(val);
  }

  setShowModalCategory(boo: boolean) {
    let val = this._sharedData.value;
    val.showModalCategory = boo;
    this._sharedData.next(val);
  }

  setShowModalSignup(boo: boolean) {
    let val = this._sharedData.value;
    val.showModalSignup = boo;
    this._sharedData.next(val);
  }

  setDeleteFor(str: string) {
    let val = this._sharedData.value;
    val.deleteFor = str;
    this._sharedData.next(val);
  }
}
