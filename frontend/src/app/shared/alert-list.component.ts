import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Alert } from '../core/api.service';

@Component({
  selector: 'app-alert-list',
  template: `
    <div *ngIf="!alerts || alerts.length === 0" class="text-center py-8 text-[var(--muted)]">
      No alerts found
    </div>
    <div *ngIf="alerts && alerts.length > 0" class="flex flex-col gap-3 max-h-[400px] overflow-y-auto pr-1">
      <div
        *ngFor="let alert of alerts"
        class="flex items-center gap-4 p-4 rounded-lg border border-[var(--border)] bg-black/5 dark:bg-black/20"
        [ngClass]="{
          'border-l-4 border-l-red-500': alert.type === 'CRITICAL',
          'border-l-4 border-l-yellow-500': alert.type === 'WARNING',
          'border-l-4 border-l-blue-500': alert.type === 'INFO',
          'opacity-50': alert.isRead
        }"
      >
        <div class="text-2xl">
          {{ alert.type === 'CRITICAL' ? '🔴' : alert.type === 'WARNING' ? '🟡' : '🔵' }}
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-sm text-[var(--text)]">{{ alert.message }}</div>
          <div *ngIf="showPlate && alert.licensePlate" class="text-xs text-blue-400 mt-0.5">{{ alert.licensePlate }}</div>
          <div class="text-xs text-[var(--muted)] mt-0.5">{{ formatTime(alert.triggeredAt) }}</div>
        </div>
        <button
          *ngIf="!alert.isRead"
          (click)="markRead.emit(alert.id)"
          class="text-xs px-3 py-1.5 border border-[var(--border)] rounded text-[var(--muted)] hover:border-green-500 hover:text-green-500 transition whitespace-nowrap"
        >
          ✓ Read
        </button>
      </div>
    </div>
  `,
  styles: []
})
export class AlertListComponent {
  @Input() alerts: Alert[] = [];
  @Input() showPlate: boolean = false;
  @Output() markRead = new EventEmitter<number>();

  formatTime(ts: string): string {
    if (!ts) return '';
    return new Date(ts).toLocaleString();
  }
}
