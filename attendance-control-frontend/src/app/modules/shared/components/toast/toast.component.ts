import { Component, EventEmitter, Input, Output } from '@angular/core';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

@Component({
  selector: 'app-toast',
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.css']
})
export class ToastComponent {
  @Input() type: ToastType = 'info';
  @Input() title: string = '';
  @Input() message: string = '';
  @Input() duration: number = 3000;
  @Output() onClose = new EventEmitter<void>();
  
  visible: boolean = false;

  ngOnInit() {
    setTimeout(() => this.visible = true, 10);
    
    if (this.duration > 0) {
      setTimeout(() => this.close(), this.duration);
    }
  }

  getIcon(): string {
    const icons = {
      success: '✓',
      error: '✕',
      warning: '⚠',
      info: 'ℹ'
    };
    return icons[this.type];
  }

  close() {
    this.visible = false;
    setTimeout(() => {
      this.onClose.emit();
    }, 400);
  }
}
