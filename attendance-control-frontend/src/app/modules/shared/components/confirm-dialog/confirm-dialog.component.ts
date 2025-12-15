import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent {
  @Input() header: string = 'Confirmación';
  @Input() message: string = '¿Está seguro de realizar esta acción?';
  @Input() acceptLabel: string = 'Aceptar';
  @Input() rejectLabel: string = 'Cancelar';
  
  @Output() onAccept = new EventEmitter<void>();
  @Output() onReject = new EventEmitter<void>();
  
  visible: boolean = false;

  ngOnInit() {
    setTimeout(() => this.visible = true, 10);
  }

  accept() {
    this.visible = false;
    setTimeout(() => {
      this.onAccept.emit();
    }, 300);
  }

  reject() {
    this.visible = false;
    setTimeout(() => {
      this.onReject.emit();
    }, 300);
  }

  onOverlayClick(event: MouseEvent) {
    // Cerrar solo si se hace click en el overlay, no en el diálogo
    if (event.target === event.currentTarget) {
      this.reject();
    }
  }
}
