package xjavax.management;

import java.io.ObjectInputStream;
import java.util.Set;

//import javax.management.*;
//import javax.management.Attribute;
//import javax.management.AttributeList;
//import javax.management.AttributeNotFoundException;
//import javax.management.IntrospectionException;
//import javax.management.InvalidAttributeValueException;
//import javax.management.MBeanException;
//import javax.management.MBeanInfo;
//import javax.management.MBeanServerConnection;
//import javax.management.ObjectName;
//import javax.management.OperationsException;
//import javax.management.QueryExp;
//import javax.management.ReflectionException;
//import javax.management.RuntimeOperationsException;
//import javax.management.loading.ClassLoaderRepository;

public interface MBeanServer extends MBeanServerConnection {
    
    // doc comment inherited from MBeanServerConnection
    public ObjectInstance createMBean(String className, ObjectName name)
        throws ReflectionException, InstanceAlreadyExistsException,
           MBeanRegistrationException, MBeanException,
           NotCompliantMBeanException;

    // doc comment inherited from MBeanServerConnection
    public ObjectInstance createMBean(String className, ObjectName name,
                      ObjectName loaderName) 
        throws ReflectionException, InstanceAlreadyExistsException,
           MBeanRegistrationException, MBeanException,
           NotCompliantMBeanException, InstanceNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public ObjectInstance createMBean(String className, ObjectName name,
                      Object params[], String signature[]) 
        throws ReflectionException, InstanceAlreadyExistsException,
               MBeanRegistrationException, MBeanException,
               NotCompliantMBeanException;

    // doc comment inherited from MBeanServerConnection
    public ObjectInstance createMBean(String className, ObjectName name,
                      ObjectName loaderName, Object params[],
                      String signature[]) 
        throws ReflectionException, InstanceAlreadyExistsException,
               MBeanRegistrationException, MBeanException,
               NotCompliantMBeanException, InstanceNotFoundException;

    /**
     * Registers a pre-existing object as an MBean with the MBean
     * server. If the object name given is null, the MBean must
     * provide its own name by implementing the {@link
     * javax.management.MBeanRegistration MBeanRegistration} interface
     * and returning the name from the {@link
     * MBeanRegistration#preRegister preRegister} method.
     *
     * @param object The  MBean to be registered as an MBean.     
     * @param name The object name of the MBean. May be null.
     *
     * @return An <CODE>ObjectInstance</CODE>, containing the
     * <CODE>ObjectName</CODE> and the Java class name of the newly
     * registered MBean.  If the contained <code>ObjectName</code>
     * is <code>n</code>, the contained Java class name is
     * <code>{@link #getMBeanInfo getMBeanInfo(n)}.getClassName()</code>.
     *
     * @exception InstanceAlreadyExistsException The MBean is already
     * under the control of the MBean server.
     * @exception MBeanRegistrationException The
     * <CODE>preRegister</CODE> (<CODE>MBeanRegistration</CODE>
     * interface) method of the MBean has thrown an exception. The
     * MBean will not be registered.
     * @exception NotCompliantMBeanException This object is not a JMX
     * compliant MBean
     * @exception RuntimeOperationsException Wraps a
     * <CODE>java.lang.IllegalArgumentException</CODE>: The object
     * passed in parameter is null or no object name is specified.
     */
    public ObjectInstance registerMBean(Object object, ObjectName name)
        throws InstanceAlreadyExistsException, MBeanRegistrationException,
           NotCompliantMBeanException;

    // doc comment inherited from MBeanServerConnection
    public void unregisterMBean(ObjectName name)
        throws InstanceNotFoundException, MBeanRegistrationException;

    // doc comment inherited from MBeanServerConnection
    public ObjectInstance getObjectInstance(ObjectName name)
        throws InstanceNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query);

    // doc comment inherited from MBeanServerConnection
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query);

    // doc comment inherited from MBeanServerConnection
    public boolean isRegistered(ObjectName name);

    /**
     * Returns the number of MBeans registered in the MBean server.
     *
     * @return the number of registered MBeans, wrapped in an Integer.
     * If the caller's permissions are restricted, this number may
     * be greater than the number of MBeans the caller can access.
     */
    public Integer getMBeanCount();

    // doc comment inherited from MBeanServerConnection
    public Object getAttribute(ObjectName name, String attribute)
        throws MBeanException, AttributeNotFoundException,
               InstanceNotFoundException, ReflectionException;

    // doc comment inherited from MBeanServerConnection
    public AttributeList getAttributes(ObjectName name, String[] attributes)
        throws InstanceNotFoundException, ReflectionException;

    // doc comment inherited from MBeanServerConnection
    public void setAttribute(ObjectName name, Attribute attribute)
        throws InstanceNotFoundException, AttributeNotFoundException,
           InvalidAttributeValueException, MBeanException, 
           ReflectionException;

    // doc comment inherited from MBeanServerConnection
    public AttributeList setAttributes(ObjectName name,
                       AttributeList attributes)
    throws InstanceNotFoundException, ReflectionException;

    // doc comment inherited from MBeanServerConnection
    public Object invoke(ObjectName name, String operationName,
             Object params[], String signature[])
        throws InstanceNotFoundException, MBeanException,
           ReflectionException;
 
    // doc comment inherited from MBeanServerConnection
    public String getDefaultDomain();

    // doc comment inherited from MBeanServerConnection
    public String[] getDomains();

    // doc comment inherited from MBeanServerConnection
    public void addNotificationListener(ObjectName name,
                    NotificationListener listener,
                    NotificationFilter filter,
                    Object handback)
        throws InstanceNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public void addNotificationListener(ObjectName name,
                    ObjectName listener,
                    NotificationFilter filter,
                    Object handback)
        throws InstanceNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public void removeNotificationListener(ObjectName name,
                       ObjectName listener) 
    throws InstanceNotFoundException, ListenerNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public void removeNotificationListener(ObjectName name,
                       ObjectName listener,
                       NotificationFilter filter,
                       Object handback)
        throws InstanceNotFoundException, ListenerNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public void removeNotificationListener(ObjectName name,
                       NotificationListener listener)
        throws InstanceNotFoundException, ListenerNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public void removeNotificationListener(ObjectName name,
                       NotificationListener listener,
                       NotificationFilter filter,
                       Object handback)
        throws InstanceNotFoundException, ListenerNotFoundException;

    // doc comment inherited from MBeanServerConnection
    public MBeanInfo getMBeanInfo(ObjectName name)
        throws InstanceNotFoundException, IntrospectionException,
               ReflectionException;

 
    // doc comment inherited from MBeanServerConnection
    public boolean isInstanceOf(ObjectName name, String className)
        throws InstanceNotFoundException;

    /**
     * <p>Instantiates an object using the list of all class loaders
     * registered in the MBean server's {@link
     * javax.management.loading.ClassLoaderRepository Class Loader
     * Repository}.  The object's class should have a public
     * constructor.  This method returns a reference to the newly
     * created object.  The newly created object is not registered in
     * the MBean server.</p>
     *
     * <p>This method is equivalent to {@link
     * #instantiate(String,Object[],String[])
     * instantiate(className, (Object[]) null, (String[]) null)}.</p>
     *
     * @param className The class name of the object to be instantiated.    
     *
     * @return The newly instantiated object.    
     *
     * @exception ReflectionException Wraps a
     * <CODE>java.lang.ClassNotFoundException</CODE> or the
     * <CODE>java.lang.Exception</CODE> that occurred when trying to
     * invoke the object's constructor.
     * @exception MBeanException The constructor of the object has
     * thrown an exception
     * @exception RuntimeOperationsException Wraps a
     * <CODE>java.lang.IllegalArgumentException</CODE>: The className
     * passed in parameter is null.
     */
    public Object instantiate(String className)
        throws ReflectionException, MBeanException;


    /**
     * <p>Instantiates an object using the class Loader specified by its
     * <CODE>ObjectName</CODE>.  If the loader name is null, the
     * ClassLoader that loaded the MBean Server will be used.  The
     * object's class should have a public constructor.  This method
     * returns a reference to the newly created object.  The newly
     * created object is not registered in the MBean server.</p>
     *
     * <p>This method is equivalent to {@link
     * #instantiate(String,ObjectName,Object[],String[])
     * instantiate(className, loaderName, (Object[]) null, (String[])
     * null)}.</p>
     *
     * @param className The class name of the MBean to be instantiated.    
     * @param loaderName The object name of the class loader to be used.
     *
     * @return The newly instantiated object.    
     *
     * @exception ReflectionException Wraps a
     * <CODE>java.lang.ClassNotFoundException</CODE> or the
     * <CODE>java.lang.Exception</CODE> that occurred when trying to
     * invoke the object's constructor.
     * @exception MBeanException The constructor of the object has
     * thrown an exception.
     * @exception InstanceNotFoundException The specified class loader
     * is not registered in the MBeanServer.
     * @exception RuntimeOperationsException Wraps a
     * <CODE>java.lang.IllegalArgumentException</CODE>: The className
     * passed in parameter is null.
     */
    public Object instantiate(String className, ObjectName loaderName) 
        throws ReflectionException, MBeanException,
           InstanceNotFoundException;

    /**
     * <p>Instantiates an object using the list of all class loaders
     * registered in the MBean server {@link
     * javax.management.loading.ClassLoaderRepository Class Loader
     * Repository}.  The object's class should have a public
     * constructor.  The call returns a reference to the newly created
     * object.  The newly created object is not registered in the
     * MBean server.</p>
     *
     * @param className The class name of the object to be instantiated.
     * @param params An array containing the parameters of the
     * constructor to be invoked.
     * @param signature An array containing the signature of the
     * constructor to be invoked.
     *
     * @return The newly instantiated object.    
     *
     * @exception ReflectionException Wraps a
     * <CODE>java.lang.ClassNotFoundException</CODE> or the
     * <CODE>java.lang.Exception</CODE> that occurred when trying to
     * invoke the object's constructor.
     * @exception MBeanException The constructor of the object has
     * thrown an exception
     * @exception RuntimeOperationsException Wraps a
     * <CODE>java.lang.IllegalArgumentException</CODE>: The className
     * passed in parameter is null.
     */    
    public Object instantiate(String className, Object params[],
                  String signature[]) 
        throws ReflectionException, MBeanException; 

    /**
     * <p>Instantiates an object. The class loader to be used is
     * identified by its object name. If the object name of the loader
     * is null, the ClassLoader that loaded the MBean server will be
     * used.  The object's class should have a public constructor.
     * The call returns a reference to the newly created object.  The
     * newly created object is not registered in the MBean server.</p>
     *
     * @param className The class name of the object to be instantiated.
     * @param params An array containing the parameters of the
     * constructor to be invoked.
     * @param signature An array containing the signature of the
     * constructor to be invoked.
     * @param loaderName The object name of the class loader to be used.
     *
     * @return The newly instantiated object.    
     *
     * @exception ReflectionException Wraps a <CODE>java.lang.ClassNotFoundException</CODE> or the <CODE>java.lang.Exception</CODE> that 
     * occurred when trying to invoke the object's constructor.  
     * @exception MBeanException The constructor of the object has
     * thrown an exception
     * @exception InstanceNotFoundException The specified class loader
     * is not registered in the MBean server.
     * @exception RuntimeOperationsException Wraps a
     * <CODE>java.lang.IllegalArgumentException</CODE>: The className
     * passed in parameter is null.
     */    
    public Object instantiate(String className, ObjectName loaderName,
                  Object params[], String signature[]) 
        throws ReflectionException, MBeanException,
           InstanceNotFoundException;

    /**
     * <p>De-serializes a byte array in the context of the class loader 
     * of an MBean.</p>
     *
     * @param name The name of the MBean whose class loader should be
     * used for the de-serialization.
     * @param data The byte array to be de-sererialized.
     *
     * @return The de-serialized object stream.
     *
     * @exception InstanceNotFoundException The MBean specified is not
     * found.
     * @exception OperationsException Any of the usual Input/Output
     * related exceptions.
     *
     * @deprecated Use {@link #getClassLoaderFor getClassLoaderFor} to
     * obtain the appropriate class loader for deserialization.
     */
    @Deprecated
    public ObjectInputStream deserialize(ObjectName name, byte[] data)
        throws InstanceNotFoundException, OperationsException;


    /**
     * <p>De-serializes a byte array in the context of a given MBean
     * class loader.  The class loader is found by loading the class
     * <code>className</code> through the {@link
     * javax.management.loading.ClassLoaderRepository Class Loader
     * Repository}.  The resultant class's class loader is the one to
     * use.
     *
     * @param className The name of the class whose class loader should be
     * used for the de-serialization.
     * @param data The byte array to be de-sererialized.
     *
     * @return  The de-serialized object stream.
     *
     * @exception OperationsException Any of the usual Input/Output
     * related exceptions.
     * @exception ReflectionException The specified class could not be
     * loaded by the class loader repository
     *
     * @deprecated Use {@link #getClassLoaderRepository} to obtain the
     * class loader repository and use it to deserialize.
     */
    @Deprecated
    public ObjectInputStream deserialize(String className, byte[] data)
        throws OperationsException, ReflectionException;

   
    /**
     * <p>De-serializes a byte array in the context of a given MBean
     * class loader.  The class loader is the one that loaded the
     * class with name "className".  The name of the class loader to
     * be used for loading the specified class is specified.  If null,
     * the MBean Server's class loader will be used.</p>
     *
     * @param className The name of the class whose class loader should be
     * used for the de-serialization.
     * @param data The byte array to be de-sererialized.
     * @param loaderName The name of the class loader to be used for
     * loading the specified class.  If null, the MBean Server's class
     * loader will be used.
     *
     * @return  The de-serialized object stream.
     *
     * @exception InstanceNotFoundException The specified class loader
     * MBean is not found.
     * @exception OperationsException Any of the usual Input/Output
     * related exceptions.
     * @exception ReflectionException The specified class could not be
     * loaded by the specified class loader.
     *
     * @deprecated Use {@link #getClassLoader getClassLoader} to obtain
     * the class loader for deserialization.
     */
    @Deprecated
    public ObjectInputStream deserialize(String className,
                     ObjectName loaderName,
                     byte[] data)
        throws InstanceNotFoundException, OperationsException,
           ReflectionException;

    /**
     * <p>Return the {@link java.lang.ClassLoader} that was used for
     * loading the class of the named MBean.</p>
     *
     * @param mbeanName The ObjectName of the MBean.
     *
     * @return The ClassLoader used for that MBean.  If <var>l</var>
     * is the MBean's actual ClassLoader, and <var>r</var> is the
     * returned value, then either:
     *
     * <ul>
     * <li><var>r</var> is identical to <var>l</var>; or
     * <li>the result of <var>r</var>{@link
     * ClassLoader#loadClass(String) .loadClass(<var>s</var>)} is the
     * same as <var>l</var>{@link ClassLoader#loadClass(String)
     * .loadClass(<var>s</var>)} for any string <var>s</var>.
     * </ul>
     *
     * What this means is that the ClassLoader may be wrapped in
     * another ClassLoader for security or other reasons.
     *
     * @exception InstanceNotFoundException if the named MBean is not found.
     *
     * @since.unbundled JMX 1.2
     */
    public ClassLoader getClassLoaderFor(ObjectName mbeanName) 
    throws InstanceNotFoundException;

    /**
     * <p>Return the named {@link java.lang.ClassLoader}.</p>
     *
     * @param loaderName The ObjectName of the ClassLoader.  May be
     * null, in which case the MBean server's own ClassLoader is
     * returned.
     *
     * @return The named ClassLoader.  If <var>l</var> is the actual
     * ClassLoader with that name, and <var>r</var> is the returned
     * value, then either:
     *
     * <ul>
     * <li><var>r</var> is identical to <var>l</var>; or
     * <li>the result of <var>r</var>{@link
     * ClassLoader#loadClass(String) .loadClass(<var>s</var>)} is the
     * same as <var>l</var>{@link ClassLoader#loadClass(String)
     * .loadClass(<var>s</var>)} for any string <var>s</var>.
     * </ul>
     *
     * What this means is that the ClassLoader may be wrapped in
     * another ClassLoader for security or other reasons.
     *
     * @exception InstanceNotFoundException if the named ClassLoader is 
     *    not found.
     *
     * @since.unbundled JMX 1.2
     */
    public ClassLoader getClassLoader(ObjectName loaderName)
    throws InstanceNotFoundException;

    /**
     * <p>Return the ClassLoaderRepository for this MBeanServer.
     * @return The ClassLoaderRepository for this MBeanServer.
     *
     * @since.unbundled JMX 1.2
     */
    public ClassLoaderRepository getClassLoaderRepository();
}
