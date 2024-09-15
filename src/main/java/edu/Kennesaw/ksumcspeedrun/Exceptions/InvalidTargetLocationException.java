package edu.Kennesaw.ksumcspeedrun.Exceptions;

/* Exception is thrown when an EnterObjective is initialized and the object is not an instanceof
   Biome, SRStructure, or Portal */
public class InvalidTargetLocationException extends Exception {
    public InvalidTargetLocationException(String message) {
        super(message);
    }
}
