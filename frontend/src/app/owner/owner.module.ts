import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { OwnerRoutingModule } from './owner-routing.module';
import { OwnerComponent } from './owner.component';
import { NgChartsModule } from 'ng2-charts';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    OwnerComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    OwnerRoutingModule,
    NgChartsModule,
    SharedModule
  ]
})
export class OwnerModule { }
