package es.uji.ei1048.spike;

public class LugarNoEncontradoException extends RuntimeException {

    public LugarNoEncontradoException(String lugar) {
        super("No se ha encontrado el lugar: " + lugar);
    }
}
