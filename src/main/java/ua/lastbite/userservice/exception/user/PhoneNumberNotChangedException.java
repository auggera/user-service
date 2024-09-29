package ua.lastbite.userservice.exception.user;

public class PhoneNumberNotChangedException extends RuntimeException {
    public PhoneNumberNotChangedException() {
        super("New phone number cannot be the same as the current phone number");
    }
}
