package br.gov.lexml.swing.spellchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import dk.dren.hunspell.Hunspell;
import dk.dren.hunspell.Hunspell.Dictionary;

/**
 * Verificador de ortografia.
 * 
 * <p>
 * Delega a funções de verificação de ortografia para a classe dk.dren.hunspell.HunspellDictionary
 * que é uma interface JNA para o Hunspell.
 * </p>
 */
public class Spellchecker {

	private Dictionary d;

	Spellchecker(File baseDir, String dictName)
			throws FileNotFoundException, UnsupportedEncodingException,
			UnsatisfiedLinkError, UnsupportedOperationException {
		File baseFileName = new File(baseDir, dictName);
		d = Hunspell.getInstance().getDictionary(baseFileName.getPath());
	}

	public boolean misspelled(String word) {
		return d.misspelled(word);
	}

	public List<String> suggest(String word) {
		return d.suggest(word);
	}
	
	public boolean addToRuntimeDictionary(String word) {
		return d.addToRuntimeDictionary(word);
	}

	public void addWordsToRuntimeDictionary(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		String line;
		while((line = reader.readLine()) != null) {
			line = line.trim().toLowerCase();
			if(!StringUtils.isEmpty(line)) {
				addToRuntimeDictionary(line);
			}
		}
		reader.close();
	}

}
