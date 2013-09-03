package br.gov.lexml.swing.spellchecker;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple testing and native build utility class, not useful in applications.
 * 
 * The Hunspell java bindings are licensed under the same terms as Hunspell
 * itself (GPL/LGPL/MPL tri-license), see the file COPYING.txt in the root of
 * the distribution for the exact terms.
 * 
 * @author Flemming Frandsen (flfr at stibo dot com)
 */

public class SpellcheckerMain {
	
	private static final Log log = LogFactory.getLog(SpellcheckerMain.class);

	private static void println(String msg) {
		System.out.println(msg);
	}

	private static void print(String msg) {
		System.out.print(msg);
	}

	public static void main(String[] args) {
		try {
			System.out.println("Preparando diretório");
			
			File baseDir = new File("target/spellchecker");
			
			if(!baseDir.isDirectory()) {
				baseDir.mkdirs();
			}
			
			Spellchecker s = SpellcheckerFactory.getInstance().createSpellchecker(baseDir);
			
			System.out.println("Hunspell library and dictionary loaded");

			String words[] = { "subnutido", "isso", "nao" };

			for (int i = 0; i < words.length; i++) {

				String word = words[i];
				if (s.misspelled(word)) {
					List<String> suggestions = s.suggest(word);
					print("misspelled: " + word);
					if (suggestions.isEmpty()) {
						print("\tNo suggestions.");
					} else {
						print("\tTry:");
						for (String sug : suggestions) {
							print(" " + sug);
						}
					}
					println("");
				} else {
					println("ok: " + word);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
