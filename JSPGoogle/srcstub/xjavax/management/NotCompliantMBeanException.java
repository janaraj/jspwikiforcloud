package xjavax.management;

//import javax.management.OperationsException;

public class NotCompliantMBeanException  extends OperationsException {


    /* Serial version */
    private static final long serialVersionUID = 5175579583207963577L;

    /**
     * Default constructor.
     */
    public NotCompliantMBeanException()  {      
    super();
    } 

    /**
     * Constructor that allows a specific error message to be specified.
     *
     * @param message the detail message.
     */
    public NotCompliantMBeanException(String message)  {      
    super(message);
    } 
    
 }
