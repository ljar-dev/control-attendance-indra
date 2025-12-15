import { Component, Input, Output, EventEmitter, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.css'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true
    }
  ]
})
export class SelectComponent implements ControlValueAccessor {
  @Input() label: string = '';
  @Input() placeholder: string = 'Seleccione una opción';
  @Input() options: any[] = [];
  @Input() optionLabel: string = 'label';
  @Input() optionValue: string = 'value';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() hasError: boolean = false;
  @Input() errorMessage: string = '';
  @Input() helperText: string = '';
  
  @Output() valueChange = new EventEmitter<any>();
  @Output() selectionChange = new EventEmitter<any>();

  value: any = '';

  // ControlValueAccessor methods
  private onChangeFn = (value: any) => {};
  private onTouchedFn = () => {};

  writeValue(value: any): void {
    this.value = value || '';
  }

  registerOnChange(fn: any): void {
    this.onChangeFn = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouchedFn = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  // Component methods
  getOptionLabel(option: any): string {
    if (typeof option === 'string' || typeof option === 'number') {
      return option.toString();
    }
    return option[this.optionLabel] || '';
  }

  getOptionValue(option: any): any {
    if (typeof option === 'string' || typeof option === 'number') {
      return option;
    }
    return option[this.optionValue];
  }

  onChange(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;
    const selectedValue = selectElement.value;
    
    this.value = selectedValue;
    this.onChangeFn(selectedValue);
    this.valueChange.emit(selectedValue);
    
    // Emitir el objeto completo seleccionado
    const selectedOption = this.options.find(
      option => this.getOptionValue(option) == selectedValue
    );
    this.selectionChange.emit(selectedOption);
  }

  onTouched(): void {
    this.onTouchedFn();
  }
}