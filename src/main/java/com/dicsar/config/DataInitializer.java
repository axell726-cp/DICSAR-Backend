package com.dicsar.config;

import com.dicsar.entity.Rol;
import com.dicsar.entity.Usuario;
import com.dicsar.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Crear usuario admin si no existe
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);
            usuarioRepository.save(admin);
            System.out.println("✅ Usuario ADMIN creado: admin / admin123");
        }
        
        // Crear usuario vendedor si no existe
        if (!usuarioRepository.existsByUsername("vendedor")) {
            Usuario vendedor = new Usuario();
            vendedor.setUsername("vendedor");
            vendedor.setPassword(passwordEncoder.encode("vendedor123"));
            vendedor.setNombreCompleto("Vendedor del Sistema");
            vendedor.setRol(Rol.VENDEDOR);
            vendedor.setActivo(true);
            usuarioRepository.save(vendedor);
            System.out.println("✅ Usuario VENDEDOR creado: vendedor / vendedor123");
        }
    }
}
