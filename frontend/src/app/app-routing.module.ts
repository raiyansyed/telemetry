import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './login/register.component';
import { AuthGuard, RoleGuard } from './core/guards';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'owner',
    loadChildren: () => import('./owner/owner.module').then(m => m.OwnerModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'OWNER' }
  },
  {
    path: 'customer',
    loadChildren: () => import('./customer/customer.module').then(m => m.CustomerModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { role: 'CUSTOMER' }
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
