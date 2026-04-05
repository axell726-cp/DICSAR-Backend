package com.dicsar.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotación para validar RUC peruano
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RucValidator.class)
public @interface ValidRuc {
    
    String message() default "RUC inválido. Debe tener 11 dígitos y empezar con 10, 15, 16, 17 o 20";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
