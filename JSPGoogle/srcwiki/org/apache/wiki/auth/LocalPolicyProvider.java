package org.apache.wiki.auth;

import java.io.File;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.AbstractWikiProvider;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiException;
import org.freshcookies.security.policy.LocalPolicy;
import org.freshcookies.security.policy.PolicyException;

public class LocalPolicyProvider extends AbstractWikiProvider {

	private final static Log log = LogFactory.getLog(LocalPolicyProvider.class);
	
    /** Property that supplies the security policy file name, in WEB-INF. */
	private static final String POLICY = "jspwiki.policy.file";

	/** Name of the default security policy file, in WEB-INF. */
	private static final String DEFAULT_POLICY = "jspwiki.policy";

	private LocalPolicy lPolicy = null;

	public LocalPolicyProvider(WikiEngine engine) throws WikiException {
		initialize(engine, engine.getWikiProperties());
	}

	public static URL getPolicyURL(WikiEngine engine) {
		String policyFileName = engine.getWikiProperties().getProperty(POLICY,
				DEFAULT_POLICY);
		URL policyURL = ResourceUtil.findConfigFile(engine, policyFileName);
		return policyURL;
	}

	private void setLocalPolicy() {
		if (lPolicy != null) {
			return;
		}
		URL policyURL = getPolicyURL(m_engine);
		File policyFile;
		String encode;

		if (policyURL != null) {
			policyFile = new File(policyURL.getPath());
			log.info("We found security policy URL: " + policyURL
					+ " and transformed it to file "
					+ policyFile.getAbsolutePath());
			encode = m_engine.getContentEncoding();
			log.info("Initialized default security policy: "
					+ policyFile.getAbsolutePath());
		} else {
			StringBuffer sb = new StringBuffer(
					"JSPWiki was unable to initialize the ");
			sb.append("default security policy (WEB-INF/jspwiki.policy) file. ");
			sb.append("Please ensure that the jspwiki.policy file exists in the default location. ");
			sb.append("This file should exist regardless of the existance of a global policy file. ");
			sb.append("The global policy file is identified by the java.security.policy variable. ");
			RuntimeException wse = new RuntimeException(sb.toString());
			log.fatal(sb.toString(), wse);
			throw wse;
		}

		lPolicy = new LocalPolicy(policyFile, encode);
		try {
			lPolicy.refresh();
		} catch (PolicyException e) {
			log.error("Local policy", e);
		}
	}

	public LocalPolicy getLocalPolicy() {
		setLocalPolicy();
		return lPolicy;
	}

}
