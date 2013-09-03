package br.gov.lexml.swing.spellchecker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class UserDictionaryManagerFactory {

	private static final UserDictionaryManagerFactory instance = new UserDictionaryManagerFactory();

	private Map<File, UserDictionaryManager> cache = new HashMap<File, UserDictionaryManager>();

	private UserDictionaryManagerFactory() {
		//
	}

	public static UserDictionaryManagerFactory getInstance() {
		return instance;
	}

	public synchronized UserDictionaryManager createLocalDictionaryManager(File baseDir) throws SpellcheckerInitializationException {

		if (!baseDir.isDirectory()) {
			throw new RuntimeException("Diretório " + baseDir.getPath()
					+ " não encontrado.");
		}

		UserDictionaryManager ldm = cache.get(baseDir);
		if (ldm == null) {
			try {
				ldm = new UserDictionaryManager(baseDir);
			} catch (IOException e) {
				throw new RuntimeException("Falha na criação dos dicionários locais.");
			}
			cache.put(baseDir, ldm);
		}

		return ldm;
	}

}
