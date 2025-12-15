import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-spinner-loader',
  templateUrl: './spinner-loader.component.html',
  styleUrls: ['./spinner-loader.component.css']
})
export class SpinnerLoaderComponent {
  @Input() show: boolean = false;
  @Input() message: string = 'Cargando...';
  @Input() fullscreen: boolean = false;
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
}
