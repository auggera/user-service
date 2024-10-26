package ua.lastbite.userservice.exception.token;

public class TokenAlreadyUsedException extends RuntimeException {
    public TokenAlreadyUsedException(String message) {
        super(message);
    }
}
