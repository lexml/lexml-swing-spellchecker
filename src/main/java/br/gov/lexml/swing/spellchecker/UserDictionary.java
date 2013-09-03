package br.gov.lexml.swing.spellchecker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Mantém uma lista de palavras representando um dicionário pessoal em um diretório do usuário.
 */
class UserDictionary {
	
	private File f;
	private List<String> words = new ArrayList<String>();
	
	UserDictionary(File baseDir, String fileNamePrefix) throws IOException {
		f = new File(baseDir, fileNamePrefix + ".txt");
		if(f.isFile()) {
			loadWords();
		}
	}

	public void addWord(String word) throws IOException {
		word = word.trim().toLowerCase();
		if(!StringUtils.isEmpty(word) && !contains(word)) {
			words.add(word);
		}
		saveWords();
	}

	public List<String> getWords() {
		return words;
	}
	
	private void loadWords() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String w;
		while((w = reader.readLine()) != null) {
			w = w.trim().toLowerCase();
			if(!StringUtils.isEmpty(w)) {
				words.add(w);
			}
		}
		reader.close();
	}
	
	private void saveWords() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		for(String w: words) {
			writer.write(w);
			writer.write("\n");
		}
		writer.flush();
		writer.close();
	}

	public boolean contains(String word) {
		return words.contains(word.trim().toLowerCase());
	}

}

