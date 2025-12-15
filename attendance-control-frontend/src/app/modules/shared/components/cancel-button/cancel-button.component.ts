import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-cancel-button',
  templateUrl: './cancel-button.component.html',
  styleUrls: ['./cancel-button.component.css']
})
export class CancelButtonComponent {
  @Input() label: string = 'Cancelar';
  @Input() icon: string = '';
  @Input() disabled: boolean = false;
}
