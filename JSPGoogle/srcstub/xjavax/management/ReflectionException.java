package xjavax.management;

public class ReflectionException extends JMException   { 

    /* Serial version */
    private static final long serialVersionUID = 9170809325636915553L;

    /**
     * @serial The wrapped {@link Exception}
     */
    private java.lang.Exception exception ;


    /**
     * Creates a <CODE>ReflectionException</CODE> that wraps the actual <CODE>java.lang.Exception</CODE>.
     *
     * @param e the wrapped exception.
     */   
    public ReflectionException(java.lang.Exception e) { 
    super() ;
    exception = e ; 
    } 

    /**
     * Creates a <CODE>ReflectionException</CODE> that wraps the actual <CODE>java.lang.Exception</CODE> with
     * a detail message.
     *
     * @param e the wrapped exception.
     * @param message the detail message.
     */
    public ReflectionException(java.lang.Exception e, String message) { 
    super(message) ;
    exception = e ; 
    } 

    /**
     * Returns the actual {@link Exception} thrown.
     *
     * @return the wrapped {@link Exception}.
     */
    public java.lang.Exception getTargetException()  { 
    return exception ;
    } 

    /**
     * Returns the actual {@link Exception} thrown.
     *
     * @return the wrapped {@link Exception}.
     */
    public Throwable getCause() {
    return exception;
    }
}