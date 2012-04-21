package xjavax.management;

import java.io.Serializable;

// import javax.management.*;

public interface QueryExp extends Serializable {
    
    
    /**
     * Applies the QueryExp on an MBean.
     *
     * @param name The name of the MBean on which the QueryExp will be applied.
     *
     * @return  True if the query was successfully applied to the MBean, false otherwise
     *
     * @exception BadStringOperationException
     * @exception BadBinaryOpValueExpException
     * @exception BadAttributeValueExpException 
     * @exception InvalidApplicationException
     */
    public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
    BadAttributeValueExpException, InvalidApplicationException ;

    /**
     * Sets the MBean server on which the query is to be performed.
     *
     * @param s The MBean server on which the query is to be performed.
     */
    public void setMBeanServer(MBeanServer s) ;

}
