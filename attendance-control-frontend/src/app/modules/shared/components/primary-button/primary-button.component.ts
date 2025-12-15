import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-primary-button',
  templateUrl: './primary-button.component.html',
  styleUrls: ['./primary-button.component.css']
})
export class PrimaryButtonComponent {
  @Input() label: string = 'Aceptar';
  @Input() icon: string = '';
  @Input() disabled: boolean = false;
  @Input() loading: boolean = false;
  @Input() loadingText: string = 'Procesando...';
  @Input() fullWidth: boolean = false;
}
