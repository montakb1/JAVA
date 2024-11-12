package gestionzoo.exceptions;

public class InvalidAgeException extends Exception {
    public InvalidAgeException() {
        super("L'âge ne peut pas être négatif !");
    }

    public InvalidAgeException(String message) {
        super(message);
    }
}
