package ua.lastbite.userservice.exception.user;

public class NameNotChangedException extends RuntimeException {
    public NameNotChangedException() {
        super("No changes to first or last name were made");
    }
}
