import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './service/auth.guard';
import { LandingComponent } from './landing/landing.component';
import { AccountsComponent } from './accounts/accounts.component';
import { BdsAllComponent } from './data/bdsAll/bdsAll.component';
import { BdsDetailComponent } from './data/bdsDetail/bdsDetail.component';
import { ConfigurationComponent } from './configuration/configuration.component';
import { AutoAllComponent } from './data/autoAll/autoAll.component';
import { AutoDetailComponent } from './data/autoDetail/autoDetail.component';
import { CategoriesComponent } from './categories/categories.component';
import { StockAllComponent } from './data/stockAll/stockAll.component';

const routes: Routes = [
  {
    path: 'config',
    component: ConfigurationComponent
  },
  {
    path: 'accounts',
    component: AccountsComponent
  },
  {
    path: 'categories',
    component: CategoriesComponent
  },
  {
    path: 'home',
    redirectTo: '/bds'
  },
  {
    path: 'bds',
    component: BdsAllComponent
  },
  {
    path: 'bds/:id',
    component: BdsDetailComponent
  },
  {
    path: 'auto',
    component: AutoAllComponent
  },
  {
    path: 'auto/:id',
    component: AutoDetailComponent
  },
  {
    path: 'stock',
    component: StockAllComponent
  },
  {
    path: 'changePassword',
    component: LandingComponent
  },
  {
    path: 'login',
    component: LandingComponent
  },
  {
    path: '**',
    redirectTo: '/bds'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: false })],
  exports: [RouterModule]
})
export class AppRoutingModule { }