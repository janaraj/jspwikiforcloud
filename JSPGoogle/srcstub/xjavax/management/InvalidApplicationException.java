package xjavax.management;

public class InvalidApplicationException extends Exception   { 
    

    /* Serial version */
    private static final long serialVersionUID = -3048022274675537269L;
  
    /**
     * @serial The object representing the class of the MBean
     */
    private Object val;


    /**
     * Constructs an <CODE>InvalidApplicationException</CODE> with the specified <CODE>Object</CODE>.
     *
     * @param val the detail message of this exception.
     */
    public InvalidApplicationException(Object val) { 
    this.val = val;
    }
}
