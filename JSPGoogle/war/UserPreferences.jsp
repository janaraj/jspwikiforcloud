<%@ page import="org.apache.commons.logging.*" %>
<%@ page import="org.apache.wiki.VariableManager" %>
<%@ page import="org.apache.wiki.WikiContext" %>
<%@ page import="org.apache.wiki.WikiSession" %>
<%@ page import="org.apache.wiki.WikiEngine" %>
<%@ page import="org.apache.wiki.auth.UserManager" %>
<%@ page import="org.apache.wiki.auth.WikiSecurityException" %>
<%@ page import="org.apache.wiki.auth.login.CookieAssertionLoginModule" %>
<%@ page import="org.apache.wiki.auth.user.DuplicateUserException" %>
<%@ page import="org.apache.wiki.auth.user.UserProfile" %>
<%@ page import="org.apache.wiki.workflow.DecisionRequiredException" %>
<%@ page import="org.apache.wiki.ui.EditorManager" %>
<%@ page import="org.apache.wiki.ui.TemplateManager" %>
<%@ page import="org.apache.wiki.preferences.*" %>
<%@ page import="org.apache.wiki.spring.BeanHolder" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%! 
    Log log = LogFactory.getLog("JSPWiki"); 
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.PREFS );
    if(!wikiContext.hasAccess( response )) return;
    
    // Extract the user profile and action attributes
//    UserManager userMgr = wiki.getUserManager();
    UserManager userMgr = BeanHolder.getUserManager();
    WikiSession wikiSession = wikiContext.getWikiSession();

    // Are we saving the profile?
    if( "saveProfile".equals(request.getParameter("action")) )
    {
        UserProfile profile = userMgr.parseProfile( wikiContext );
         
        // Validate the profile
        userMgr.validateProfile( wikiContext, profile );

        // If no errors, save the profile now & refresh the principal set!
        if ( wikiSession.getMessages( "profile" ).length == 0 )
        {
            try
            {
                userMgr.setUserProfile( wikiSession, profile );
                CookieAssertionLoginModule.setUserCookie( response, profile.getFullname() );
            }
            catch( DuplicateUserException e )
            {
                // User collision! (full name or wiki name already taken)
                wikiSession.addMessage( "profile", e.getMessage() );
            }
            catch( DecisionRequiredException e )
            {
                String redirect = wiki.getURL(WikiContext.VIEW,"ApprovalRequiredForUserProfiles",null,true);
                response.sendRedirect( redirect );
                return;
            }
            catch( WikiSecurityException e )
            {
                // Something went horribly wrong! Maybe it's an I/O error...
                wikiSession.addMessage( "profile", e.getMessage() );
            }
        }
        if ( wikiSession.getMessages( "profile" ).length == 0 )
        {
            String redirectPage = request.getParameter( "redirect" );

            if( !wiki.pageExists( redirectPage ) )
            {
               redirectPage = wiki.getFrontPage();
            }
            
            String viewUrl = ( "UserPreferences".equals( redirectPage ) ) ? "Wiki.jsp" : wiki.getViewURL( redirectPage );
            log.info( "Redirecting user to " + viewUrl );
            response.sendRedirect( viewUrl );
            return;
        }
    }
    if( "setAssertedName".equals(request.getParameter("action")) )
    {
        Preferences.reloadPreferences(pageContext);
        
        String assertedName = request.getParameter("assertedName");
        CookieAssertionLoginModule.setUserCookie( response, assertedName );

        String redirectPage = request.getParameter( "redirect" );
        if( !wiki.pageExists( redirectPage ) )
        {
          redirectPage = wiki.getFrontPage();
        }
        String viewUrl = ( "UserPreferences".equals( redirectPage ) ) ? "Wiki.jsp" : wiki.getViewURL( redirectPage );

        log.info( "Redirecting user to " + viewUrl );
        response.sendRedirect( viewUrl );
        return;
    }
    if( "clearAssertedName".equals(request.getParameter("action")) )
    {
        CookieAssertionLoginModule.clearUserCookie( response );
        response.sendRedirect( wikiContext.getURL(WikiContext.NONE,"Logout.jsp") );
        return;
    }
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    String contentPage = BeanHolder.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "ViewTemplate.jsp" );
%>
<wiki:Include page="<%=contentPage%>" />
