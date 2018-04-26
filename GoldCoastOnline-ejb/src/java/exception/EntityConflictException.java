/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exception;

/**
 * Thrown when an entity's unique constraint is violated. 
 * 
 * @author Chuck
 */
public class EntityConflictException extends Exception {
    
    public EntityConflictException(String message) {
        super(message);
    }
}
