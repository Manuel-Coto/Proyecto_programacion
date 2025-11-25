package sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

@Entity
@Table(name = "compra", schema = "public")
public class Compra {

    @Id
    @Column(name = "id_compra", nullable = false)
    private Long id;

    @Column(name = "id_proveedor")
    private Integer idProveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", insertable = false, updatable = false)
    private Proveedor proveedor;

    @Column(name = "fecha")
    private OffsetDateTime fecha;

    @Size(max = 10)
    @Column(name = "estado", length = 10)
    private String estado;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
        if (proveedor != null) {
            this.idProveedor = proveedor.getId();
        }
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public void setFecha(OffsetDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}