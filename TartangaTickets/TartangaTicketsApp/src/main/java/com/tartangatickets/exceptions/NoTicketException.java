/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tartangatickets.exceptions;

/**
 *
 * @author ubuntu
 */
public class NoTicketException extends Exception {

    /**
     * Creates a new instance of <code>NoTicketException</code> without detail
     * message.
     */
    public NoTicketException() {
    }

    /**
     * Constructs an instance of <code>NoTicketException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NoTicketException(String msg) {
        super(msg);
    }
}
