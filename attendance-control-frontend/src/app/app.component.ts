import { Component } from '@angular/core';
import { SpinnerLoaderService } from './modules/shared/services/spinner-loader/spinner-loader.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'attendance-control-frontend';

  constructor(public spinnerService: SpinnerLoaderService){}
}
