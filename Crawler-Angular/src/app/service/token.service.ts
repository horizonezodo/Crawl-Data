import { Injectable } from '@angular/core';

const TOKEN_KEY = 'Token_Key';
const REFRESH_TOKEN_KEY = 'Refresh_Token_Key';
const USERNAME_KEY = 'Username_Key';
const USER_IMAGE_KEY = 'User_Image_Key';
const USER_ROLE_KEY = 'User_Role_Key';
const TOKEN_VALID_KEY = 'Token_Valid_Key';

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  constructor() { }

  public setToken(token: string) {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.setItem(TOKEN_KEY, token);
  }

  public getToken(): string {
    // @ts-ignore
    return localStorage.getItem(TOKEN_KEY);
  }

  public setRefreshToken(refreshToken: string) {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }

  public getRefreshToken(): string {
    // @ts-ignore
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  public setUsername(username: string) {
    localStorage.removeItem(USERNAME_KEY);
    localStorage.setItem(USERNAME_KEY, username);
  }

  public getUsername(): string {
    // @ts-ignore
    return localStorage.getItem(USERNAME_KEY);
  }

  public setUserImage(userImage: string) {
    localStorage.removeItem(USER_IMAGE_KEY);
    localStorage.setItem(USER_IMAGE_KEY, userImage);
  }

  public getUserImage(): string {
    // @ts-ignore
    return localStorage.getItem(USER_IMAGE_KEY);
  }

  public setUserRole(userRole: string) {
    localStorage.removeItem(USER_ROLE_KEY);
    localStorage.setItem(USER_ROLE_KEY, userRole);
  }

  public getUserRole(): string {
    // @ts-ignore
    return localStorage.getItem(USER_ROLE_KEY);
  }

  public setTokenValid(tokenValid: boolean) {
    localStorage.removeItem(TOKEN_VALID_KEY);
    localStorage.setItem(TOKEN_VALID_KEY, JSON.stringify(tokenValid));
  }

  public getTokenValid(): boolean {
    if (localStorage.getItem(TOKEN_VALID_KEY)) {
      // @ts-ignore
      return JSON.parse(localStorage.getItem(TOKEN_VALID_KEY));
    } else return false;
  }
}
