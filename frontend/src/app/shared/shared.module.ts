import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GaugeComponent } from './gauge.component';
import { AlertListComponent } from './alert-list.component';

@NgModule({
  declarations: [
    GaugeComponent,
    AlertListComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    GaugeComponent,
    AlertListComponent
  ]
})
export class SharedModule { }
