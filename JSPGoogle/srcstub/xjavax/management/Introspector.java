package xjavax.management;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//import javax.management.Descriptor;
// import javax.management.DescriptorKey;
//import javax.management.ImmutableDescriptor;

public class Introspector
{
    public static Descriptor descriptorForElement(final AnnotatedElement elmt) {
        if (elmt == null)
            return ImmutableDescriptor.EMPTY_DESCRIPTOR;
        final Annotation[] annots = elmt.getAnnotations();
        return descriptorForAnnotations(annots);
    }
    
    public static Descriptor descriptorForAnnotations(Annotation[] annots) {
        if (annots.length == 0)
            return ImmutableDescriptor.EMPTY_DESCRIPTOR;
        Map<String, Object> descriptorMap = new HashMap<String, Object>();
        for (Annotation a : annots) {
            Class<? extends Annotation> c = a.annotationType();
            Method[] elements = c.getMethods();
            for (Method element : elements) {
                DescriptorKey key = element.getAnnotation(DescriptorKey.class);
                if (key != null) {
                    String name = key.value();
                    Object value;
                    try {
                        value = element.invoke(a);
                    } catch (RuntimeException e) { 
                        // we don't expect this - except for possibly
                        // security exceptions? 
                        // RuntimeExceptions shouldn't be "UndeclaredThrowable".
                        // anyway...
                        //
                        throw e;
                    } catch (Exception e) {
                        // we don't expect this
                        throw new UndeclaredThrowableException(e);
                    }
                    value = annotationToField(value);
                    Object oldValue = descriptorMap.put(name, value);
                    if (oldValue != null && !equals(oldValue, value)) {
                        final String msg =
                            "Inconsistent values for descriptor field " + name +
                            " from annotations: " + value + " :: " + oldValue;
                        throw new IllegalArgumentException(msg);
                    }
                }
            }
        }
        
        if (descriptorMap.isEmpty())
            return ImmutableDescriptor.EMPTY_DESCRIPTOR;
        else
            return new ImmutableDescriptor(descriptorMap);
    }
    
    private static Object annotationToField(Object x) {
        // An annotation element cannot have a null value but never mind
        if (x == null)
            return null;
        if (x instanceof Number || x instanceof String ||
                x instanceof Character || x instanceof Boolean ||
                x instanceof String[])
            return x;
        // Remaining possibilities: array of primitive (e.g. int[]),
        // enum, class, array of enum or class.
        Class<?> c = x.getClass();
        if (c.isArray()) {
            if (c.getComponentType().isPrimitive())
                return x;
            Object[] xx = (Object[]) x;
            String[] ss = new String[xx.length];
            for (int i = 0; i < xx.length; i++)
                ss[i] = (String) annotationToField(xx[i]);
            return ss;
        }
        if (x instanceof Class)
            return ((Class<?>) x).getName();
        if (x instanceof Enum)
            return ((Enum) x).name();
        // The only other possibility is that the value is another
        // annotation, or that the language has evolved since this code
        // was written.  We don't allow for either of those currently.
        throw new IllegalArgumentException("Illegal type for annotation " +
                "element: " + x.getClass().getName());
    }
    
    // This must be consistent with the check for duplicate field values in
    // ImmutableDescriptor.union.  But we don't expect to be called very
    // often so this inefficient check should be enough.
    private static boolean equals(Object x, Object y) {
        return Arrays.deepEquals(new Object[] {x}, new Object[] {y});
    }


    
}
