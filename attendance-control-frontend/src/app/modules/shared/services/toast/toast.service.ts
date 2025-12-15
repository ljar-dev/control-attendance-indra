import { ApplicationRef, ComponentRef, createComponent, EnvironmentInjector, Injectable } from '@angular/core';
import { ToastComponent, ToastType } from '../../components/toast/toast.component';

@Injectable({
  providedIn: 'root'
})
export class ToastService {

  private toastRefs: ComponentRef<ToastComponent>[] = [];

  constructor(
    private appRef: ApplicationRef,
    private injector: EnvironmentInjector
  ) {}

  show(type: ToastType, title: string, message: string, duration: number = 3000) {
    // Crear el componente dinámicamente
    const componentRef = createComponent(ToastComponent, {
      environmentInjector: this.injector
    });

    // Configurar las propiedades
    componentRef.instance.type = type;
    componentRef.instance.title = title;
    componentRef.instance.message = message;
    componentRef.instance.duration = duration;

    // Adjuntar al DOM
    this.appRef.attachView(componentRef.hostView);
    const domElem = (componentRef.hostView as any).rootNodes[0] as HTMLElement;
    document.body.appendChild(domElem);

    // Guardar referencia
    this.toastRefs.push(componentRef);

    // Remover después de la duración + tiempo de animación
    setTimeout(() => {
      this.removeToast(componentRef);
    }, duration + 500);
  }

  success(title: string, message: string, duration?: number) {
    this.show('success', title, message, duration);
  }

  error(title: string, message: string, duration?: number) {
    this.show('error', title, message, duration);
  }

  warning(title: string, message: string, duration?: number) {
    this.show('warning', title, message, duration);
  }

  info(title: string, message: string, duration?: number) {
    this.show('info', title, message, duration);
  }

  private removeToast(componentRef: ComponentRef<ToastComponent>) {
    const index = this.toastRefs.indexOf(componentRef);
    if (index > -1) {
      this.toastRefs.splice(index, 1);
      this.appRef.detachView(componentRef.hostView);
      componentRef.destroy();
    }
  }
}
