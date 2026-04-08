package com.dicsar.config;

import com.dicsar.entity.*;
import com.dicsar.enums.EstadoVencimiento;
import com.dicsar.enums.TipoMovimiento;
import com.dicsar.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🆕 NUEVOS REPOSITORIES
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ProveedorRepository proveedorRepository;
    @Autowired
    private UnidadMedRepository unidadMedRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private MovimientoRepository movimientoRepository;
    @Autowired
    private ReporteVentaRepository reporteVentaRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n🔄 Inicializando datos seed del sistema...\n");

        // 1️⃣ CREAR ROLES
        crearRoles();

        // 2️⃣ CREAR USUARIOS
        crearUsuarios();

        // 3️⃣ CREAR UNIDADES DE MEDIDA
        crearUnidadesMedida();

        // 4️⃣ CREAR CATEGORÍAS
        crearCategorias();

        // 5️⃣ CREAR PROVEEDORES
        crearProveedores();

        // 6️⃣ CREAR PRODUCTOS
        crearProductos();

        // 7️⃣ CREAR CLIENTES
        crearClientes();

        // 8️⃣ CREAR MOVIMIENTOS
        crearMovimientos();

        // 9️⃣ CREAR VENTAS HISTÓRICAS (para dashboard)
        crearVentasHistorico();

        System.out.println("\n✅ Datos seed cargados exitosamente\n");
    }

    // 1️⃣ ROLES
    private void crearRoles() {
        if (!rolRepository.existsByNombre("ADMIN")) {
            rolRepository.save(RolEntity.builder()
                    .nombre("ADMIN")
                    .descripcion("Acceso total al sistema, reportes y exportaciones")
                    .activo(true).build());
            System.out.println("✅ Rol ADMIN creado");
        }
        if (!rolRepository.existsByNombre("VENDEDOR")) {
            rolRepository.save(RolEntity.builder()
                    .nombre("VENDEDOR")
                    .descripcion("Solo puede agregar productos, movimientos")
                    .activo(true).build());
            System.out.println("✅ Rol VENDEDOR creado");
        }
    }

    // 2️⃣ USUARIOS
    private void crearUsuarios() {
        try {
            RolEntity adminRole = rolRepository.findByNombre("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
            RolEntity vendedorRole = rolRepository.findByNombre("VENDEDOR")
                    .orElseThrow(() -> new RuntimeException("Rol VENDEDOR no encontrado"));

            // Crear o actualizar usuario ADMIN - SIEMPRE ACTIVO
            Usuario admin = usuarioRepository.findByUsername("admin").orElse(new Usuario());
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setRol(adminRole);
            admin.setActivo(true); // ✅ SIEMPRE ACTIVO
            usuarioRepository.save(admin);
            System.out.println("✅ Usuario ADMIN: admin/admin123 (activo=true)");

            // Crear o actualizar usuario VENDEDOR - SIEMPRE ACTIVO
            Usuario vendedor = usuarioRepository.findByUsername("vendedor").orElse(new Usuario());
            vendedor.setUsername("vendedor");
            vendedor.setPassword(passwordEncoder.encode("vendedor123"));
            vendedor.setNombreCompleto("Vendedor Demo");
            vendedor.setRol(vendedorRole);
            vendedor.setActivo(true); // ✅ SIEMPRE ACTIVO
            usuarioRepository.save(vendedor);
            System.out.println("✅ Usuario VENDEDOR: vendedor/vendedor123 (activo=true)");
        } catch (RuntimeException e) {
            System.err.println("❌ Error al crear usuarios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 3️⃣ UNIDADES DE MEDIDA
    private void crearUnidadesMedida() {
        String[] unidades = { "kg", "L", "und", "m", "caja", "paq" };
        String[] nombres = { "Kilogramos", "Litros", "Unidades", "Metros", "Caja", "Paquete" };

        for (int i = 0; i < unidades.length; i++) {
            if (!unidadMedRepository.existsByNombre(nombres[i])
                    && !unidadMedRepository.existsByAbreviatura(unidades[i])) {
                unidadMedRepository.save(UnidadMed.builder()
                        .nombre(nombres[i])
                        .abreviatura(unidades[i])
                        .estado(true).build());
            }
        }
        System.out.println("✅ Unidades de medida creadas");
    }

    // 4️⃣ CATEGORÍAS
    private void crearCategorias() {
        String[] cats = { "Alimentos", "Bebidas", "Higiene", "Electrónica", "Textiles" };

        for (String cat : cats) {
            if (!categoriaRepository.existsByNombre(cat)) {
                categoriaRepository.save(Categoria.builder()
                        .nombre(cat)
                        .descripcion("Categoría de " + cat)
                        .estado(true).build());
            }
        }
        System.out.println("✅ Categorías creadas");
    }

    // 5️⃣ PROVEEDORES
    private void crearProveedores() {
        Object[][] provs = {
                { "Distribuidora ABC", "20123456789", "Jr. Principal 100", "985123456", "abc@email.com", "Juan Pérez" },
                { "Importadora XYZ", "20987654321", "Av. Comercial 200", "989876543", "xyz@email.com", "María García" },
                { "Mayorista del Centro", "20111222333", "Calle Central 50", "981111222", "mayorista@email.com",
                        "Carlos López" }
        };

        for (Object[] prov : provs) {
            String ruc = (String) prov[1];
            if (!proveedorRepository.existsByRuc(ruc)) {
                proveedorRepository.save(Proveedor.builder()
                        .razonSocial((String) prov[0])
                        .ruc(ruc)
                        .direccion((String) prov[2])
                        .telefono((String) prov[3])
                        .email((String) prov[4])
                        .contacto((String) prov[5])
                        .estado(true).build());
            }
        }
        System.out.println("✅ Proveedores creados");
    }

    // 6️⃣ PRODUCTOS
    private void crearProductos() {
        // Primero verificar que existan las unidades de medida necesarias
        if (unidadMedRepository.count() == 0 || categoriaRepository.count() == 0 || proveedorRepository.count() == 0) {
            System.out.println("⚠️ No se pueden crear productos: faltan categorías, unidades o proveedores");
            return;
        }

        try {
            // Buscar o crear las unidades que necesitamos
            UnidadMed kg = unidadMedRepository.findByAbreviatura("kg")
                    .orElseGet(() -> unidadMedRepository.save(UnidadMed.builder()
                            .nombre("Kilogramos")
                            .abreviatura("kg")
                            .estado(true).build()));

            UnidadMed l = unidadMedRepository.findByAbreviatura("L")
                    .orElseGet(() -> unidadMedRepository.save(UnidadMed.builder()
                            .nombre("Litros")
                            .abreviatura("L")
                            .estado(true).build()));

            UnidadMed und = unidadMedRepository.findByAbreviatura("und")
                    .orElseGet(() -> unidadMedRepository.save(UnidadMed.builder()
                            .nombre("Unidades")
                            .abreviatura("und")
                            .estado(true).build()));

            Categoria alimentos = categoriaRepository.findByNombre("Alimentos")
                    .orElseThrow(() -> new RuntimeException("Categoría 'Alimentos' no encontrada"));
            Categoria bebidas = categoriaRepository.findByNombre("Bebidas")
                    .orElseThrow(() -> new RuntimeException("Categoría 'Bebidas' no encontrada"));
            Categoria higiene = categoriaRepository.findByNombre("Higiene")
                    .orElseThrow(() -> new RuntimeException("Categoría 'Higiene' no encontrada"));

            List<Proveedor> proveedores = proveedorRepository.findAll();
            if (proveedores.isEmpty()) {
                throw new RuntimeException("No hay proveedores disponibles");
            }
            Proveedor prov1 = proveedores.get(0);
            Proveedor prov2 = proveedores.size() > 1 ? proveedores.get(1) : prov1;

            Object[][] prods = {
                    // Alimentos
                    { "Arroz Integral", "ARRZ-001", "Arroz 100% integral premium", 8.50, 5.00, 100, 20,
                            LocalDate.now().plusDays(180), alimentos, kg, prov1 },
                    { "Frijoles Negros", "FRIJ-001", "Frijoles negros de calidad", 6.50, 3.50, 80, 15,
                            LocalDate.now().plusDays(200), alimentos, kg, prov1 },
                    { "Lenteja Roja", "LENT-001", "Lenteja roja pelada", 7.50, 4.00, 60, 10,
                            LocalDate.now().plusDays(220), alimentos, kg, prov2 },
                    { "Papa Amarilla", "PAPA-001", "Papa amarilla fresca", 2.50, 1.00, 200, 50,
                            LocalDate.now().plusDays(60), alimentos, kg, prov1 },

                    // Bebidas
                    { "Aceite Cocinero", "ACEI-001", "Aceite vegetal puro", 9.00, 5.50, 50, 10,
                            LocalDate.now().plusDays(365), bebidas, l, prov2 },
                    { "Jugo Natural Naranja", "JUGO-001", "Jugo 100% naranja", 3.50, 2.00, 120, 20,
                            LocalDate.now().plusDays(30), bebidas, l, prov1 },
                    { "Agua Mineral", "AGUA-001", "Agua mineral 500ml", 1.50, 0.80, 300, 50,
                            LocalDate.now().plusDays(365), bebidas, und, prov2 },

                    // Higiene
                    { "Detergente Polvo", "DETE-001", "Detergente multiusos 1kg", 5.00, 2.80, 75, 15,
                            LocalDate.now().plusDays(180), higiene, kg, prov1 },
                    { "Jabón Líquido", "JABO-001", "Jabón líquido 500ml", 4.00, 2.00, 90, 15,
                            LocalDate.now().plusDays(180), higiene, l, prov2 },
                    { "Papel Higiénico", "PAPE-001", "Papel higiénico doble hoja", 3.50, 1.50, 200, 30,
                            LocalDate.now().plusDays(90), higiene, und, prov1 }
            };

            for (Object[] prod : prods) {
                String codigo = (String) prod[1];
                if (!productoRepository.existsByCodigo(codigo)) {
                    productoRepository.save(Producto.builder()
                            .nombre((String) prod[0])
                            .codigo(codigo)
                            .descripcion((String) prod[2])
                            .precio((Double) prod[3])
                            .precioCompra((Double) prod[4])
                            .stockActual((Integer) prod[5])
                            .stockMinimo((Integer) prod[6])
                            .fechaVencimiento((LocalDate) prod[7])
                            .categoria((Categoria) prod[8])
                            .unidadMedida((UnidadMed) prod[9])
                            .proveedor((Proveedor) prod[10])
                            .estado(true)
                            .estadoVencimiento(EstadoVencimiento.VIGENTE).build());
                }
            }
            System.out.println("✅ Productos creados (10 ítems)");
        } catch (RuntimeException e) {
            System.err.println("❌ Error al crear productos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 7️⃣ CLIENTES
    private void crearClientes() {
        Object[][] clientes = {
                { "Juan", "Pérez García", "DNI", "12345678", "Jr. Lima 100", "985111111", "juan@email.com", false },
                { "María", "García López", "DNI", "87654321", "Jr. Ucayali 200", "987222222", "maria@email.com",
                        false },
                { "Carlos", "Rodríguez Martínez", "DNI", "11223344", "Jr. Amazonas 150", "988333333",
                        "carlos@email.com", false },
                { "Ana", "Fernández Ruiz", "DNI", "22334455", "Av. Brasil 300", "989444444", "ana@email.com", false },
                { "Pedro", "López Sánchez", "DNI", "33445566", "Calle Central 50", "981555555", "pedro@email.com",
                        false },
                { "Empresa ABC", "", "RUC", "20123456789", "Av. Principal 500", "981333333", "empresa@email.com",
                        true },
                { "Empresa XYZ", "", "RUC", "20987654321", "Av. Comercial 800", "982666666", "xyz@email.com", true },
                { "Comercial Perú", "", "RUC", "20555666777", "Jr. del Comercio 250", "983777777",
                        "comercial@email.com", true }
        };

        for (Object[] cliente : clientes) {
            String doc = (String) cliente[3];
            if (!clienteRepository.existsByNumeroDocumento(doc)) {
                clienteRepository.save(Cliente.builder()
                        .nombre((String) cliente[0])
                        .apellidos((String) cliente[1])
                        .tipoDocumento((String) cliente[2])
                        .numeroDocumento(doc)
                        .direccion((String) cliente[4])
                        .telefono((String) cliente[5])
                        .email((String) cliente[6])
                        .esEmpresa((Boolean) cliente[7])
                        .estado(true).build());
            }
        }
        System.out.println("✅ Clientes creados (8 clientes)");
    }

    // 8️⃣ MOVIMIENTOS
    private void crearMovimientos() {
        if (movimientoRepository.count() == 0 && productoRepository.count() > 0) {
            List<Producto> productos = productoRepository.findAll();
            Random random = new Random();

            // Crear múltiples movimientos para cada producto
            for (Producto producto : productos) {
                // 1. Entrada inicial (compra)
                movimientoRepository.save(Movimiento.builder()
                        .producto(producto)
                        .tipoMovimiento(TipoMovimiento.ENTRADA)
                        .cantidad(producto.getStockActual())
                        .precio(producto.getPrecioCompra())
                        .descripcion("Compra inicial a proveedor")
                        .usuarioMovimiento("admin")
                        .fechaMovimiento(LocalDateTime.now().minusDays(30)).build());

                // 2. Salidas aleatorias (ventas)
                for (int i = 0; i < 3; i++) {
                    int cantidad = random.nextInt(5) + 1; // 1-5 unidades
                    movimientoRepository.save(Movimiento.builder()
                            .producto(producto)
                            .tipoMovimiento(TipoMovimiento.SALIDA)
                            .cantidad(cantidad)
                            .precio(producto.getPrecio())
                            .descripcion("Venta a cliente")
                            .usuarioMovimiento("vendedor")
                            .fechaMovimiento(LocalDateTime.now().minusDays(random.nextInt(25))).build());
                }

                // 3. Ajuste de inventario
                if (random.nextBoolean()) {
                    movimientoRepository.save(Movimiento.builder()
                            .producto(producto)
                            .tipoMovimiento(TipoMovimiento.AJUSTE)
                            .cantidad(random.nextInt(2)) // 0-1 unidades
                            .precio(producto.getPrecio())
                            .descripcion("Ajuste por inventario físico")
                            .usuarioMovimiento("admin")
                            .fechaMovimiento(LocalDateTime.now().minusDays(random.nextInt(10))).build());
                }
            }

            System.out.println("✅ Movimientos históricos creados");
        }
    }

    // 9️⃣ VENTAS HISTÓRICAS (para dashboard y reportes)
    private void crearVentasHistorico() {
        System.out.println("💰 Generando HISTÓRICO DE VENTAS...");

        // 🔄 LIMPIAR tabla reporte_venta para reiniciar
        long ventasActuales = reporteVentaRepository.count();
        if (ventasActuales > 0) {
            reporteVentaRepository.deleteAll();
            System.out.println("   🗑️ Eliminadas " + ventasActuales + " ventas antiguas");
        }

        if (productoRepository.count() > 0 && clienteRepository.count() > 0) {
            List<Producto> productos = productoRepository.findAll();
            List<Cliente> clientes = clienteRepository.findAll();
            Random random = new Random();

            int totalVentas = 0;

            // EXACTAMENTE 60 VENTAS EN 3 MESES = 20 VENTAS/MES = 5 VENTAS/SEMANA
            for (int mes = 3; mes >= 1; mes--) {
                int diasAtras = mes * 30;

                // 20 ventas por mes (EXACTO)
                for (int i = 0; i < 20; i++) {
                    try {
                        Producto producto = productos.get(random.nextInt(productos.size()));
                        Cliente cliente = clientes.get(random.nextInt(clientes.size()));

                        int cantidad = random.nextInt(10) + 1;
                        Double precioUnitario = producto.getPrecio();
                        Double total = precioUnitario * cantidad;

                        int diaDelMes = random.nextInt(30);
                        LocalDateTime fechaVenta = LocalDateTime.now()
                                .minusDays(diasAtras - diaDelMes)
                                .plusHours(random.nextInt(24))
                                .plusMinutes(random.nextInt(60));

                        reporteVentaRepository.save(ReporteVenta.builder()
                                .cliente(cliente)
                                .producto(producto)
                                .cantidad(cantidad)
                                .precioUnitario(precioUnitario)
                                .total(total)
                                .tipoDocumento(cliente.getEsEmpresa() ? "Factura" : "Factura")
                                .fechaVenta(fechaVenta)
                                .estado(true).build());

                        totalVentas++;
                    } catch (Exception e) {
                        System.err.println("⚠️ Error generando venta: " + e.getMessage());
                    }
                }
            }

            System.out.println("   ✅ " + totalVentas + " Ventas históricas creadas (3 meses - 20/mes - 5/semana)");
        }
    }
}
