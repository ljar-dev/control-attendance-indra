import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmButtonComponent } from './components/confirm-button/confirm-button.component';
import { CancelButtonComponent } from './components/cancel-button/cancel-button.component';
import { ToastComponent } from './components/toast/toast.component';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';
import { SelectComponent } from './components/select/select.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PrimaryButtonComponent } from './components/primary-button/primary-button.component';
import { SpinnerLoaderComponent } from './components/spinner-loader/spinner-loader.component';
import { PaginatorComponent } from './components/paginator/paginator.component';
import { InputFieldComponent } from './components/input-field/input-field.component';



@NgModule({
  declarations: [
    ConfirmButtonComponent,
    CancelButtonComponent,
    ToastComponent,
    ConfirmDialogComponent,
    SelectComponent,
    PrimaryButtonComponent,
    SpinnerLoaderComponent,
    PaginatorComponent,
    InputFieldComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule
  ],
  exports: [
    CancelButtonComponent,
    ConfirmButtonComponent,
    SelectComponent,
    FormsModule,
    PrimaryButtonComponent,
    SpinnerLoaderComponent,
    ReactiveFormsModule,
    PaginatorComponent,
    InputFieldComponent
  ]
})
export class SharedModule { }
