package ua.lastbite.userservice.exception.user;

public class PhoneNumberAlreadyExistsException extends RuntimeException
{
    public PhoneNumberAlreadyExistsException(String phoneNumber) {
        super("Phone number " + phoneNumber + " is already in use");
    }
}
