package com.dicsar.service;

import com.dicsar.dto.AuthResponse;
import com.dicsar.dto.LoginRequest;
import com.dicsar.dto.UsuarioDTO;
import com.dicsar.entity.RolEntity;
import com.dicsar.entity.Usuario;
import com.dicsar.repository.UsuarioRepository;
import com.dicsar.repository.RolRepository;
import com.dicsar.security.JwtUtil;
import com.dicsar.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar que el usuario tenga un rol asignado
        if (usuario.getRol() == null) {
            throw new ResourceNotFoundException("El usuario no tiene un rol asignado");
        }

        String rolNombre = usuario.getRol().getNombre();
        final String jwt = jwtUtil.generateToken(userDetails, rolNombre);

        return new AuthResponse(jwt, usuario.getUsername(), usuario.getNombreCompleto(), rolNombre);
    }

    @Transactional
    public UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        usuario.setNombreCompleto(usuarioDTO.getNombreCompleto());

        // Si no se especifica rol, asignar VENDEDOR por defecto
        RolEntity rol = usuarioDTO.getRol();
        if (rol == null) {
            rol = rolRepository.findByNombre("VENDEDOR")
                    .orElseThrow(() -> new ResourceNotFoundException("Rol VENDEDOR no encontrado"));
        }
        usuario.setRol(rol);
        usuario.setActivo(usuarioDTO.getActivo() != null ? usuarioDTO.getActivo() : true);

        Usuario saved = usuarioRepository.save(usuario);
        return convertToDTO(saved);
    }

    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UsuarioDTO> listarUsuariosActivos() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UsuarioDTO obtenerUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return convertToDTO(usuario);
    }

    @Transactional
    public UsuarioDTO actualizarUsuario(Integer id, UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuarioDTO.getUsername() != null && !usuarioDTO.getUsername().equals(usuario.getUsername())) {
            if (usuarioRepository.existsByUsername(usuarioDTO.getUsername())) {
                throw new RuntimeException("El nombre de usuario ya existe");
            }
            usuario.setUsername(usuarioDTO.getUsername());
        }

        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        }

        if (usuarioDTO.getNombreCompleto() != null) {
            usuario.setNombreCompleto(usuarioDTO.getNombreCompleto());
        }

        if (usuarioDTO.getRol() != null) {
            usuario.setRol(usuarioDTO.getRol());
        }

        if (usuarioDTO.getActivo() != null) {
            usuario.setActivo(usuarioDTO.getActivo());
        }

        Usuario updated = usuarioRepository.save(usuario);
        return convertToDTO(updated);
    }

    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuarioRepository.delete(usuario);
    }
    
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }
    
    @Transactional
    public void cambiarPassword(String passwordActual, String passwordNueva) {
        // Obtener el usuario actual desde el contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Actualizar a la nueva contraseña
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }

    private UsuarioDTO convertToDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setUsername(usuario.getUsername());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.getActivo());
        // No incluimos la contraseña en el DTO de respuesta
        return dto;
    }
}
