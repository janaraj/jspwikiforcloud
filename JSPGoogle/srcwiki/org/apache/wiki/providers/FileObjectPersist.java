package org.apache.wiki.providers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.wiki.IObjectPersist;
import org.apache.wiki.NoRequiredPropertyException;
import org.apache.wiki.WikiEngine;

import xjava.io.FileOutputStream;

@SuppressWarnings("serial")
public class FileObjectPersist implements IObjectPersist {

	private static Logger log = Logger.getLogger(FileObjectPersist.class);

	private WikiEngine m_engine = null;

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

	@Override
	public void initialize(WikiEngine engine, Properties properties)
			throws NoRequiredPropertyException, IOException {
		this.m_engine = engine;
	}

	@Override
	public String getProviderInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
