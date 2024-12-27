package ua.lastbite.userservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Constraint(validatedBy = SizeValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomSize {
    String message() default "Invalid size";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int min() default 0;
    int max() default Integer.MAX_VALUE;
}
