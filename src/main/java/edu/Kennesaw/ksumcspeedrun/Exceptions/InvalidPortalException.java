package edu.Kennesaw.ksumcspeedrun.Exceptions;

// Exception is thrown when a portal object is initialized when two invalid dimensions
public class InvalidPortalException extends Exception {
    public InvalidPortalException(String message) {
        super(message);
    }
}
