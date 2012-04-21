package xjavax.management;

public interface NotificationFilter extends java.io.Serializable { 

    /**
     * Invoked before sending the specified notification to the listener.
     *   
     * @param notification The notification to be sent.
     * @return <CODE>true</CODE> if the notification has to be sent to the listener, <CODE>false</CODE> otherwise.
     */  
    public boolean isNotificationEnabled(Notification notification);  
}
