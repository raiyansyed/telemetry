/**
 * GaugeComponent - A reusable circular gauge that displays a numeric value with a visual arc.
 *
 * HOW IT WORKS:
 * - Uses CSS conic-gradient to create a circular progress indicator.
 * - The filled portion of the circle represents the current value as a percentage of max.
 * - The center shows the numeric value and unit label.
 *
 * USAGE IN HTML:
 *   <app-gauge [value]="85.5" [max]="200" unit="km/h" label="Speed" type="speed"></app-gauge>
 *
 * @Input() PROPERTIES:
 * - value: the current numeric value to display (e.g., 85.5)
 * - max: the maximum value (used to calculate the percentage; e.g., 200 for speed)
 * - unit: the unit label shown below the number (e.g., "km/h" or "°C")
 * - label: descriptive text shown below the gauge (e.g., "Speed" or "Engine Temp")
 * - type: determines the arc color ('speed' = blue, 'temp' = orange)
 *
 * WHAT IS @Input()?
 * - @Input() marks a property as receivable from a parent component.
 * - The parent passes data using square brackets: [value]="someVariable"
 * - This is how Angular components communicate: parent -> child via @Input.
 *
 * USED BY: Owner dashboard vehicle detail popup (speed and temperature gauges)
 */
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
