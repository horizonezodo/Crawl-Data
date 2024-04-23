import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ModalConfigComponent } from './modal/modal-config/modal-config.component';
import { TransferService } from './service/transfer.service';
import { PaginationComponent } from './pagination/pagination.component';
import { NavigationComponent } from './navigation/navigation.component';
import { ModalSigninComponent } from './modal/modal-signin/modal-signin.component';
import { Auth_interceptor } from './service/auth_interceptor';
import { ModalSignupComponent } from './modal/modal-signup/modal-signup.component';
import { LandingComponent } from './landing/landing.component';
import { ModalNewPasswordComponent } from './modal/modal-new-password/modal-new-password.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { AccountsComponent } from './accounts/accounts.component';
import { HomeComponent } from './home/home.component';
import { ViewComponent } from './view/view.component';
import { ConfigurationComponent } from './configuration/configuration.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    ViewComponent,
    ConfigurationComponent,
    ModalConfigComponent,
    ModalSigninComponent,
    ModalSignupComponent,
    ModalNewPasswordComponent,
    PaginationComponent,
    NavigationComponent,
    LandingComponent,
    AccountsComponent,
    NotFoundComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: Auth_interceptor,
      multi: true
    },
    TransferService],
  bootstrap: [AppComponent]
})
export class AppModule { }
