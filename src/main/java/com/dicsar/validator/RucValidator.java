package com.dicsar.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador personalizado para RUC peruano
 * Formato: 11 dígitos numéricos
 */
public class RucValidator implements ConstraintValidator<ValidRuc, String> {

    @Override
    public void initialize(ValidRuc constraintAnnotation) {
    }

    @Override
    public boolean isValid(String ruc, ConstraintValidatorContext context) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return false;
        }

        // RUC debe tener exactamente 11 dígitos
        if (!ruc.matches("^\\d{11}$")) {
            return false;
        }

        // Validación adicional: RUC debe empezar con 10, 15, 16, 17 o 20
        String primerDigito = ruc.substring(0, 2);
        return primerDigito.equals("10") || 
               primerDigito.equals("15") || 
               primerDigito.equals("16") || 
               primerDigito.equals("17") || 
               primerDigito.equals("20");
    }
}
