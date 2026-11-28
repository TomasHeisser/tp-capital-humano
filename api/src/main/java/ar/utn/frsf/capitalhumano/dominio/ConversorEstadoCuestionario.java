package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Pasa el estado a texto para guardarlo y lo vuelve a armar con la fabrica al leerlo.
 */
@Converter
public class ConversorEstadoCuestionario implements AttributeConverter<EstadoCuestionario, String> {

    private final FabricaEstadoCuestionario fabrica = new FabricaEstadoCuestionario();

    public String aColumna(EstadoCuestionario e) {
        return e == null ? null : e.getNombre();
    }

    public EstadoCuestionario aEntidad(String nombre) {
        return nombre == null ? null : fabrica.crearEstado(nombre);
    }

    @Override
    public String convertToDatabaseColumn(EstadoCuestionario e) {
        return aColumna(e);
    }

    @Override
    public EstadoCuestionario convertToEntityAttribute(String nombre) {
        return aEntidad(nombre);
    }
}
