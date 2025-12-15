import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SpinnerLoaderService {

  private showSpinner = new BehaviorSubject<boolean>(false);
  private spinnerMessage = new BehaviorSubject<string>('Cargando...');

  showSpinner$ = this.showSpinner.asObservable();
  spinnerMessage$ = this.spinnerMessage.asObservable();

  show(message: string = 'Cargando...') {
    this.spinnerMessage.next(message);
    this.showSpinner.next(true);
  }

  hide() {
    this.showSpinner.next(false);
  }

  isShowing(): boolean {
    return this.showSpinner.value;
  }
}
