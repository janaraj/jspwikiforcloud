package xjavax.management;


public class BadBinaryOpValueExpException extends Exception   { 


    /* Serial version */
    private static final long serialVersionUID = -3105272988410493376L;

    /**
     * @serial The attribute value that originated this exception
     */
    private Object val;

    /**
     * Constructs an <CODE>BadAttributeValueExpException</CODE> with the specified Object.
     *
     * @param val the inappropriate value.
     */
    public BadBinaryOpValueExpException (Object val) { 
    this.val = val;
    } 
   
 
    /**
     * Returns the string representing the object.
     */
    public String toString()  { 
    return "BadAttributeValueException: " + val;
    } 

 }
