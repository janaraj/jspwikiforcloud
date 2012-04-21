package xjavax.management;

public class OperationsException extends JMException   { 

    /* Serial version */
    private static final long serialVersionUID = -4967597595580536216L;

    /**
     * Default constructor.
     */
    public OperationsException() {
    super();
    }
    
    /**
     * Constructor that allows a specific error message to be specified.
     *
     * @param message the detail message.
     */
    public OperationsException(String message) {
    super(message);
    }
    
}
