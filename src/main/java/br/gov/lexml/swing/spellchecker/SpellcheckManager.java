
package br.gov.lexml.swing.spellchecker;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Aplica a funcionalidade de correção ortográfica a um JTextPane.
 * 
 * <p>
 * Utilização:
 * 
 * <pre>
 * 
 * JTextPane textPane = new JTextPane();
 * 
 * File userDictionaryDir = new File(&quot;/path/to/user/dictionary/dir&quot;);
 * 
 * SpellcheckManager spellCheckManager = new SpellcheckManager(userDictionaryDir, textpane);
 * </pre>
 * 
 * Após setar novo texto ou novo documento no JText pane:
 * 
 * <pre>
 * spellCheckManager.registerDocument(textPane.getDocument());
 * </pre>
 * 
 * </p>
 */
public class SpellcheckManager {

	private static final Log log = LogFactory.getLog(SpellcheckManager.class);

	private Spellchecker spellchecker;

	private UserDictionaryManager userDirectoryManager;

	private JTextPane textPaneAtivo;

	// private Document doc;

	/*
	 * private JPopupMenu popup;
	 * 
	 * private JMenuItem menuItemIgnorar;
	 * 
	 * private JMenuItem menuItemIgnorarSempre;
	 * 
	 * private JMenuItem menuItemAdicionar;
	 */

	private PopupActionListener popupActionListener;

	private HighlightPainter myHighlightPainter = new SpellcheckHighlightPainter();

	private static final Pattern PATTERN_WORD_CHARS = Pattern.compile("[\\p{L}\\d-]");

	private static final Pattern PATTERN_ORDINAL = Pattern.compile("\\d+[ºª]");

	private List<JTextPane> lista = null;

	private ListIterator iterator;

	private Map<Document, JTextPane> mapa;

	public SpellcheckManager(File baseDir, JTextPane... listaPanels) throws SpellcheckerInitializationException {

		// this.textPaneAtivo = textPane[0];

		spellchecker = SpellcheckerFactory.getInstance().createSpellchecker(baseDir);

		userDirectoryManager = UserDictionaryManagerFactory.getInstance().createLocalDictionaryManager(baseDir);
		userDirectoryManager.register(this);

		// Inicializa componentes de interface
		/*
		 * popupActionListener = new PopupActionListener();
		 * 
		 * popup = new JPopupMenu();
		 * 
		 * menuItemIgnorar = new JMenuItem("Ignorar");
		 * menuItemIgnorar.addActionListener(popupActionListener);
		 * 
		 * menuItemIgnorarSempre = new JMenuItem("Ignorar sempre");
		 * menuItemIgnorarSempre.addActionListener(popupActionListener);
		 * 
		 * menuItemAdicionar = new JMenuItem("Adicionar palavra");
		 * menuItemAdicionar.addActionListener(popupActionListener);
		 */

		configurarPanels(listaPanels);

	}

	public void configurarPanels(JTextPane... listaPanels) {

		if (listaPanels != null) {

			lista = Arrays.asList(listaPanels);

			iterator = lista.listIterator();

			mapa = new HashMap<Document, JTextPane>();

			for (JTextPane jTextPane : listaPanels) {

				this.textPaneAtivo = jTextPane;

				initTextPane();

				mapa.put(jTextPane.getDocument(), jTextPane);

			}

			restart();

		}
	}

	private void initTextPane() {

		textPaneAtivo.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {

				maybeShowPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {

				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {

				if (e.isPopupTrigger()) {
					checkMenu(e);
				}
			}

		});

		registerDocument(textPaneAtivo.getDocument());

	}

	public void registerDocument(Document newDoc) {

		// if (newDoc != doc) {

		newDoc.addDocumentListener(new DocumentListener() {

			public void insertUpdate(DocumentEvent event) {

				highlight(event);
			}

			public void removeUpdate(DocumentEvent event) {

				highlight(event);
			}

			public void changedUpdate(DocumentEvent event) {

			}

		});

		// doc = newDoc;
		// }

		highlight();

	}

	private void highlight() {

		/*
		 * SwingUtilities.invokeLater(new Runnable() {
		 * 
		 * @Override public void run() {
		 */

//		textPaneAtivo.requestFocus();
		textPaneAtivo.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		highlight(0, textPaneAtivo.getDocument().getLength());
		textPaneAtivo.setCursor(Cursor.getDefaultCursor());
		// }

		// });

	}

	private void highlight(DocumentEvent event) {

		Document document = event.getDocument();

		this.textPaneAtivo = mapa.get(document);

		// log.debug("Event: " + event.getType());
		highlight(event.getOffset(), event.getLength());
	}

	private void highlight(int eventOffset, int eventLength) {

		Document doc = textPaneAtivo.getDocument();

		int docLength = doc.getLength();

		log.debug("docLength: " + docLength + ", eventOffset: " + eventOffset + ", eventLength: " + eventLength);

		if (docLength == 0) {
			return;
		}

		try {

			int i1 = findWordStartIndex(eventOffset, docLength);

			if (i1 == -1) {

				// Não encontrou palavra
				return;
			}

			// log.debug("i1: " + i1);

			int i2 = Math.min(eventOffset + eventLength, docLength);
			while (i2 < docLength && isWordCharacterAt(i2)) {
				i2++;
			}

			// log.debug("i2: " + i2);

			// log.debug("Palavras alteradas: " + doc.getText(i1, i2 -
			// i1));

			Highlighter hilite = textPaneAtivo.getHighlighter();

			removeHighlights(i1, i2);

			while (i1 < i2) {
				WordSelection ws = selectFirstWord(i1, i2);
				if (!StringUtils.isEmpty(ws.word)) {

					// log.debug("... " + ws.word);

					if (misspelled(ws.word)) {
						hilite.addHighlight(ws.offset, ws.offset + ws.length, myHighlightPainter);
					}

				}
				i1 = ws.offset + ws.length + 1;
			}

		} catch (BadLocationException e) {
			log.error(e.getMessage(), e);
		}

	}

	private boolean misspelled(String word) {

		return spellchecker.misspelled(word) && !userDirectoryManager.isIgnored(word) && !isException(word);
	}

	private boolean isException(String word) {

		return word.equals("-") || PATTERN_ORDINAL.matcher(word).matches();
	}

	private WordSelection selectFirstWord(int iStart, int iLimit) throws BadLocationException {

		// log.debug("iStart: " + iStart + ", iLimit: " + iLimit);

		Document doc = textPaneAtivo.getDocument();

		WordSelection ws = new WordSelection();

		int i = iStart;

		while (i < iLimit && !isWordCharacterAt(i)) {
			i++;
		}
		ws.offset = i;

		// log.debug("..... ws.offset: " + i);

		while (i < iLimit && isWordCharacterAt(i)) {
			i++;
		}
		ws.length = i - ws.offset;

		// log.debug("..... ws.length: " + i);

		ws.word = ws.offset >= 0 ? doc.getText(ws.offset, ws.length) : "";

		return ws;
	}

	private int findWordStartIndex(int i, int docLength) throws BadLocationException {

		if (i >= docLength) {
			i = docLength - 1;
		}
		if (i < 0) {
			return -1;
		}
		while (i >= 0 && isWordCharacterAt(i - 1)) {
			i--;
		}
		return i;
	}

	private int findWordEndIndex(int i, int docLength) throws BadLocationException {

		while (i < docLength && isWordCharacterAt(i)) {
			i++;
		}
		return i;
	}

	private boolean isWordCharacterAt(int i) throws BadLocationException {

		Document doc = textPaneAtivo.getDocument();

		return i >= 0 && isWordCharacter(doc.getText(i, 1));
	}

	private boolean isWordCharacter(String c) {

		return PATTERN_WORD_CHARS.matcher(c).matches();
	}

	private boolean isLineBreakAt(int i) throws BadLocationException {

		Document doc = textPaneAtivo.getDocument();

		return "\r\n".contains(doc.getText(i, 1));
	}

	private class WordSelection {

		int offset, length;

		String word;
	}

	// Removes only our private highlights
	private void removeHighlights() {

		removeHighlights(0, textPaneAtivo.getDocument().getLength());
	}

	// Removes only our private highlights
	private void removeHighlights(int from, int to) {

		Highlighter hilite = textPaneAtivo.getHighlighter();
		Highlight[] hilites = hilite.getHighlights();

		for (Highlight h : hilites) {
			if (h.getStartOffset() >= from && h.getEndOffset() <= to) {
				if (h.getPainter() instanceof SpellcheckHighlightPainter) {
					hilite.removeHighlight(h);
				}
			}
		}
	}

	void removeHighlight(String word) {

		Highlighter hilite = textPaneAtivo.getHighlighter();
		Highlight[] hilites = hilite.getHighlights();

		try {
			for (Highlight h : hilites) {
				if (h.getPainter() instanceof SpellcheckHighlightPainter) {
					String highlightedWord = getHighlightedWord(h);
					if (highlightedWord.equalsIgnoreCase(word)) {
						hilite.removeHighlight(h);
					}
				}
			}
		} catch (BadLocationException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void removeHighlight(Highlight h) {

		textPaneAtivo.getHighlighter().removeHighlight(h);
	}

	public Highlight highlight(int startOffset, int endOffset, HighlightPainter painter) {

		try {
	
			textPaneAtivo.setCaretPosition(startOffset);
			
			return (Highlight) textPaneAtivo.getHighlighter().addHighlight(startOffset, endOffset, painter);
			
		} catch (BadLocationException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	private void checkMenu(MouseEvent e) {

		JTextPane pane = (JTextPane) e.getSource();

		this.textPaneAtivo = pane;

	//	if (pane == textPaneAtivo) {

			Document doc = textPaneAtivo.getDocument();

			try {
				log.debug("------------ : " + doc.getText(0, doc.getLength()));
				int i = Math.min(Math.max(textPaneAtivo.viewToModel(e.getPoint()), 0), doc.getLength() - 1);

				int docLength = doc.getLength();

				i = findWordStartIndex(i, docLength);
				if (i == -1) {
					return;
				}

				WordSelection ws = selectFirstWord(i, docLength - 1);

				if (!StringUtils.isEmpty(ws.word)) {

					Highlighter hilite = textPaneAtivo.getHighlighter();
					Highlight[] hilites = hilite.getHighlights();

					for (Highlight h : hilites) {
						if (h.getStartOffset() == ws.offset) {
							List<String> suggs = spellchecker.suggest(ws.word);
							showSuggestionMenu(e, ws.word, h, suggs);
						}
					}

				}

			} catch (BadLocationException e1) {
				log.error(e1.getMessage(), e1);
			}

		//}

	}

	private void showSuggestionMenu(MouseEvent e, String word, Highlight highlight, List<String> suggs) {

		popupActionListener = new PopupActionListener();

		JPopupMenu popup = new JPopupMenu();

		JMenuItem menuItemIgnorar = new JMenuItem("Ignorar");
		menuItemIgnorar.addActionListener(popupActionListener);
		menuItemIgnorar.setActionCommand("menuItemIgnorar");

		JMenuItem menuItemIgnorarSempre = new JMenuItem("Ignorar sempre");
		menuItemIgnorarSempre.addActionListener(popupActionListener);
		menuItemIgnorarSempre.setActionCommand("menuItemIgnorarSempre");

		JMenuItem menuItemAdicionar = new JMenuItem("Adicionar palavra");
		menuItemAdicionar.addActionListener(popupActionListener);
		menuItemAdicionar.setActionCommand("menuItemAdicionar");

		popupActionListener.setWord(word);
		popupActionListener.setHighlight(highlight);

		popup.removeAll();
		for (String sugg : suggs) {
			JMenuItem menuItem = new JMenuItem(sugg);
			menuItem.addActionListener(popupActionListener);
			popup.add(menuItem);
		}

		if (popup.getComponents().length > 0) {
			popup.addSeparator();
		}
		popup.add(menuItemIgnorar);
		popup.add(menuItemIgnorarSempre);
		popup.add(menuItemAdicionar);

		popup.show(e.getComponent(), e.getX(), e.getY());
	}

	public class PopupActionListener implements ActionListener {

		private String word;

		private Highlight highlight;

		public void setWord(String word) {

			this.word = word;
		}

		public void setHighlight(Highlight highlight) {

			this.highlight = highlight;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			// Ignorar palavra
			if (e.getActionCommand().equals("menuItemIgnorar")) {
				removeHighlight(highlight);
			}

			// Ignorar sempre
			else if (e.getActionCommand().equals("menuItemIgnorarSempre")) {
				ignorarSempre(word);
			}

			// Adicionar palavra
			else if (e.getActionCommand().equals("menuItemAdicionar")) {
				adicionar(word);
			}

			// Aceitar sugestão
			else {
				String newWord = ((JMenuItem) e.getSource()).getText();
				substituir(highlight, newWord);
			}

		}

	}

	public Highlight nextHighlight(int idx) {

		List<Highlight> hilites = getSpellcheckHilitesOrdered();

		for (Highlight h : hilites) {
			if (h.getStartOffset() >= idx) {
				return h;
			}
		}

		if (idx == 0) {

			while ((hilites == null || hilites.size() == 0) && iterator.hasNext()) {

				this.textPaneAtivo = (JTextPane) iterator.next();

				hilites = getSpellcheckHilitesOrdered();

				for (Highlight h : hilites) {
					if (h.getStartOffset() >= idx) {
						return h;
					}
				}

			}

		}

		return null;
	}

	private List<Highlight> getSpellcheckHilitesOrdered() {

		log.debug(textPaneAtivo.getText());
		
		Highlighter hilite = textPaneAtivo.getHighlighter();

		List<Highlight> hilites = new ArrayList<Highlight>();

		for (Highlight h : hilite.getHighlights()) {
			if (h.getPainter() instanceof SpellcheckHighlightPainter) {
				hilites.add(h);
			}
		}

		Collections.sort(hilites, new Comparator<Highlight>() {

			public int compare(Highlight o1, Highlight o2) {

				return o1.getStartOffset() - o2.getStartOffset();
			};

		});

		return hilites;
	}

	public String getHighlightedWord(Highlight h) throws BadLocationException {

		Document doc = textPaneAtivo.getDocument();

		return doc.getText(h.getStartOffset(), h.getEndOffset() - h.getStartOffset());
	}

	public List<String> getSuggestions(String word) {

		return spellchecker.suggest(word);
	}

	public void ignorar(Highlight h) {

		removeHighlights(h.getStartOffset(), h.getEndOffset());
	}

	public void ignorarSempre(String word) {

		try {
			userDirectoryManager.ignoreWord(word);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void adicionar(String word) {

		try {
			userDirectoryManager.addWord(word);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public void substituir(Highlight h, String suggestion) {

		Document doc = textPaneAtivo.getDocument();

		int startOffset = h.getStartOffset();

		int endOffset = h.getEndOffset();

		removeHighlights(startOffset, endOffset);

		try {

			doc.remove(startOffset, endOffset - startOffset);
			doc.insertString(startOffset, suggestion, textPaneAtivo.getInputAttributes());

			textPaneAtivo.setSelectionStart(startOffset + suggestion.length());
			textPaneAtivo.setSelectionEnd(textPaneAtivo.getSelectionStart());

		} catch (BadLocationException e1) {

			log.error(e1.getMessage(), e1);

		}
	}

	public void substituirTodas(String word, String suggestion) {

		restart();

		while (iterator.hasNext()) {

			this.textPaneAtivo = (JTextPane) iterator.next();

			List<Highlight> list = getSpellcheckHilitesOrdered();
			Collections.reverse(list);

			try {

				String hWord = null;

				Document doc = textPaneAtivo.getDocument();

				for (Highlight h : list) {

					hWord = doc.getText(h.getStartOffset(), h.getEndOffset() - h.getStartOffset());

					if (hWord.equalsIgnoreCase(word)) {

						substituir(h, suggestion);

					}
				}

			} catch (BadLocationException e) {

				log.error(e.getMessage(), e);

			}

		}
		
		//restart();

	}

	public WordInContext getWordInContext(Highlight h) throws BadLocationException {

		int hStartOffset = h.getStartOffset();
		int sStartOffset = hStartOffset;
		int hEndOffset = h.getEndOffset();
		int sEndOffset = hEndOffset;

		int maxWords = 5;

		// Busca início do contexto
		for (int i = 0; i < maxWords && sStartOffset >= 0; i++) {
			sStartOffset = inicioDaPalavraAnteriorParaContexto(sStartOffset);
		}

		Document doc = textPaneAtivo.getDocument();

		// Busca fim do contexto
		int docLength = doc.getLength();
		for (int i = 0; i < maxWords && sEndOffset < docLength; i++) {
			sEndOffset = fimDaPalavraPosteriorParaContexto(sEndOffset);
		}

		WordInContext wic = new WordInContext();
		wic.context = doc.getText(sStartOffset, sEndOffset - sStartOffset);
		wic.word = getHighlightedWord(h);
		wic.startOffset = hStartOffset - sStartOffset;
		wic.endOffset = hEndOffset - sStartOffset;
		return wic;

	}

	private int inicioDaPalavraAnteriorParaContexto(int sStartOffset) throws BadLocationException {

		Document doc = textPaneAtivo.getDocument();

		int docLength = doc.getLength();

		int i = sStartOffset;
		int inicioPalavraAtual = i = findWordStartIndex(i, docLength);

		if (i >= 0) {
			while (i >= 0 && !isWordCharacterAt(i)) {
				if (isLineBreakAt(i)) {
					return inicioPalavraAtual;
				}
				i--;
			}
			if (i >= 0) {
				i = findWordStartIndex(i, docLength);
			}
		}

		return Math.max(i, 0);

	}

	private int fimDaPalavraPosteriorParaContexto(int sEndOffset) throws BadLocationException {

		Document doc = textPaneAtivo.getDocument();

		int i = sEndOffset;
		int docLength = doc.getLength();

		int fimPalavraAtual = i = findWordEndIndex(i, docLength);

		if (i < docLength) {
			while (i < docLength && !isWordCharacterAt(i)) {
				if (isLineBreakAt(i)) {
					return fimPalavraAtual;
				}
				i++;
			}
			if (i < docLength) {
				i = findWordEndIndex(i, docLength);
			}
		}

		return Math.min(i, docLength);
	}

	public static class WordInContext {

		public String context;

		public String word;

		public int startOffset;

		public int endOffset;
	}

	public boolean hasMoreElements() {

		return ((this.iterator != null) && this.iterator.hasNext());
	}

	public void setListaJTextPane(List<JTextPane> lista) {

		this.lista = lista;
	}

	public void addJTextPane(JTextPane jTextPane) {

		if (this.lista == null) {

			this.lista = new ArrayList<JTextPane>();

		}

		this.lista.add(jTextPane);

	}

	public void removeJTextPane(JTextPane jTextPane) {

		if (this.lista != null && lista.contains(jTextPane)) {

			this.lista.remove(jTextPane);

			if (this.mapa.containsKey(jTextPane.getDocument())) {

				this.mapa.remove(jTextPane.getDocument());

			}

		}

	}

	public void restart() {

		if (lista != null) {

			iterator = lista.listIterator();

			while (iterator.hasPrevious()) {

				iterator.previous();

			}

			this.textPaneAtivo = lista.get(0);

		}

	}

	public List<JTextPane> getListaJTextPane() {

		return lista;
	}

}
