import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-justification-modal',
  templateUrl: './justification-modal.component.html',
  styleUrls: ['./justification-modal.component.css']
})
export class JustificationModalComponent implements OnInit, OnChanges {
  
  @Input() show = false;
  @Input() attendanceRecord: any = null;
  @Input() justification: any = null;
  @Input() isReadOnly = false;
  
  @Output() closeModal = new EventEmitter<void>();
  @Output() submitJustification = new EventEmitter<string>();
  
  justificationForm: FormGroup;
  submitting = false;

  constructor(private fb: FormBuilder) {
    this.justificationForm = this.fb.group({
      justificationText: ['', [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(1000)
      ]]
    });
  }

  ngOnInit(): void {
    this.loadJustificationText();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['justification'] || changes['show']) {
      this.loadJustificationText();
    }
  }

  loadJustificationText(): void {
    if (this.justification?.justificationText) {
      this.justificationForm.patchValue({
        justificationText: this.justification.justificationText
      });
    } else {
      this.justificationForm.reset();
    }
  }

  get justificationText() {
    return this.justificationForm.get('justificationText');
  }

  get isJustified(): boolean {
    return this.justification?.justificationText !== null && 
           this.justification?.justificationText !== undefined;
  }

  get characterCount(): number {
    return this.justificationText?.value?.length || 0;
  }

  onClose(): void {
    this.closeModal.emit();
    this.justificationForm.reset();
    this.submitting = false;
  }

  onSubmit(): void {
    if (this.justificationForm.valid && !this.submitting) {
      this.submitting = true;
      this.submitJustification.emit(this.justificationText?.value);
    }
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.onClose();
    }
  }
}