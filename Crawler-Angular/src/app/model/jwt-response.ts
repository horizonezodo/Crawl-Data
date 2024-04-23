export class JwtResponse {
    username: string;
    email: string;
    user_role: string;
    user_image: string;
    token: string;
    refreshToken: string;

    constructor(username: string, email: string, user_role: string, user_image: string, token: string, refreshToken: string) {
        this.username = username;
        this.email = email;
        this.user_role = user_role;
        this.token = token;
        this.refreshToken = refreshToken;
        this.user_image = user_image;
    }
}