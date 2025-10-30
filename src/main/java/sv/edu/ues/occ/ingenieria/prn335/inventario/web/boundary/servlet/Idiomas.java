package sv.edu.ues.occ.ingenieria.prn335.inventario.web.boundary.servlet;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;

@Named("idiomas")
@SessionScoped
public class Idiomas implements Serializable {

    private String idioma = "es"; // Idioma por defecto
    private String pais = "SV";   // País por defecto (El Salvador)

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Locale getLocale() {
        return new Locale(idioma, pais);  // ✅ Con idioma Y país
    }

    public void cambiarIdioma(String nuevoIdioma) {
        this.idioma = nuevoIdioma;
        // Cambiar país según el idioma si es necesario
        if ("en".equals(nuevoIdioma)) {
            this.pais = "US";
        } else if ("es".equals(nuevoIdioma)) {
            this.pais = "SV";
        }

        FacesContext.getCurrentInstance()
                .getViewRoot()
                .setLocale(new Locale(nuevoIdioma, pais));
    }
}