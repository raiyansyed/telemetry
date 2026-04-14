import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Alert } from '../core/api.service';

@Component({
  selector: 'app-alert-list',
  template: `
    <div *ngIf="!alerts || alerts.length === 0" class="text-center py-8 text-[var(--muted)]">
      No alerts found
    </div>
    <div *ngIf="alerts && alerts.length > 0">
      <div *ngIf="showMarkAllRead && hasUnread" class="flex justify-end mb-3">
        <button (click)="markAllRead.emit()"
          class="text-xs px-3 py-1.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-semibold">
          ✓ Mark All as Read
        </button>
      </div>
      <div class="flex flex-col gap-3 max-h-[400px] overflow-y-auto pr-1">
        <div
          *ngFor="let alert of alerts"
          class="flex items-center gap-4 p-4 rounded-lg border border-[var(--border)] bg-black/5 dark:bg-black/20"
          [ngClass]="{
            'border-l-4 border-l-red-500': alert.type === 'CRITICAL',
            'border-l-4 border-l-yellow-500': alert.type === 'WARNING',
            'border-l-4 border-l-blue-500 bg-blue-50/40': alert.type === 'INFO',
            'opacity-50': alert.isRead
          }"
        >
          <div class="text-2xl flex-shrink-0">
            {{ alert.isAssignmentRequest ? '📋' : alert.type === 'CRITICAL' ? '🔴' : alert.type === 'WARNING' ? '🟡' : '🔵' }}
          </div>
          <div class="flex-1 min-w-0">
            <div *ngIf="alert.isAssignmentRequest" class="text-xs font-bold uppercase tracking-wider text-blue-600 mb-0.5">Assignment Request</div>
            <div class="text-sm text-[var(--text)]">{{ alert.message }}</div>
            <div *ngIf="showPlate && alert.licensePlate && !alert.isAssignmentRequest" class="text-xs text-blue-400 mt-0.5">{{ alert.licensePlate }}</div>
            <div class="text-xs text-[var(--muted)] mt-0.5">{{ formatTime(alert.triggeredAt) }}</div>
          </div>
          <!-- Assignment request: approve/reject buttons -->
          <div *ngIf="alert.isAssignmentRequest" class="flex gap-1.5 flex-shrink-0">
            <button
              (click)="approve.emit(alert.id)"
              class="text-xs px-2.5 py-1.5 bg-green-600 text-white rounded-lg hover:bg-green-700 transition font-semibold whitespace-nowrap">
              ✓ Approve
            </button>
            <button
              (click)="reject.emit(alert.id)"
              class="text-xs px-2.5 py-1.5 bg-red-500 text-white rounded-lg hover:bg-red-600 transition font-semibold whitespace-nowrap">
              ✕ Reject
            </button>
          </div>
          <!-- Normal alert: mark read button -->
          <button
            *ngIf="!alert.isAssignmentRequest && !alert.isRead"
            (click)="markRead.emit(alert.id)"
            class="text-xs px-3 py-1.5 border border-[var(--border)] rounded text-[var(--muted)] hover:border-green-500 hover:text-green-500 transition whitespace-nowrap"
          >
            ✓ Read
          </button>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class AlertListComponent {
  @Input() alerts: Alert[] = [];
  @Input() showPlate: boolean = false;
  @Input() showMarkAllRead: boolean = false;
  @Output() markRead = new EventEmitter<number>();
  @Output() markAllRead = new EventEmitter<void>();
  @Output() approve = new EventEmitter<number>();
  @Output() reject = new EventEmitter<number>();

  formatTime(ts: string): string {
    if (!ts) return '';
    return new Date(ts).toLocaleString();
  }

  get hasUnread(): boolean {
    return this.alerts?.some(a => !a.isRead && !a.isAssignmentRequest) ?? false;
  }
}
