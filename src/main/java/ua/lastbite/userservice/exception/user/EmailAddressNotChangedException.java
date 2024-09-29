package ua.lastbite.userservice.exception.user;

public class EmailAddressNotChangedException extends RuntimeException {
    public EmailAddressNotChangedException() {
        super("New email is the same as the current email");
    }
}
