import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CustomerRoutingModule } from './customer-routing.module';
import { CustomerComponent } from './customer.component';
import { SharedModule } from '../shared/shared.module';
import { NgChartsModule } from 'ng2-charts';

@NgModule({
  declarations: [
    CustomerComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    CustomerRoutingModule,
    SharedModule,
    NgChartsModule
  ]
})
export class CustomerModule { }
