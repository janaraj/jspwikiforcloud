package xjavax.management;

import java.security.PrivilegedAction;

public class GetPropertyAction implements PrivilegedAction<String> {
    private final String key;

    public GetPropertyAction(String key) {
    this.key = key;
    }

    public String run() {
    return System.getProperty(key);
    }
}
