package dk.dren.hunspell;

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

public class HunspellMain {
	
	private static final Log log = LogFactory.getLog(HunspellMain.class);

	private static void println(String msg) {
		System.out.println(msg);
	}

	private static void print(String msg) {
		System.out.print(msg);
	}

	public static void main(String[] args) {
		try {
			if (args.length == 1 && args[0].equals("-libname")) {
				System.out.println(Hunspell.libName());

			} else {

				System.out.println("Loading Hunspell");
				
				File dir = new File("src/main/resources/dict");
				String dirPath = dir.getCanonicalPath() + "/pt_BR";
				
				Hunspell.Dictionary d = Hunspell.getInstance().getDictionary(dirPath);
				
				System.out.println("Hunspell library and dictionary loaded");

				String words[] = { "subnutido", "isso", "nao" };

				for (int i = 0; i < words.length; i++) {

					String word = words[i];
					if (d.misspelled(word)) {
						List<String> suggestions = d.suggest(word);
						print("misspelled: " + word);
						if (suggestions.isEmpty()) {
							print("\tNo suggestions.");
						} else {
							print("\tTry:");
							for (String s : suggestions) {
								print(" " + s);
							}
						}
						println("");
					} else {
						println("ok: " + word);
					}
				}
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
