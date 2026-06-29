package com.dicsar.validator;

import com.dicsar.repository.ProveedorRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validador personalizado para RUC peruano
 * Formato: 11 dígitos numéricos
 */
@Component
public class RucValidator implements ConstraintValidator<ValidRuc, String> {

    @Autowired(required = false)
    private ProveedorRepository proveedorRepository;

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
        boolean formato = primerDigito.equals("10") || 
               primerDigito.equals("15") || 
               primerDigito.equals("16") || 
               primerDigito.equals("17") || 
               primerDigito.equals("20");

        if (!formato) return false;

        // Si hay repositorio disponible, validar existencia en BD (sin mock)
        try {
            if (proveedorRepository != null) {
                return proveedorRepository.existsByRuc(ruc);
            }
        } catch (Exception ignored) {}

        // Fallback al formato si no se puede consultar BD
        return true;
    }
}
