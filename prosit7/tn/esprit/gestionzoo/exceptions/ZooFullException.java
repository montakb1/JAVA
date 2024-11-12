package gestionzoo.exceptions;

public class ZooFullException extends Exception {
    public ZooFullException() {
        super("Le zoo est plein !");
    }

    public ZooFullException(String message) {
        super(message);
    }
}

