package ua.lastbite.userservice.exception.user;

public class EmailNotChangedException extends RuntimeException {
    public EmailNotChangedException() {
        super("New email is the same as the current email");
    }
}
