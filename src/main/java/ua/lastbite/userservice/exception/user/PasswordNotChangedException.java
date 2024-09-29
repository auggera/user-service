package ua.lastbite.userservice.exception.user;

public class PasswordNotChangedException extends RuntimeException {
    public PasswordNotChangedException() {
        super("New password cannot be the same as the current password");
    }
}
