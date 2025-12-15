import { ApplicationRef, ComponentRef, createComponent, EnvironmentInjector, Injectable } from '@angular/core';
import { ConfirmDialogComponent } from '../../components/confirm-dialog/confirm-dialog.component';

export interface ConfirmDialogOptions {
  header?: string;
  message: string;
  acceptLabel?: string;
  rejectLabel?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ConfirmDialogService {

  private dialogRef: ComponentRef<ConfirmDialogComponent> | null = null;

  constructor(
    private appRef: ApplicationRef,
    private injector: EnvironmentInjector
  ) {}

  confirm(options: ConfirmDialogOptions): Promise<boolean> {
    return new Promise((resolve) => {
      // Si ya hay un diálogo abierto, rechazarlo primero
      if (this.dialogRef) {
        this.close();
      }

      // Crear el componente dinámicamente
      this.dialogRef = createComponent(ConfirmDialogComponent, {
        environmentInjector: this.injector
      });

      // Configurar las propiedades
      this.dialogRef.instance.header = options.header || 'Confirmación';
      this.dialogRef.instance.message = options.message;
      this.dialogRef.instance.acceptLabel = options.acceptLabel || 'Aceptar';
      this.dialogRef.instance.rejectLabel = options.rejectLabel || 'Cancelar';

      // Suscribirse a los eventos
      this.dialogRef.instance.onAccept.subscribe(() => {
        resolve(true);
        this.close();
      });

      this.dialogRef.instance.onReject.subscribe(() => {
        resolve(false);
        this.close();
      });

      // Adjuntar al DOM
      this.appRef.attachView(this.dialogRef.hostView);
      const domElem = (this.dialogRef.hostView as any).rootNodes[0] as HTMLElement;
      document.body.appendChild(domElem);
    });
  }

  private close() {
    if (this.dialogRef) {
      this.appRef.detachView(this.dialogRef.hostView);
      this.dialogRef.destroy();
      this.dialogRef = null;
    }
  }
}
