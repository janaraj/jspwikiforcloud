<%@ page import="org.apache.commons.logging.*" %>
<%@ page import="org.apache.wiki.*" %>
<%@ page import="org.apache.wiki.auth.NoSuchPrincipalException" %>
<%@ page import="org.apache.wiki.auth.WikiSecurityException" %>
<%@ page import="org.apache.wiki.auth.authorize.Group" %>
<%@ page import="org.apache.wiki.auth.authorize.GroupManager" %>
<%@ page import="org.apache.wiki.spring.BeanHolder" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%! 
    Log log = LogFactory.getLog("JSPWiki"); 
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.CREATE_GROUP );
    if(!wikiContext.hasAccess( response )) return;
    
    // Extract the current user, group name, members and action attributes
    WikiSession wikiSession = wikiContext.getWikiSession();
//    GroupManager groupMgr = wiki.getGroupManager();
    GroupManager groupMgr = BeanHolder.getGroupManager();
    Group group = null;
    try 
    {
        group = groupMgr.parseGroup( wikiContext, true );
        pageContext.setAttribute ( "Group", group, PageContext.REQUEST_SCOPE );
    }
    catch ( WikiSecurityException e )
    {
        wikiSession.addMessage( GroupManager.MESSAGES_KEY, e.getMessage() );
        response.sendRedirect( "Group.jsp" );
    }
    
    // Are we saving the group?
    if( "save".equals(request.getParameter("action")) )
    {
        // Validate the group
        groupMgr.validateGroup( wikiContext, group );
        
        try 
        {
            groupMgr.getGroup( group.getName() );

            // Oops! The group already exists. This is mischief!
            ResourceBundle rb = wikiContext.getBundle("CoreResources");
            Object[] args = { group.getName() };
            wikiSession.addMessage( GroupManager.MESSAGES_KEY,
                                    MessageFormat.format(rb.getString("newgroup.exists"),args));
        }
        catch ( NoSuchPrincipalException e )
        {
            // Group not found; this is good!
        }

        // If no errors, save the group now
        if ( wikiSession.getMessages( GroupManager.MESSAGES_KEY ).length == 0 )
        {
            try
            {
                groupMgr.setGroup( wikiSession, group );
            }
            catch( WikiSecurityException e )
            {
                // Something went horribly wrong! Maybe it's an I/O error...
                wikiSession.addMessage( GroupManager.MESSAGES_KEY, e.getMessage() );
            }
        }
        if ( wikiSession.getMessages( GroupManager.MESSAGES_KEY ).length == 0 )
        {
            response.sendRedirect( "Group.jsp?group=" + group.getName() );
            return;
        }
    }

    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    String contentPage = BeanHolder.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "ViewTemplate.jsp" );

%><wiki:Include page="<%=contentPage%>" />

