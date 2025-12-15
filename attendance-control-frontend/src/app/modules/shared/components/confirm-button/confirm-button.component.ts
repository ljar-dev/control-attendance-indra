import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-confirm-button',
  templateUrl: './confirm-button.component.html',
  styleUrls: ['./confirm-button.component.css']
})
export class ConfirmButtonComponent {
  @Input() label: string = 'Aceptar';
  @Input() icon: string = '';
  @Input() disabled: boolean = false;
  @Input() loading: boolean = false;
  @Input() loadingText: string = 'Procesando...';
}
