
package br.gov.lexml.swing.spellchecker;

import java.util.List;

import javax.swing.JTextPane;

public interface SpellcheckGroupManager {

	public JTextPane findNextJTextPane(JTextPane componente);

	public JTextPane findPreviousJTextPane(JTextPane componente);

	public JTextPane findFirstJTextPane();
	
	public List<JTextPane> getAllJTextPane();
}
