package ua.lastbite.userservice.exception.user;

public class IncorrectCurrentPasswordException extends RuntimeException {
    public IncorrectCurrentPasswordException() {
        super("Current password is incorrect");
    }
}
