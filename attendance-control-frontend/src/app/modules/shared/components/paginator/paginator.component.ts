import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';

export interface PageEvent {
  pageIndex: number;
  pageSize: number;
  totalItems: number;
}

@Component({
  selector: 'app-paginator',
  templateUrl: './paginator.component.html',
  styleUrls: ['./paginator.component.css']
})
export class PaginatorComponent implements OnChanges {
  @Input() totalItems: number = 0;
  @Input() pageSize: number = 10;
  @Input() currentPage: number = 1;
  @Input() maxVisiblePages: number = 5;

  @Output() pageChange = new EventEmitter<PageEvent>();

  totalPages: number = 0;
  visiblePages: number[] = [];
  startItem: number = 0;
  endItem: number = 0;

  ngOnChanges(changes: SimpleChanges): void {
    this.calculatePagination();
  }

  calculatePagination(): void {
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
    this.visiblePages = this.getVisiblePages();
    this.startItem = (this.currentPage - 1) * this.pageSize + 1;
    this.endItem = Math.min(this.currentPage * this.pageSize, this.totalItems);
  }

  getVisiblePages(): number[] {
    const pages: number[] = [];
    const half = Math.floor(this.maxVisiblePages / 2);

    let start = Math.max(1, this.currentPage - half);
    let end = Math.min(this.totalPages, start + this.maxVisiblePages - 1);

    if (end - start + 1 < this.maxVisiblePages) {
      start = Math.max(1, end - this.maxVisiblePages + 1);
    }

    // Agregar primera página si no está visible
    if (start > 1) {
      pages.push(1);
      if (start > 2) {
        pages.push(-1); // -1 representa "..."
      }
    }

    // Agregar páginas visibles
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    // Agregar última página si no está visible
    if (end < this.totalPages) {
      if (end < this.totalPages - 1) {
        pages.push(-1); // -1 representa "..."
      }
      pages.push(this.totalPages);
    }

    return pages;
  }

  goToPage(page: number): void {
    if (page === -1 || page === this.currentPage || page < 1 || page > this.totalPages) {
      return;
    }

    this.currentPage = page;
    this.calculatePagination();
    this.emitPageChange();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.goToPage(this.currentPage + 1);
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.goToPage(this.currentPage - 1);
    }
  }

  goToFirstPage(): void {
    this.goToPage(1);
  }

  goToLastPage(): void {
    this.goToPage(this.totalPages);
  }

  onPageSizeChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.pageSize = Number(select.value);
    this.currentPage = 1;
    this.calculatePagination();
    this.emitPageChange();
  }

  private emitPageChange(): void {
    this.pageChange.emit({
      pageIndex: this.currentPage,
      pageSize: this.pageSize,
      totalItems: this.totalItems
    });
  }
}