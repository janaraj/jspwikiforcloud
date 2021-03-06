package org.apache.wiki.providers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wiki.AbstractWikiProvider;
import org.apache.wiki.IObjectPersist;

import xjava.io.FileOutputStream;

@SuppressWarnings("serial")
public class FileObjectPersist extends AbstractWikiProvider implements IObjectPersist {

	private static Log log = LogFactory.getLog(FileObjectPersist.class);

	@Override
	public ObjectInputStream constructInput(String pDir, String pName) {
		File f;
		
		if (pDir == null) {
			f = new File(m_engine.getWorkDir(), pName);
		} else {
			File fDir = new File(m_engine.getWorkDir(), pName);
			f = new File(fDir, pName);
		}

		if (!f.exists()) {
			return null;
		}

		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new BufferedInputStream(
					new FileInputStream(f)));
		} catch (FileNotFoundException e) {
			log.debug(f.getAbsolutePath(), e);
		} catch (IOException e) {
			log.debug(f.getAbsolutePath(), e);
		}
		return in;
	}

	@Override
	public ObjectOutputStream constructOutput(String pDir, String pName,
			boolean delete) {
		File f;
		if (pDir == null) {
			f = new File(m_engine.getWorkDir(), pName);
		} else {
			File fDir = new File(m_engine.getWorkDir(), pName);
			if (!fDir.exists()) {
				fDir.mkdirs();
			}
			f = new File(fDir, pName);
		}
		if (delete) {
			f.delete();
			return null;
		}

		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(f)));
		} catch (FileNotFoundException e) {
			log.debug(f.getAbsolutePath(), e);
		} catch (IOException e) {
			log.debug(f.getAbsolutePath(), e);
		}
		return out;
	}

}
