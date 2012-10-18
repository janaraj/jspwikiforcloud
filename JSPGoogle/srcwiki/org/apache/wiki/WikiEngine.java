/* 
    JSPWiki - a JSP-based WikiWiki clone.

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.  
 */
package org.apache.wiki;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.auth.acl.AclManager;
import org.apache.wiki.auth.acl.DefaultAclManager;
import org.apache.wiki.diff.DifferenceManager;
import org.apache.wiki.event.WikiEngineEvent;
import org.apache.wiki.event.WikiEventListener;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.filters.FilterManager;
import org.apache.wiki.i18n.InternationalizationManager;
import org.apache.wiki.parser.JSPWikiMarkupParser;
import org.apache.wiki.plugin.PluginManager;
import org.apache.wiki.providers.ProviderException;
import org.apache.wiki.providers.WikiPageProvider;
import org.apache.wiki.render.RenderingManager;
import org.apache.wiki.rss.RSSGenerator;
import org.apache.wiki.rss.RSSThread;
import org.apache.wiki.spring.BeanHolder;
import org.apache.wiki.spring.WikiSetContext;
import org.apache.wiki.ui.Command;
import org.apache.wiki.ui.CommandResolver;
import org.apache.wiki.url.URLConstructor;
import org.apache.wiki.util.ClassUtil;
import org.apache.wiki.util.WatchDog;
import org.apache.wiki.workflow.Decision;
import org.apache.wiki.workflow.DecisionRequiredException;
import org.apache.wiki.workflow.Fact;
import org.apache.wiki.workflow.Task;
import org.apache.wiki.workflow.Workflow;
import org.apache.wiki.workflow.WorkflowBuilder;
import org.apache.wiki.workflow.WorkflowManager;

/**
 * Provides Wiki services to the JSP page.
 * 
 * <P>
 * This is the main interface through which everything should go.
 * 
 * <P>
 * Using this class: Always get yourself an instance from JSP page by using the
 * WikiEngine.getInstance() method. Never create a new WikiEngine() from
 * scratch, unless you're writing tests.
 * <p>
 * There's basically only a single WikiEngine for each web application, and you
 * should always get it using the WikiEngine.getInstance() method.
 */
@SuppressWarnings("serial")
public class WikiEngine implements Serializable {
    // private static final String ATTR_WIKIENGINE =
    // "org.apache.wiki.WikiEngine";

    private static final Log log = LogFactory.getLog(WikiEngine.class);

    /** Stores properties. */
    private Properties m_properties;

    /** Property for application name */
    public static final String PROP_APPNAME = "jspwiki.applicationName";

    /** Property start for any interwiki reference. */
    public static final String PROP_INTERWIKIREF = "jspwiki.interWikiRef.";

    /** If true, then the user name will be stored with the page data. */
    public static final String PROP_STOREUSERNAME = "jspwiki.storeUserName";

    /** Define the used encoding. Currently supported are ISO-8859-1 and UTF-8 */
    public static final String PROP_ENCODING = "jspwiki.encoding";

    /** The name for the base URL to use in all references. */
    public static final String PROP_BASEURL = "jspwiki.baseURL";

    /**
     * The name for the property which allows you to set the current reference
     * style. The value is {@value} .
     */
    public static final String PROP_REFSTYLE = "jspwiki.referenceStyle";

    /** Property name for the "spaces in titles" -hack. */
    public static final String PROP_BEAUTIFYTITLE = "jspwiki.breakTitleWithSpaces";

    public static final String WIKIACTIONRESULT = "wikiactionresult";

    /**
     * Property name for where the jspwiki work directory should be. If not
     * specified, reverts to ${java.tmpdir}.
     */
    public static final String PROP_WORKDIR = "jspwiki.workDir";

    /** The name of the cookie that gets stored to the user browser. */
    public static final String PREFS_COOKIE_NAME = "JSPWikiUserProfile";

    /** Property name for the "match english plurals" -hack. */
    public static final String PROP_MATCHPLURALS = "jspwiki.translatorReader.matchEnglishPlurals";

    /** Property name for the template that is used. */
    public static final String PROP_TEMPLATEDIR = "jspwiki.templateDir";

    /** Property name for the default front page. */
    public static final String PROP_FRONTPAGE = "jspwiki.frontPage";

    /** Property name for setting the url generator instance */

    public static final String PROP_URLCONSTRUCTOR = "jspwiki.urlConstructor";

    /**
     * If this property is set to false, all filters are disabled when
     * translating.
     */
    public static final String PROP_RUNFILTERS = "jspwiki.runFilters";

    /**
     * The name of the property containing the ACLManager implementing class.
     * The value is {@value} .
     */
    public static final String PROP_ACL_MANAGER_IMPL = "jspwiki.aclManager";

    /**
     * If this property is set to false, we don't allow the creation of empty
     * pages
     */
    public static final String PROP_ALLOW_CREATION_OF_EMPTY_PAGES = "jspwiki.allowCreationOfEmptyPages";

    /** Should the user info be saved with the page data as well? */
    private boolean m_saveUserInfo = true;

    /** If true, uses UTF8 encoding for all data */
    private boolean m_useUTF8 = true;

    /** Stores the base URL. */
    private String m_baseURL;

    /** Stores the ACL manager. */
    private AclManager m_aclManager = null;

    /** Resolves wiki actions, JSPs and special pages. */
    private CommandResolver m_commandResolver = null;

    private InternationalizationManager m_internationalizationManager;

    /** Generates RSS feed when requested. */
    private RSSGenerator m_rssGenerator;

    /** The RSS file to generate. */
    private String m_rssFile;

    /** If true, all titles will be cleaned. */
    private boolean m_beautifyTitle = false;

    /** Stores the template path. This is relative to "templates". */
    private String m_templateDir;

    /** The default front page name. Defaults to "Main". */
    private String m_frontPage;

    /** The time when this engine was started. */
    private Date m_startTime;

    /** The location where the work directory is. */
    private String m_workDir;

    /** Each engine has their own application id. */
    private String m_appid = "";

    private boolean m_isConfigured = false; // Flag.

    /** Each engine has its own workflow manager. */
    private WorkflowManager m_workflowMgr = null;

    /** Stores wikiengine attributes. */
    private Map<String, Object> m_attributes = Collections
            .synchronizedMap(new HashMap<String, Object>());

    private boolean isInitialized() {
        return m_workflowMgr != null;
    }

    /**
     * Gets a WikiEngine related to this servlet. Since this method is only
     * called from JSP pages (and JspInit()) to be specific, we throw a
     * RuntimeException if things don't work.
     * 
     * @param config
     *            The ServletConfig object for this servlet.
     * 
     * @return A WikiEngine instance.
     * @throws InternalWikiException
     *             in case something fails. This is a RuntimeException, so be
     *             prepared for it.
     */

    // FIXME: It seems that this does not work too well, jspInit()
    // does not react to RuntimeExceptions, or something...

    public static synchronized WikiEngine getInstance(ServletConfig config)
            throws InternalWikiException {
        return getInstance(config.getServletContext(), null);
    }

    /**
     * Gets a WikiEngine related to the servlet. Works like
     * getInstance(ServletConfig), but does not force the Properties object.
     * This method is just an optional way of initializing a WikiEngine for
     * embedded JSPWiki applications; normally, you should use
     * getInstance(ServletConfig).
     * 
     * @param config
     *            The ServletConfig of the webapp servlet/JSP calling this
     *            method.
     * @param props
     *            A set of properties, or null, if we are to load JSPWiki's
     *            default jspwiki.properties (this is the usual case).
     * 
     * @return One well-behaving WikiEngine instance.
     */
    public static synchronized WikiEngine getInstance(ServletConfig config,
            Properties props) {
        return getInstance(config.getServletContext(), props);
    }

    /**
     * ` Gets a WikiEngine related to the servlet. Works just like getInstance(
     * ServletConfig )
     * 
     * @param context
     *            The ServletContext of the webapp servlet/JSP calling this
     *            method.
     * @param props
     *            A set of properties, or null, if we are to load JSPWiki's
     *            default jspwiki.properties (this is the usual case).
     * 
     * @return One fully functional, properly behaving WikiEngine.
     * @throws InternalWikiException
     *             If the WikiEngine instantiation fails.
     */

    // FIXME: Potential make-things-easier thingy here: no need to fetch the
    // wikiengine anymore
    // Wiki.jsp.jspInit() [really old code]; it's probably even faster to fetch
    // it
    // using this method every time than go to pageContext.getAttribute().

    public static synchronized WikiEngine getInstance(ServletContext context,
            Properties props) throws InternalWikiException {
        WikiSetContext.setContext(context, "WikiEngine");
        WikiEngine engine = BeanHolder.getWikiEngine();
        if (!engine.isInitialized()) {
            String appid = Integer.toString(context.hashCode()); // FIXME:
                                                                 // Kludge,
                                                                 // use real
                                                                 // type.

            context.log(" Assigning new engine to " + appid);
            try {
                if (props == null) {
                    props = BeanHolder.getWikiProperties();
                }

                engine.initializeWikiEngine(appid, props);
            } catch (Exception e) {
                context.log("ERROR: Failed to create a Wiki engine: "
                        + e.getMessage());
                log.error(
                        "ERROR: Failed to create a Wiki engine, stacktrace follows ",
                        e);
                throw new InternalWikiException("No wiki engine, check logs.");
            }

        }

        return engine;
    }

    /**
     * Instantiate using this method when you're running as a servlet and
     * WikiEngine will figure out where to look for the property file. Do not
     * use this method - use WikiEngine.getInstance() instead.
     * 
     * @param context
     *            A ServletContext.
     * @param appid
     *            An Application ID. This application is an unique random string
     *            which is used to recognize this WikiEngine.
     * @param props
     *            The WikiEngine configuration.
     * @throws WikiException
     *             If the WikiEngine construction fails.
     */
    private void initializeWikiEngine(String appid,
            Properties props) throws WikiException {
        // super();
        // m_servletContext = context;
        m_appid = appid;

        // Stash the WikiEngine in the servlet context

        try {
            //
            // Note: May be null, if JSPWiki has been deployed in a WAR file.
            //
            initialize(props);
        } catch (Exception e) {
            String msg = Release.APPNAME
                    + ": Unable to load and setup properties from jspwiki.properties. "
                    + e.getMessage();
            throw new WikiException(msg, e);
        }
    }

    /**
     * Does all the real initialization.
     */
    private void initialize(Properties props) throws WikiException {
        m_startTime = new Date();
        m_properties = props;

        log.info("*******************************************");
        log.info(Release.APPNAME + " " + Release.getVersionString()
                + " starting. Whee!");

        log.debug("Java version: " + System.getProperty("java.runtime.version"));
        log.debug("Java vendor: " + System.getProperty("java.vm.vendor"));
        log.debug("OS: " + System.getProperty("os.name") + " "
                + System.getProperty("os.version") + " "
                + System.getProperty("os.arch"));
        log.debug("Default server locale: " + Locale.getDefault());
        log.debug("Default server timezone: "
                + TimeZone.getDefault().getDisplayName(true, TimeZone.LONG));

        log.debug("Configuring WikiEngine...");

        // Initializes the CommandResolver
        m_commandResolver = new CommandResolver(this, props);

        m_saveUserInfo = TextUtil.getBooleanProperty(props, PROP_STOREUSERNAME,
                m_saveUserInfo);

        m_useUTF8 = "UTF-8".equals(TextUtil.getStringProperty(props,
                PROP_ENCODING, "ISO-8859-1"));
        m_baseURL = TextUtil.getStringProperty(props, PROP_BASEURL, "");
        if (!m_baseURL.endsWith("/")) {
            m_baseURL = m_baseURL + "/";
        }

        m_beautifyTitle = TextUtil.getBooleanProperty(props,
                PROP_BEAUTIFYTITLE, m_beautifyTitle);

        m_templateDir = TextUtil.getStringProperty(props, PROP_TEMPLATEDIR,
                "default");
        m_frontPage = TextUtil.getStringProperty(props, PROP_FRONTPAGE, "Main");

        //
        // Initialize the important modules. Any exception thrown by the
        // managers means that we will not start up.
        //

        // FIXME: This part of the code is getting unwieldy. We must think
        // of a better way to do the startup-sequence.
        try {
            m_aclManager = getAclManager();

            // Start the Workflow manager
            m_workflowMgr = (WorkflowManager) ClassUtil
                    .getMappedObject(WorkflowManager.class.getName());
            m_workflowMgr.initialize(this, props);

            m_internationalizationManager = (InternationalizationManager) ClassUtil
                    .getMappedObject(
                            InternationalizationManager.class.getName(), this);

            initReferenceManager();

        }

        catch (RuntimeException e) {
            // RuntimeExceptions may occur here, even if they shouldn't.
            log.fatal("Failed to start managers.", e);
            e.printStackTrace();
            throw new WikiException("Failed to start managers: "
                    + e.getMessage(), e);
        } catch (Exception e) {
            // Final catch-all for everything
            log.fatal(
                    "JSPWiki could not start, due to an unknown exception when starting.",
                    e);
            e.printStackTrace();
            throw new WikiException(
                    "Failed to start; please check log files for better information.",
                    e);
        }

        //
        // Initialize the good-to-have-but-not-fatal modules.
        //
        try {
            if (TextUtil.getBooleanProperty(props,
                    RSSGenerator.PROP_GENERATE_RSS, false)) {
                m_rssGenerator = (RSSGenerator) ClassUtil.getMappedObject(
                        RSSGenerator.class.getName(), this, props);
            }

        } catch (Exception e) {
            log.error(
                    "Unable to start RSS generator - JSPWiki will still work, "
                            + "but there will be no RSS feed.", e);
        }

        // Start the RSS generator & generator thread
        if (m_rssGenerator != null) {
            m_rssFile = TextUtil.getStringProperty(props,
                    RSSGenerator.PROP_RSSFILE, "rss.rdf");
            File rssFile = null;
            if (m_rssFile.startsWith(File.separator)) {
                // honor absolute pathnames:
                rssFile = new File(m_rssFile);
            } else {
                // relative path names are anchored from the webapp root path:
                String rootPath = BeanHolder.getRootURL();
                rssFile = new File(rootPath, m_rssFile);
            }
            int rssInterval = TextUtil.getIntegerProperty(props,
                    RSSGenerator.PROP_INTERVAL, 3600);
            RSSThread rssThread = new RSSThread(this, rssFile, rssInterval);
            rssThread.start();
        }

        log.info("WikiEngine configured.");
        m_isConfigured = true;
    }

    /**
     * Initializes the reference manager. Scans all existing WikiPages for
     * internal links and adds them to the ReferenceManager object.
     * 
     * @throws WikiException
     *             If the reference manager initialization fails.
     */
    @SuppressWarnings("unchecked")
    private void initReferenceManager() throws WikiException {
    }

    /**
     * Throws an exception if a property is not found.
     * 
     * @param props
     *            A set of properties to search the key in.
     * @param key
     *            The key to look for.
     * @return The required property
     * 
     * @throws NoRequiredPropertyException
     *             If the search key is not in the property set.
     */

    // FIXME: Should really be in some util file.
    public static String getRequiredProperty(Properties props, String key)
            throws NoRequiredPropertyException {
        String value = TextUtil.getStringProperty(props, key, null);

        if (value == null) {
            throw new NoRequiredPropertyException(
                    "Required property not found", key);
        }

        return value;
    }

    /**
     * Returns the set of properties that the WikiEngine was initialized with.
     * Note that this method returns a direct reference, so it's possible to
     * manipulate the properties. However, this is not advised unless you really
     * know what you're doing.
     * 
     * @return The wiki properties
     */

    public Properties getWikiProperties() {
        return m_properties;
    }

    /**
     * Returns the JSPWiki working directory set with "jspwiki.workDir".
     * 
     * @since 2.1.100
     * @return The working directory.
     */
    public String getWorkDir() {
        return m_workDir;
    }

    /**
     * Don't use.
     * 
     * @since 1.8.0
     * @deprecated
     * @return Something magical.
     */
    public String getPluginSearchPath() {
        // FIXME: This method should not be here, probably.
        return TextUtil.getStringProperty(m_properties,
                PluginManager.PROP_SEARCHPATH, null);
    }

    /**
     * Returns the current template directory.
     * 
     * @since 1.9.20
     * @return The template directory as initialized by the engine.
     */
    public String getTemplateDir() {
        return m_templateDir;
    }

    /**
     * Returns the base URL, telling where this Wiki actually lives.
     * 
     * @since 1.6.1
     * @return The Base URL.
     */

    public String getBaseURL() {
        return m_baseURL;
    }

    /**
     * Returns the moment when this engine was started.
     * 
     * @since 2.0.15.
     * @return The start time of this wiki.
     */

    public Date getStartTime() {
        return (Date) m_startTime.clone();
    }

    /**
     * <p>
     * Returns the basic absolute URL to a page, without any modifications. You
     * may add any parameters to this.
     * </p>
     * <p>
     * Since 2.3.90 it is safe to call this method with <code>null</code>
     * pageName, in which case it will default to the front page.
     * </p>
     * 
     * @since 2.0.3
     * @param pageName
     *            The name of the page. May be null, in which case defaults to
     *            the front page.
     * @return An absolute URL to the page.
     */
    public String getViewURL(String pageName) {
        if (pageName == null) {
            pageName = getFrontPage();
        }
        URLConstructor u = BeanHolder.getURLConstructor();
        return u.makeURL(WikiContext.VIEW, pageName, true, null);
    }

    /**
     * Returns the basic URL to an editor. Please use WikiContext.getURL() or
     * WikiEngine.getURL() instead.
     * 
     * @see #getURL(String, String, String, boolean)
     * @see WikiContext#getURL(String, String)
     * @deprecated
     * 
     * @param pageName
     *            The name of the page.
     * @return An URI.
     * 
     * @since 2.0.3
     */
    public String getEditURL(String pageName) {
        URLConstructor u = BeanHolder.getURLConstructor();
        return u.makeURL(WikiContext.EDIT, pageName, false, null);
    }

    /**
     * Returns the basic attachment URL.Please use WikiContext.getURL() or
     * WikiEngine.getURL() instead.
     * 
     * @see #getURL(String, String, String, boolean)
     * @see WikiContext#getURL(String, String)
     * @since 2.0.42.
     * @param attName
     *            Attachment name
     * @deprecated
     * @return An URI.
     */
    public String getAttachmentURL(String attName) {
        URLConstructor u = BeanHolder.getURLConstructor();
        return u.makeURL(WikiContext.ATTACH, attName, false, null);
    }

    /**
     * Returns an URL if a WikiContext is not available.
     * 
     * @param context
     *            The WikiContext (VIEW, EDIT, etc...)
     * @param pageName
     *            Name of the page, as usual
     * @param params
     *            List of parameters. May be null, if no parameters.
     * @param absolute
     *            If true, will generate an absolute URL regardless of
     *            properties setting.
     * @return An URL (absolute or relative).
     */
    public String getURL(String context, String pageName, String params,
            boolean absolute) {
        if (pageName == null)
            pageName = getFrontPage();
        URLConstructor u = BeanHolder.getURLConstructor();
        return u.makeURL(context, pageName, absolute, params);
    }

    /**
     * Returns the default front page, if no page is used.
     * 
     * @return The front page name.
     */

    public String getFrontPage() {
        return m_frontPage;
    }

    /**
     * Returns the ServletContext that this particular WikiEngine was
     * initialized with. <B>It may return null</B>, if the WikiEngine is not
     * running inside a servlet container!
     * 
     * @since 1.7.10
     * @return ServletContext of the WikiEngine, or null.
     */

    public ServletContext getServletContext() {
        return null;
    }

    /**
     * This is a safe version of the Servlet.Request.getParameter() routine.
     * Unfortunately, the default version always assumes that the incoming
     * character set is ISO-8859-1, even though it was something else. This
     * means that we need to make a new string using the correct encoding.
     * <P>
     * For more information, see: <A
     * HREF="http://www.jguru.com/faq/view.jsp?EID=137049">JGuru FAQ</A>.
     * <P>
     * Incidentally, this is almost the same as encodeName(), below. I am not
     * yet entirely sure if it's safe to merge the code.
     * 
     * @param request
     *            The servlet request
     * @param name
     *            The parameter name to get.
     * @return The parameter value or null
     * @since 1.5.3
     * @deprecated JSPWiki now requires servlet API 2.3, which has a better way
     *             of dealing with this stuff. This will be removed in the near
     *             future.
     */

    public String safeGetParameter(ServletRequest request, String name) {
        try {
            String res = request.getParameter(name);
            if (res != null) {
                res = new String(res.getBytes("ISO-8859-1"),
                        getContentEncoding());
            }

            return res;
        } catch (UnsupportedEncodingException e) {
            log.fatal("Unsupported encoding", e);
            return "";
        }

    }

    /**
     * Returns the query string (the portion after the question mark).
     * 
     * @param request
     *            The HTTP request to parse.
     * @return The query string. If the query string is null, returns an empty
     *         string.
     * 
     * @since 2.1.3
     */
    public String safeGetQueryString(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        try {
            String res = request.getQueryString();
            if (res != null) {
                res = new String(res.getBytes("ISO-8859-1"),
                        getContentEncoding());

                //
                // Ensure that the 'page=xyz' attribute is removed
                // FIXME: Is it really the mandate of this routine to
                // do that?
                //
                int pos1 = res.indexOf("page=");
                if (pos1 >= 0) {
                    String tmpRes = res.substring(0, pos1);
                    int pos2 = res.indexOf("&", pos1) + 1;
                    if ((pos2 > 0) && (pos2 < res.length())) {
                        tmpRes = tmpRes + res.substring(pos2);
                    }
                    res = tmpRes;
                }
            }

            return res;
        } catch (UnsupportedEncodingException e) {
            log.fatal("Unsupported encoding", e);
            return "";
        }
    }

    /**
     * Returns an URL to some other Wiki that we know.
     * 
     * @param wikiName
     *            The name of the other wiki.
     * @return null, if no such reference was found.
     */
    public String getInterWikiURL(String wikiName) {
        return TextUtil.getStringProperty(m_properties, PROP_INTERWIKIREF
                + wikiName, null);
    }

    /**
     * Returns a collection of all supported InterWiki links.
     * 
     * @return A Collection of Strings.
     */
    public Collection getAllInterWikiLinks() {
        Vector<String> v = new Vector<String>();

        for (Enumeration i = m_properties.propertyNames(); i.hasMoreElements();) {
            String prop = (String) i.nextElement();

            if (prop.startsWith(PROP_INTERWIKIREF)) {
                v.add(prop.substring(prop.lastIndexOf(".") + 1));
            }
        }

        return v;
    }

    /**
     * Returns a collection of all image types that get inlined.
     * 
     * @return A Collection of Strings with a regexp pattern.
     */

    public Collection getAllInlinedImagePatterns() {
        return JSPWikiMarkupParser.getImagePatterns(this);
    }

    /**
     * <p>
     * If the page is a special page, then returns a direct URL to that page.
     * Otherwise returns <code>null</code>. This method delegates requests to
     * {@link org.apache.wiki.ui.CommandResolver#getSpecialPageReference(String)}
     * .
     * </p>
     * <p>
     * Special pages are defined in jspwiki.properties using the
     * jspwiki.specialPage setting. They're typically used to give Wiki page
     * names to e.g. custom JSP pages.
     * </p>
     * 
     * @param original
     *            The page to check
     * @return A reference to the page, or null, if there's no special page.
     */
    public String getSpecialPageReference(String original) {
        return m_commandResolver.getSpecialPageReference(original);
    }

    /**
     * Beautifies the title of the page by appending spaces in suitable places,
     * if the user has so decreed in the properties when constructing this
     * WikiEngine. However, attachment names are only beautified by the name.
     * 
     * @param title
     *            The title to beautify
     * @return A beautified title (or, if beautification is off, returns the
     *         title without modification)
     * @since 1.7.11
     */
    public String beautifyTitle(String title) {
        if (m_beautifyTitle) {
            try {
                Attachment att = BeanHolder.getAttachmentManager()
                        .getAttachmentInfo(title);

                if (att == null) {
                    return TextUtil.beautifyString(title);
                }

                String parent = TextUtil.beautifyString(att.getParentName());

                return parent + "/" + att.getFileName();
            } catch (ProviderException e) {
                return title;
            }
        }

        return title;
    }

    /**
     * Beautifies the title of the page by appending non-breaking spaces in
     * suitable places. This is really suitable only for HTML output, as it uses
     * the &amp;nbsp; -character.
     * 
     * @param title
     *            The title to beautify
     * @return A beautified title.
     * @since 2.1.127
     */
    public String beautifyTitleNoBreak(String title) {
        if (m_beautifyTitle) {
            return TextUtil.beautifyString(title, "&nbsp;");
        }

        return title;
    }

    /**
     * Returns true, if the requested page (or an alias) exists. Will consider
     * any version as existing. Will also consider attachments.
     * 
     * @param page
     *            WikiName of the page.
     * @return true, if page (or attachment) exists.
     */
    public boolean pageExists(String page) {
        Attachment att = null;

        try {
            if (m_commandResolver.getSpecialPageReference(page) != null)
                return true;

            if (getFinalPageName(page) != null) {
                return true;
            }

            att = BeanHolder.getAttachmentManager().getAttachmentInfo(
                    (WikiContext) null, page);
        } catch (ProviderException e) {
            log.debug("pageExists() failed to find attachments", e);
        }

        return att != null;
    }

    /**
     * Returns true, if the requested page (or an alias) exists with the
     * requested version.
     * 
     * @param page
     *            Page name
     * @param version
     *            Page version
     * @return True, if page (or alias, or attachment) exists
     * @throws ProviderException
     *             If the provider fails.
     */
    public boolean pageExists(String page, int version)
            throws ProviderException {
        if (m_commandResolver.getSpecialPageReference(page) != null)
            return true;

        String finalName = getFinalPageName(page);

        boolean isThere = false;

        if (finalName != null) {
            //
            // Go and check if this particular version of this page
            // exists.
            //
            isThere = BeanHolder.getPageManager()
                    .pageExists(finalName, version);
        }

        if (isThere == false) {
            //
            // Go check if such an attachment exists.
            //
            try {
                isThere = BeanHolder.getAttachmentManager().getAttachmentInfo(
                        (WikiContext) null, page, version) != null;
            } catch (ProviderException e) {
                log.debug("pageExists() failed to find attachments", e);
            }
        }

        return isThere;
    }

    /**
     * Returns true, if the requested page (or an alias) exists, with the
     * specified version in the WikiPage.
     * 
     * @param page
     *            A WikiPage object describing the name and version.
     * @return true, if the page (or alias, or attachment) exists.
     * @throws ProviderException
     *             If something goes badly wrong.
     * @since 2.0
     */
    public boolean pageExists(WikiPage page) throws ProviderException {
        if (page != null) {
            return pageExists(page.getName(), page.getVersion());
        }
        return false;
    }

    /**
     * Returns the correct page name, or null, if no such page can be found.
     * Aliases are considered. This method simply delegates to
     * {@link org.apache.wiki.ui.CommandResolver#getFinalPageName(String)}.
     * 
     * @since 2.0
     * @param page
     *            Page name.
     * @return The rewritten page name, or null, if the page does not exist.
     * @throws ProviderException
     *             If something goes wrong in the backend.
     */
    public String getFinalPageName(String page) throws ProviderException {
        return m_commandResolver.getFinalPageName(page);
    }

    /**
     * Turns a WikiName into something that can be called through using an URL.
     * 
     * @since 1.4.1
     * @param pagename
     *            A name. Can be actually any string.
     * @return A properly encoded name.
     * @see #decodeName(String)
     */
    public String encodeName(String pagename) {
        try {
            return URLEncoder.encode(pagename, m_useUTF8 ? "UTF-8"
                    : "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new InternalWikiException(
                    "ISO-8859-1 not a supported encoding!?!  Your platform is borked.");
        }
    }

    /**
     * Decodes a URL-encoded request back to regular life. This properly heeds
     * the encoding as defined in the settings file.
     * 
     * @param pagerequest
     *            The URL-encoded string to decode
     * @return A decoded string.
     * @see #encodeName(String)
     */
    public String decodeName(String pagerequest) {
        try {
            return URLDecoder.decode(pagerequest, m_useUTF8 ? "UTF-8"
                    : "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new InternalWikiException(
                    "ISO-8859-1 not a supported encoding!?!  Your platform is borked.");
        }
    }

    /**
     * Returns the IANA name of the character set encoding we're supposed to be
     * using right now.
     * 
     * @since 1.5.3
     * @return The content encoding (either UTF-8 or ISO-8859-1).
     */
    public String getContentEncoding() {
        if (m_useUTF8)
            return "UTF-8";

        return "ISO-8859-1";
    }

    /**
     * Returns the {@link org.apache.wiki.workflow.WorkflowManager} associated
     * with this WikiEngine. If the WIkiEngine has not been initialized, this
     * method will return <code>null</code>.
     * 
     * @return the task queue
     */
    public WorkflowManager getWorkflowManager() {
        return m_workflowMgr;
    }

    /**
     * Returns the un-HTMLized text of the latest version of a page. This method
     * also replaces the &lt; and &amp; -characters with their respective HTML
     * entities, thus making it suitable for inclusion on an HTML page. If you
     * want to have the page text without any conversions, use getPureText().
     * 
     * @param page
     *            WikiName of the page to fetch.
     * @return WikiText.
     */
    public String getText(String page) {
        return getText(page, WikiPageProvider.LATEST_VERSION);
    }

    /**
     * Returns the un-HTMLized text of the given version of a page. This method
     * also replaces the &lt; and &amp; -characters with their respective HTML
     * entities, thus making it suitable for inclusion on an HTML page. If you
     * want to have the page text without any conversions, use getPureText().
     * 
     * 
     * @param page
     *            WikiName of the page to fetch
     * @param version
     *            Version of the page to fetch
     * @return WikiText.
     */
    public String getText(String page, int version) {
        String result = getPureText(page, version);

        result = TextUtil.replaceEntities(result);

        return result;
    }

    /**
     * Returns the un-HTMLized text of the given version of a page in the given
     * context. USE THIS METHOD if you don't know what doing.
     * <p>
     * This method also replaces the &lt; and &amp; -characters with their
     * respective HTML entities, thus making it suitable for inclusion on an
     * HTML page. If you want to have the page text without any conversions, use
     * getPureText().
     * 
     * @since 1.9.15.
     * @param context
     *            The WikiContext
     * @param page
     *            A page reference (not an attachment)
     * @return The page content as HTMLized String.
     * @see #getPureText(WikiPage)
     */
    public String getText(WikiContext context, WikiPage page) {
        return getText(page.getName(), page.getVersion());
    }

    /**
     * Returns the pure text of a page, no conversions. Use this if you are
     * writing something that depends on the parsing of the page. Note that you
     * should always check for page existence through pageExists() before
     * attempting to fetch the page contents.
     * 
     * @param page
     *            The name of the page to fetch.
     * @param version
     *            If WikiPageProvider.LATEST_VERSION, then uses the latest
     *            version.
     * @return The page contents. If the page does not exist, returns an empty
     *         string.
     */
    // FIXME: Should throw an exception on unknown page/version?
    public String getPureText(String page, int version) {
        String result = null;

        try {
            result = BeanHolder.getPageManager().getPageText(page, version);
        } catch (ProviderException e) {
            // FIXME
        } finally {
            if (result == null)
                result = "";
        }

        return result;
    }

    /**
     * Returns the pure text of a page, no conversions. Use this if you are
     * writing something that depends on the parsing the page. Note that you
     * should always check for page existence through pageExists() before
     * attempting to fetch the page contents.
     * 
     * @param page
     *            A handle to the WikiPage
     * @return String of WikiText.
     * @since 2.1.13.
     */
    public String getPureText(WikiPage page) {
        return getPureText(page.getName(), page.getVersion());
    }

    /**
     * Returns the converted HTML of the page using a different context than the
     * default context.
     * 
     * @param context
     *            A WikiContext in which you wish to render this page in.
     * @param page
     *            WikiPage reference.
     * @return HTML-rendered version of the page.
     */

    public String getHTML(WikiContext context, WikiPage page) {
        String pagedata = null;

        pagedata = getPureText(page.getName(), page.getVersion());

        String res = TextToHtml.textToHTML(context, pagedata);

        return res;
    }

    /**
     * Returns the converted HTML of the page.
     * 
     * @param page
     *            WikiName of the page to convert.
     * @return HTML-rendered version of the page.
     */
    public String getHTML(String page) {
        return getHTML(page, WikiPageProvider.LATEST_VERSION);
    }

    /**
     * Returns the converted HTML of the page's specific version. The version
     * must be a positive integer, otherwise the current version is returned.
     * 
     * @param pagename
     *            WikiName of the page to convert.
     * @param version
     *            Version number to fetch
     * @return HTML-rendered page text.
     */
    public String getHTML(String pagename, int version) {
        WikiPage page = getPage(pagename, version);

        WikiContext context = new WikiContext(this, page);
        context.setRequestContext(WikiContext.NONE);

        String res = getHTML(context, page);

        return res;
    }

    /**
     * Protected method that signals that the WikiEngine will be shut down by
     * the servlet container. It is called by {@link WikiServlet#destroy()}.
     * When this method is called, it fires a "shutdown" WikiEngineEvent to all
     * registered listeners.
     */
    protected void shutdown() {
        fireEvent(WikiEngineEvent.SHUTDOWN);
        BeanHolder.getFilterManager().destroy();
    }

    /**
     * Just convert WikiText to HTML.
     * 
     * @param context
     *            The WikiContext in which to do the conversion
     * @param pagedata
     *            The data to render
     * @param localLinkHook
     *            Is called whenever a wiki link is found
     * @param extLinkHook
     *            Is called whenever an external link is found
     * 
     * @return HTML-rendered page text.
     */

    public String textToHTML(WikiContext context, String pagedata,
            StringTransmutator localLinkHook, StringTransmutator extLinkHook) {
        FilterManager fManager = BeanHolder.getFilterManager();
        return TextToHtml.textToHTML(context, fManager, pagedata,
                localLinkHook, extLinkHook, null, true, false);
    }

    /**
     * Just convert WikiText to HTML.
     * 
     * @param context
     *            The WikiContext in which to do the conversion
     * @param pagedata
     *            The data to render
     * @param localLinkHook
     *            Is called whenever a wiki link is found
     * @param extLinkHook
     *            Is called whenever an external link is found
     * @param attLinkHook
     *            Is called whenever an attachment link is found
     * @return HTML-rendered page text.
     */

    public String textToHTML(WikiContext context, String pagedata,
            StringTransmutator localLinkHook, StringTransmutator extLinkHook,
            StringTransmutator attLinkHook) {
        FilterManager fManager = BeanHolder.getFilterManager();
        return TextToHtml.textToHTML(context, fManager, pagedata,
                localLinkHook, extLinkHook, attLinkHook, true, false);
    }

    /**
     * Updates all references for the given page.
     * 
     * @param page
     *            wiki page for which references should be updated
     */
    public void updateReferences(WikiPage page) {
        String pageData = getPureText(page.getName(),
                WikiProvider.LATEST_VERSION);
        ReferenceManager rManager = BeanHolder.getReferenceManager();
        FilterManager fManager = BeanHolder.getFilterManager();
        RenderingManager reManager = BeanHolder.getRenderingManager();

        rManager.updateReferences(page.getName(), TextToHtml.scanWikiLinks(
                this, fManager, reManager, page, pageData));
    }

    /**
     * Writes the WikiText of a page into the page repository. If the
     * <code>jspwiki.properties</code> file contains the property
     * <code>jspwiki.approver.workflow.saveWikiPage</code> and its value
     * resolves to a valid user, {@link org.apache.wiki.auth.authorize.Group} or
     * {@link org.apache.wiki.auth.authorize.Role}, this method will place a
     * {@link org.apache.wiki.workflow.Decision} in the approver's workflow
     * inbox and throw a
     * {@link org.apache.wiki.workflow.DecisionRequiredException}. If the
     * submitting user is authenticated and the page save is rejected, a
     * notification will be placed in the user's decision queue.
     * 
     * @since 2.1.28
     * @param context
     *            The current WikiContext
     * @param text
     *            The Wiki markup for the page.
     * @throws WikiException
     *             if the save operation encounters an error during the save
     *             operation. If the page-save operation requires approval, the
     *             exception will be of type
     *             {@link org.apache.wiki.workflow.DecisionRequiredException}.
     *             Individual PageFilters, such as the
     *             {@link org.apache.wiki.filters.SpamFilter} may also throw a
     *             {@link org.apache.wiki.filters.RedirectException}.
     */
    public void saveText(WikiContext context, String text) throws WikiException {
        // Check if page data actually changed; bail if not
        DifferenceManager diff = BeanHolder.getDifferenceManager();
        WikiPage page = context.getPage();
        String oldText = getPureText(page);
        String proposedText = TextUtil.normalizePostData(text);
        if (oldText != null && oldText.equals(proposedText)) {
            return;
        }

        // Check if creation of empty pages is allowed; bail if not
        boolean allowEmpty = TextUtil.getBooleanProperty(m_properties,
                PROP_ALLOW_CREATION_OF_EMPTY_PAGES, false);
        if (!allowEmpty && !pageExists(page) && text.trim().equals("")) {
            return;
        }

        // Create approval workflow for page save; add the diffed, proposed
        // and old text versions as Facts for the approver (if approval is
        // required)
        // If submitter is authenticated, any reject messages will appear in
        // his/her workflow inbox.
        WorkflowBuilder builder = WorkflowBuilder.getBuilder(this);
        Principal submitter = context.getCurrentUser();
        Task prepTask = new PageManager.PreSaveWikiPageTask(context,
                proposedText);
        Task completionTask = new PageManager.SaveWikiPageTask();
        String diffText = diff.makeDiff(context, oldText, proposedText);
        boolean isAuthenticated = context.getWikiSession().isAuthenticated();
        Fact[] facts = new Fact[5];
        facts[0] = new Fact(PageManager.FACT_PAGE_NAME, page.getName());
        facts[1] = new Fact(PageManager.FACT_DIFF_TEXT, diffText);
        facts[2] = new Fact(PageManager.FACT_PROPOSED_TEXT, proposedText);
        facts[3] = new Fact(PageManager.FACT_CURRENT_TEXT, oldText);
        facts[4] = new Fact(PageManager.FACT_IS_AUTHENTICATED,
                Boolean.valueOf(isAuthenticated));
        String rejectKey = isAuthenticated ? PageManager.SAVE_REJECT_MESSAGE_KEY
                : null;
        Workflow workflow = builder.buildApprovalWorkflow(submitter,
                PageManager.SAVE_APPROVER, prepTask,
                PageManager.SAVE_DECISION_MESSAGE_KEY, facts, completionTask,
                rejectKey);
        m_workflowMgr.start(workflow);

        // Let callers know if the page-save requires approval
        if (workflow.getCurrentStep() instanceof Decision) {
            throw new DecisionRequiredException(
                    "The page contents must be approved before they become active.");
        }
    }

    /**
     * Returns the number of pages in this Wiki
     * 
     * @return The total number of pages.
     */
    public int getPageCount() {
        return BeanHolder.getPageManager().getTotalPageCount();
    }

    /**
     * Return information about current provider. This method just calls the
     * corresponding PageManager method, which in turn calls the provider
     * method.
     * 
     * @return A textual description of the current provider.
     * @since 1.6.4
     */
    public String getCurrentProviderInfo() {
        return BeanHolder.getPageManager().getProviderDescription();
    }

    /**
     * Returns a Collection of WikiPages, sorted in time order of last change
     * (i.e. first object is the most recently changed). This method also
     * includes attachments.
     * 
     * @return Collection of WikiPage objects. In reality, the returned
     *         collection is a Set, but due to API compatibility reasons, we're
     *         not changing the signature soon...
     */

    // FIXME: Should really get a Date object and do proper comparisons.
    // This is terribly wasteful.
    @SuppressWarnings("unchecked")
    public Collection getRecentChanges() {
        try {
            Collection<WikiPage> pages = BeanHolder.getPageManager()
                    .getAllPages();
            Collection<Attachment> atts = BeanHolder.getAttachmentManager()
                    .getAllAttachments();

            TreeSet<WikiPage> sortedPages = new TreeSet<WikiPage>(
                    new PageTimeComparator());

            sortedPages.addAll(pages);
            sortedPages.addAll(atts);

            return sortedPages;
        } catch (ProviderException e) {
            log.error("Unable to fetch all pages: ", e);
            return null;
        }
    }

    /**
     * Parses an incoming search request, then does a search.
     * <P>
     * The query is dependent on the actual chosen search provider - each one of
     * them has a language of its own.
     * 
     * @param query
     *            The query string
     * @return A Collection of SearchResult objects.
     * @throws ProviderException
     *             If the searching failed
     * @throws IOException
     *             If the searching failed
     */

    //
    // FIXME: Should also have attributes attached.
    //
    public Collection findPages(String query) throws ProviderException,
            IOException {
        Collection results = BeanHolder.getSearchManager().findPages(query);

        return results;
    }

    /**
     * Finds the corresponding WikiPage object based on the page name. It always
     * finds the latest version of a page.
     * 
     * @param pagereq
     *            The name of the page to look for.
     * @return A WikiPage object, or null, if the page by the name could not be
     *         found.
     */

    public WikiPage getPage(String pagereq) {
        return getPage(pagereq, WikiProvider.LATEST_VERSION);
    }

    /**
     * Finds the corresponding WikiPage object base on the page name and
     * version.
     * 
     * @param pagereq
     *            The name of the page to look for.
     * @param version
     *            The version number to look for. May be
     *            WikiProvider.LATEST_VERSION, in which case it will look for
     *            the latest version (and this method then becomes the
     *            equivalent of getPage(String).
     * 
     * @return A WikiPage object, or null, if the page could not be found; or if
     *         there is no such version of the page.
     * @since 1.6.7.
     */

    public WikiPage getPage(String pagereq, int version) {
        try {
            WikiPage p = BeanHolder.getPageManager().getPageInfo(pagereq,
                    version);

            if (p == null) {
                p = BeanHolder.getAttachmentManager().getAttachmentInfo(
                        (WikiContext) null, pagereq);
            }

            return p;
        } catch (ProviderException e) {
            log.error("Unable to fetch page info", e);
            return null;
        }
    }

    /**
     * Returns a Collection of WikiPages containing the version history of a
     * page.
     * 
     * @param page
     *            Name of the page to look for
     * @return an ordered List of WikiPages, each corresponding to a different
     *         revision of the page.
     */

    public List getVersionHistory(String page) {
        List c = null;

        try {
            c = BeanHolder.getPageManager().getVersionHistory(page);

            if (c == null) {
                c = BeanHolder.getAttachmentManager().getVersionHistory(page);
            }
        } catch (ProviderException e) {
            log.error("FIXME");
        }

        return c;
    }

    /**
     * Returns a diff of two versions of a page.
     * <p>
     * Note that the API was changed in 2.6 to provide a WikiContext object!
     * 
     * @param context
     *            The WikiContext of the page you wish to get a diff from
     * @param version1
     *            Version number of the old page. If
     *            WikiPageProvider.LATEST_VERSION (-1), then uses current page.
     * @param version2
     *            Version number of the new page. If
     *            WikiPageProvider.LATEST_VERSION (-1), then uses current page.
     * 
     * @return A HTML-ized difference between two pages. If there is no
     *         difference, returns an empty string.
     */
    public String getDiff(WikiContext context, int version1, int version2) {
        String page = context.getPage().getName();
        String page1 = getPureText(page, version1);
        String page2 = getPureText(page, version2);

        // Kludge to make diffs for new pages to work this way.

        if (version1 == WikiPageProvider.LATEST_VERSION) {
            page1 = "";
        }
        DifferenceManager m_diff = BeanHolder.getDifferenceManager();

        String diff = m_diff.makeDiff(context, page1, page2);

        return diff;
    }

    /**
     * Shortcut to getVariableManager().getValue(). However, this method does
     * not throw a NoSuchVariableException, but returns null in case the
     * variable does not exist.
     * 
     * @param context
     *            WikiContext to look the variable in
     * @param name
     *            Name of the variable to look for
     * @return Variable value, or null, if there is no such variable.
     * @since 2.2
     */
    public String getVariable(WikiContext context, String name) {
        try {
            VariableManager m_variableManager = BeanHolder.getVariableManager();
            return m_variableManager.getValue(context, name);
        } catch (NoSuchVariableException e) {
            return null;
        }
    }

    /**
     * Returns the CommandResolver for this wiki engine.
     * 
     * @return the resolver
     */
    public CommandResolver getCommandResolver() {
        return m_commandResolver;
    }

    /**
     * Figure out to which page we are really going to. Considers special page
     * names from the jspwiki.properties, and possible aliases. This method
     * delgates requests to {@link org.apache.wiki.WikiContext#getRedirectURL()}
     * .
     * 
     * @param context
     *            The Wiki Context in which the request is being made.
     * @return A complete URL to the new page to redirect to
     * @since 2.2
     */

    public String getRedirectURL(WikiContext context) {
        return context.getRedirectURL();
    }

    /**
     * Shortcut to create a WikiContext from a supplied HTTP request, using a
     * default wiki context.
     * 
     * @param request
     *            the HTTP request
     * @param requestContext
     *            the default context to use
     * @return a new WikiContext object.
     * 
     * @see org.apache.wiki.ui.CommandResolver
     * @see org.apache.wiki.ui.Command
     * @since 2.1.15.
     */
    // FIXME: We need to have a version which takes a fixed page
    // name as well, or check it elsewhere.
    public WikiContext createContext(HttpServletRequest request,
            String requestContext) {

        if (!m_isConfigured) {
            throw new InternalWikiException(
                    "WikiEngine has not been properly started.  It is likely that the configuration is faulty.  Please check all logs for the possible reason.");
        }

        // Build the wiki context
        Command command = m_commandResolver
                .findCommand(request, requestContext);
        return new WikiContext(this, request, command);
    }

    /**
     * Deletes a page or an attachment completely, including all versions. If
     * the page does not exist, does nothing.
     * 
     * @param pageName
     *            The name of the page.
     * @throws ProviderException
     *             If something goes wrong.
     */
    public void deletePage(String pageName) throws ProviderException {
        WikiPage p = getPage(pageName);
        AttachmentManager aManager = BeanHolder.getAttachmentManager();

        if (p != null) {
            if (p instanceof Attachment) {
                aManager.deleteAttachment((Attachment) p);
            } else {
                if (aManager.hasAttachments(p)) {
                    Collection attachments = aManager.listAttachments(p);
                    for (Iterator atti = attachments.iterator(); atti.hasNext();) {
                        aManager.deleteAttachment((Attachment) (atti.next()));
                    }
                }
                BeanHolder.getPageManager().deletePage(p);
            }
        }
    }

    /**
     * Deletes a specific version of a page or an attachment.
     * 
     * @param page
     *            The page object.
     * @throws ProviderException
     *             If something goes wrong.
     */
    public void deleteVersion(WikiPage page) throws ProviderException {
        if (page instanceof Attachment) {
            BeanHolder.getAttachmentManager().deleteVersion((Attachment) page);
        } else {
            BeanHolder.getPageManager().deleteVersion(page);
        }
    }

    /**
     * Returns the URL of the global RSS file. May be null, if the RSS file
     * generation is not operational.
     * 
     * @since 1.7.10
     * @return The global RSS url
     */
    public String getGlobalRSSURL() {
        if (m_rssGenerator != null && m_rssGenerator.isEnabled()) {
            return getBaseURL() + m_rssFile;
        }

        return null;
    }

    /**
     * Returns the RSSGenerator. If the property
     * <code>jspwiki.rss.generate</code> has not been set to <code>true</code>,
     * this method will return <code>null</code>,
     * <em>and callers should check for this value.</em>
     * 
     * @since 2.1.165
     * @return the RSS generator
     */
    public RSSGenerator getRSSGenerator() {
        return m_rssGenerator;
    }

    /**
     * Returns the AclManager employed by this WikiEngine. The AclManager is
     * lazily initialized.
     * <p>
     * The AclManager implementing class may be set by the System property
     * {@link #PROP_ACL_MANAGER_IMPL}.
     * </p>
     * 
     * @since 2.3
     * @return The current AclManager.
     */
    public AclManager getAclManager() {
        if (m_aclManager == null) {
            try {
                String s = m_properties.getProperty(PROP_ACL_MANAGER_IMPL,
                        DefaultAclManager.class.getName());
                m_aclManager = (AclManager) ClassUtil.getMappedObject(s);
                // TODO:
                // I
                // am
                // not
                // sure
                // whether
                // this
                // is
                // the
                // right
                // call
                m_aclManager.initialize(this, m_properties);
            } catch (WikiException we) {
                log.fatal("unable to instantiate class for AclManager: "
                        + we.getMessage());
                throw new InternalWikiException(
                        "Cannot instantiate AclManager, please check logs.");
            }
        }
        return m_aclManager;
    }

    /**
     * Returns the current i18n manager.
     * 
     * @return The current Intertan... Interante... Internatatializ... Whatever.
     */
    public InternationalizationManager getInternationalizationManager() {
        return m_internationalizationManager;
    }

    /**
     * Registers a WikiEventListener with this instance.
     * 
     * @param listener
     *            the event listener
     */
    public final synchronized void addWikiEventListener(
            WikiEventListener listener) {
        WikiEventManager.addWikiEventListener(this, listener);
    }

    /**
     * Un-registers a WikiEventListener with this instance.
     * 
     * @param listener
     *            the event listener
     */
    public final synchronized void removeWikiEventListener(
            WikiEventListener listener) {
        WikiEventManager.removeWikiEventListener(this, listener);
    }

    /**
     * Fires a WikiEngineEvent to all registered listeners.
     * 
     * @param type
     *            the event type
     */
    protected final void fireEvent(int type) {
        if (WikiEventManager.isListening(this)) {
            WikiEventManager.fireEvent(this, new WikiEngineEvent(this, type));
        }
    }

    /**
     * Adds an attribute to the engine for the duration of this engine. The
     * value is not persisted.
     * 
     * @since 2.4.91
     * @param key
     *            the attribute name
     * @param value
     *            the value
     */
    public void setAttribute(String key, Object value) {
        m_attributes.put(key, value);
    }

    /**
     * Gets an attribute from the engine.
     * 
     * @param key
     *            the attribute name
     * @return the value
     */
    public Object getAttribute(String key) {
        return m_attributes.get(key);
    }

    /**
     * Removes an attribute.
     * 
     * @param key
     *            The key of the attribute to remove.
     * @return The previous attribute, if it existed.
     */
    public Object removeAttribute(String key) {
        return m_attributes.remove(key);
    }

    /**
     * Returns a WatchDog for current thread.
     * 
     * @return The current thread WatchDog.
     * @since 2.4.92
     */
    public WatchDog getCurrentWatchDog() {
        return WatchDog.getCurrentWatchDog(this);
    }

}
