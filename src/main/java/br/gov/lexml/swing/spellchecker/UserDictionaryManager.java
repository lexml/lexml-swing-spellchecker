package br.gov.lexml.swing.spellchecker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Administra os dicionários de usuário (palavras ignoradas e palavras adicionadas)
 */
class UserDictionaryManager {
	
	private List<SpellcheckManager> spellcheckManagers = new ArrayList<SpellcheckManager>();
	
	private Spellchecker spellchecker;
	private UserDictionary udIgnoradas;
	private UserDictionary udAdicionadas;

	UserDictionaryManager(File baseDir) throws IOException, SpellcheckerInitializationException {
		spellchecker = SpellcheckerFactory.getInstance().createSpellchecker(baseDir);
		udIgnoradas = new UserDictionary(baseDir, "palavrasIgnoradas");
		udAdicionadas = new UserDictionary(baseDir, "palavrasAdicionadas");
		for(String w: udAdicionadas.getWords()) {
			spellchecker.addToRuntimeDictionary(w);
		}
	}
	
	public void register(SpellcheckManager sm) {
		if(!spellcheckManagers.contains(sm)) {
			spellcheckManagers.add(sm);
		}
	}
	
	public void ignoreWord(String s) throws IOException {
		addWord(s, true);
	}

	public void addWord(String s) throws IOException {
		addWord(s, false);
	}
	
	private void addWord(String w, boolean ignoreOnly) throws IOException {
		if(ignoreOnly) {
			udIgnoradas.addWord(w);
		}
		else {
			udAdicionadas.addWord(w);
			spellchecker.addToRuntimeDictionary(w.trim().toLowerCase());
		}
		for(SpellcheckManager s: spellcheckManagers) {
			s.removeHighlight(w);
		}
	}

	public boolean isIgnored(String word) {
		return udIgnoradas.contains(word);
	}
	
}
