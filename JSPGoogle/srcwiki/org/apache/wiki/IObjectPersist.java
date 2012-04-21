package org.apache.wiki;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface IObjectPersist extends WikiProvider {

	ObjectInputStream constructInput(String pDir,
			String pName);

	ObjectOutputStream constructOutput(String pDir,
			String pName, boolean delete);
	
}
