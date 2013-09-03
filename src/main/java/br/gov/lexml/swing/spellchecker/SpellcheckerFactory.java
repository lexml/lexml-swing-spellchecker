package br.gov.lexml.swing.spellchecker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * Cria um Spellchecker com dicionário em português do Brasil e palavras extra em /dict/outrosTermosComuns.txt
 */
public class SpellcheckerFactory {

	private static final SpellcheckerFactory instance = new SpellcheckerFactory();

	private static final String DICT_PT_BR = "pt_BR";

	private Map<File, Spellchecker> cache = new HashMap<File, Spellchecker>();

	private SpellcheckerFactory() {
		//
	}

	public static SpellcheckerFactory getInstance() {
		return instance;
	}

	public synchronized Spellchecker createSpellchecker(File baseDir) throws SpellcheckerInitializationException {

		if (!baseDir.isDirectory()) {
			throw new SpellcheckerInitializationException("Diretório " + baseDir.getPath()
					+ " não encontrado.");
		}

		Spellchecker s = cache.get(baseDir);
		if (s == null) {
			checkDictionary(baseDir, DICT_PT_BR);
			try {
				s = new Spellchecker(baseDir, DICT_PT_BR);
				s.addWordsToRuntimeDictionary(getClass().getResourceAsStream("/dict/outrosTermosComuns.txt"));
			} catch (Exception e) {
				throw new SpellcheckerInitializationException(e);
			}
			cache.put(baseDir, s);
		}

		return s;
	}

	private void checkDictionary(File baseDir, String dictName) throws SpellcheckerInitializationException {
		checkDictFile(baseDir, dictName, ".aff");
		checkDictFile(baseDir, dictName, ".dic");
	}

	private void checkDictFile(File baseDir, String dictName, String extension) throws SpellcheckerInitializationException {
		File f = new File(baseDir, dictName + extension);
		if (!f.exists()) {
			InputStream is = getClass().getResourceAsStream(
					"/dict/" + dictName + extension);
			if (is != null) {
				try {
					IOUtils.copy(is, new FileOutputStream(f));
				} catch (Exception e) {
					throw new SpellcheckerInitializationException(
							"Falha ao copiar o dicionário para a pasta "
									+ baseDir.getPath(), e);
				}
			} else {
				throw new SpellcheckerInitializationException("Dicionário " + f.getPath()
						+ " não encontrado.");
			}
		}
	}

}
