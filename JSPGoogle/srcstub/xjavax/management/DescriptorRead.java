package xjavax.management;

// import javax.management.Descriptor;

public interface DescriptorRead {
    /** 
     * Returns a copy of Descriptor.
     *
     * @return Descriptor associated with the component implementing this interface.
     * The return value is never null, but the returned descriptor may be empty.
     */
     public Descriptor getDescriptor();
 }
