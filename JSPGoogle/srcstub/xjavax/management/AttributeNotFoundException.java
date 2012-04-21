package xjavax.management;

public class AttributeNotFoundException extends OperationsException {

    /* Serial version */
    private static final long serialVersionUID = 6511584241791106926L;

    /**
     * Default constructor.
     */
    public AttributeNotFoundException() {
    super();
    }

    /**
     * Constructor that allows a specific error message to be specified.
     *
     * @param message detail message.
     */
    public AttributeNotFoundException(String message) {
    super(message);
    }

}
