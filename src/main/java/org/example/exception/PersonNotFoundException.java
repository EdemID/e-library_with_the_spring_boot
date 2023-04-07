package org.example.exception;

public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException(int id) {
        super(String.format("Person with id=%s not found", id));
    }
}