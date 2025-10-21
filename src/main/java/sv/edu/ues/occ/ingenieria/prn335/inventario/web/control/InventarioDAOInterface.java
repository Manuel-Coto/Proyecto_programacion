package sv.edu.ues.occ.ingenieria.prn335.inventario.web.control;

import java.util.List;

public interface InventarioDAOInterface<T> {
    public void crear(T registro) throws IllegalArgumentException, IllegalAccessException;

    /*
    public void leer(T registro) throws IllegalArgumentException, IllegalAccessException;
    */
    /*
     * @param id Identificador de la entidad a eliminar
     * @throws IllegalArgumentException Si la entidad es nula
     * @throws IllegalStateException

    public void eliminar(Object id)  throws IllegalArgumentException, IllegalStateException;
     */

    //public T buscarPorId(Object id) throws IllegalArgumentException;

    public List<T> findRange(int first, int max) throws IllegalArgumentException;

    public int count() throws IllegalArgumentException;
}
