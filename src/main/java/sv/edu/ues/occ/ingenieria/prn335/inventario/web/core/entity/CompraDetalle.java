package sv.edu.ues.occ.ingenieria.prn335.inventario.web.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "compra_detalle", schema = "public")
public class CompraDetalle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id_compra_detalle", nullable = false)
    private UUID id;

    @NotNull(message = "La compra es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_compra", nullable = false)
    private Compra idCompra;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
    @Column(name = "cantidad", precision = 8, scale = 2, nullable = false)
    private BigDecimal cantidad;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio debe ser válido")
    @Column(name = "precio", precision = 8, scale = 2, nullable = false)
    private BigDecimal precio;

    @Size(max = 10, message = "El estado no debe exceder 10 caracteres")
    @Column(name = "estado", length = 10)
    private String estado;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    // Constructor vacío
    public CompraDetalle() {
    }

    // Constructor con parámetros principales
    public CompraDetalle(UUID id, Compra idCompra, Producto idProducto,
                         BigDecimal cantidad, BigDecimal precio, String estado) {
        this.id = id;
        this.idCompra = idCompra;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precio = precio;
        this.estado = estado;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Compra getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(Compra idCompra) {
        this.idCompra = idCompra;
    }

    public Producto getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Producto idProducto) {
        this.idProducto = idProducto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
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

    // Métodos útiles
    @Override
    public String toString() {
        return "CompraDetalle{" +
                "id=" + id +
                ", idCompra=" + (idCompra != null ? idCompra.getId() : null) +
                ", idProducto=" + (idProducto != null ? idProducto.getNombreProducto() : null) +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                ", estado='" + estado + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompraDetalle that = (CompraDetalle) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}