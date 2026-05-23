// exception/BusinessException.java
package com.itc.recolecta.recolectaDemo.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}