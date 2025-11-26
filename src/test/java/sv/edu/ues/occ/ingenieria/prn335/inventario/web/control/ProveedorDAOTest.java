package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity.Proveedor;

import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorDAOTest {

    @Mock
    private EntityManager entityManager;

    private ProveedorDAO proveedorDAO;

    private Proveedor proveedor;
    private Integer testId;

    @BeforeEach
    void setUp() throws Exception {
        proveedorDAO = new ProveedorDAO();

        // Usar reflexión para inyectar el EntityManager mock
        Field emField = ProveedorDAO.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(proveedorDAO, entityManager);

        testId = 1;
        proveedor = new Proveedor();
        proveedor.setId(testId);
        proveedor.setNombre("Distribuidora El Salvador S.A.");
        proveedor.setRazonSocial("Distribuidora El Salvador Sociedad Anónima");
        proveedor.setNit("0614-123456-101-2");
        proveedor.setActivo(true);
        proveedor.setObservaciones("Proveedor principal de tecnología");
    }

    @Test
    void testGetEntityManager() {
        EntityManager result = proveedorDAO.getEntityManager();
        assertNotNull(result);
        assertEquals(entityManager, result);
    }

    @Test
    void testFindById_Success() {
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("Distribuidora El Salvador S.A.", result.getNombre());
        assertEquals("Distribuidora El Salvador Sociedad Anónima", result.getRazonSocial());
        assertEquals("0614-123456-101-2", result.getNit());
        assertTrue(result.getActivo());
        assertEquals("Proveedor principal de tecnología", result.getObservaciones());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testFindById_NotFound() {
        Integer id = 999;
        when(entityManager.find(Proveedor.class, id)).thenReturn(null);

        Proveedor result = proveedorDAO.findById(id);

        assertNull(result);
        verify(entityManager).find(Proveedor.class, id);
    }

    @Test
    void testFindById_Exception() {
        Integer id = 1;
        when(entityManager.find(Proveedor.class, id))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(IllegalStateException.class, () -> proveedorDAO.findById(id));
        verify(entityManager).find(Proveedor.class, id);
    }

    @Test
    void testFindById_NullId() {
        assertThrows(IllegalArgumentException.class, () -> {
            proveedorDAO.findById(null);
        });
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    void testFindById_InactiveProveedor() {
        proveedor.setActivo(false);
        when(entityManager.find(Proveedor.class, testId)).thenReturn(proveedor);

        Proveedor result = proveedorDAO.findById(testId);

        assertNotNull(result);
        assertFalse(result.getActivo());
        verify(entityManager).find(Proveedor.class, testId);
    }

    @Test
    void testCrear_Success() {
        Proveedor newProveedor = new Proveedor();
        newProveedor.setNombre("Nuevo Proveedor");
        newProveedor.setActivo(true);

        doNothing().when(entityManager).persist(newProveedor);

        proveedorDAO.crear(newProveedor);

        assertNotNull(newProveedor.getId());
        verify(entityManager).persist(newProveedor);
    }

    @Test
    void testCrear_NullProveedor() {
        assertThrows(IllegalArgumentException.class, () -> {
            proveedorDAO.crear(null);
        });
        verify(entityManager, never()).persist(any());
    }

    @Test
    void testCrear_Exception() {
        Proveedor newProveedor = new Proveedor();
        newProveedor.setNombre("Proveedor con Error");

        doThrow(new RuntimeException("Database error"))
                .when(entityManager).persist(newProveedor);

        assertThrows(IllegalStateException.class, () -> proveedorDAO.crear(newProveedor));
        verify(entityManager).persist(newProveedor);
    }
}
