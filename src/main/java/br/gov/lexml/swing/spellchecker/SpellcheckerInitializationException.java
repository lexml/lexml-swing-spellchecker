package br.gov.lexml.swing.spellchecker;

@SuppressWarnings("serial")
public class SpellcheckerInitializationException extends Exception {
	
	public SpellcheckerInitializationException(String message) {
		super(message);
	}

	public SpellcheckerInitializationException(String message, Exception e) {
		super(message, e);
	}

	public SpellcheckerInitializationException(Exception e) {
		super("Falha na inicialização do corretor ortográfico.", e);
	}

}
