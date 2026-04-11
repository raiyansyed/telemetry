import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-gauge',
  template: `
    <div class="flex flex-col items-center min-w-[160px]">
      <div class="relative w-36 h-36 rounded-full flex items-center justify-center mb-2"
           [ngStyle]="{ 'background': conicGradient }">
        <div class="absolute inset-1 rounded-full bg-[var(--card)] flex flex-col items-center justify-center">
          <span class="text-3xl font-bold text-[var(--text)]">{{ displayValue }}</span>
          <span class="text-xs text-[var(--muted)]">{{ unit }}</span>
        </div>
      </div>
      <div class="text-sm font-medium text-[var(--muted)]">{{ label }}</div>
    </div>
  `,
  styles: []
})
export class GaugeComponent {
  @Input() value: number = 0;
  @Input() max: number = 200;
  @Input() unit: string = '';
  @Input() label: string = '';
  @Input() type: 'speed' | 'temp' = 'speed';

  get pct(): number {
    return Math.min((this.value / this.max) * 100, 100);
  }

  get displayValue(): string {
    return this.value?.toFixed(1) ?? '--';
  }

  get color(): string {
    return this.type === 'speed' ? '#40c4ff' : '#ffab40';
  }

  get conicGradient(): string {
    const deg = this.pct * 3.6;
    return `conic-gradient(${this.color} ${deg}deg, var(--border) 0)`;
  }
}
