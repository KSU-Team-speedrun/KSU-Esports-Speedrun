package edu.Kennesaw.ksumcspeedrun.Exceptions;

// Exception is thrown when a player tries to set a KillObjective to an entity that is not living
public class NonLivingEntityException extends Exception {
    public NonLivingEntityException(String message) { super(message); }
}
