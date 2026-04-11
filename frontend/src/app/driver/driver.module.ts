import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { DriverRoutingModule } from './driver-routing.module';
import { DriverComponent } from './driver.component';
import { NgChartsModule } from 'ng2-charts';

@NgModule({
  declarations: [
    DriverComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    DriverRoutingModule,
    NgChartsModule
  ]
})
export class DriverModule { }
