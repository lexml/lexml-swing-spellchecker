package br.gov.lexml.swing.spellchecker.dialog;

import java.awt.Color;

import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.gov.lexml.swing.spellchecker.SpellcheckManager;
import br.gov.lexml.swing.spellchecker.SpellcheckManager.WordInContext;

public class SpellcheckDialogControllerImpl implements SpellcheckDialogController {

	private static final Log log = LogFactory.getLog(SpellcheckDialogControllerImpl.class);

	private SpellcheckManager mgr;

	private SpellcheckDialogView view;

	private int idx;

	private Highlight highlight;

	private Highlight selectionHighlight;

	private String word;

	private HighlightPainter highlightPainter;

	public SpellcheckDialogControllerImpl(SpellcheckManager mgr) {

		this.mgr = mgr;
		view = new SpellcheckDialogView(this);
		highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
	}

	@Override
	public void iniciar() {

		idx = 0;
		mgr.restart();
		if (proximaPalavra()) {
			view.centraliza();
			view.setVisible(true);
		}
	}

	private boolean proximaPalavra() {

		removeSelectionHighlight();

		highlight = mgr.nextHighlight(idx);

		if (highlight == null) {

			idx = 0;

			while (highlight == null) {// && mgr.hasMoreElements()

				highlight = mgr.nextHighlight(idx);

				if (!mgr.hasMoreElements()) {

					break;

				}

			}

			if (highlight == null) {

				JOptionPane.showMessageDialog(view, "A verificação ortográfica está completa.");

				fechar();

				return false;

			}

		} // else {

		try {
			
			addSelectionHighlight();
			
			WordInContext wic = mgr.getWordInContext(highlight);
			word = wic.word;
			
			idx = highlight.getEndOffset();
			
			view.setWordInContext(wic);
			view.setSuggestions(mgr.getSuggestions(word));

		} catch (BadLocationException e) {

			log.error(e.getMessage(), e);

		}
		// }

		view.habilitaBotoes();

		return true;
	}

	private void addSelectionHighlight() {

		selectionHighlight = mgr.highlight(highlight.getStartOffset(), highlight.getEndOffset(), highlightPainter);
	}

	private void removeSelectionHighlight() {

		if (selectionHighlight != null) {
			mgr.removeHighlight(selectionHighlight);
			selectionHighlight = null;
		}
	}

	@Override
	public void ignorar() {

		mgr.ignorar(highlight);
		proximaPalavra();
	}

	@Override
	public void ignorarSempre() {

		mgr.ignorarSempre(word);
		proximaPalavra();
	}

	@Override
	public void adicionar() {

		mgr.adicionar(word);
		proximaPalavra();
	}

	@Override
	public void substituir() {

		mgr.substituir(highlight, view.getSuggestion());
		proximaPalavra();
	}

	@Override
	public void substituirTodas() {

		mgr.substituirTodas(word, view.getSuggestion());
		proximaPalavra();
	}

	@Override
	public void fechar() {

		removeSelectionHighlight();
		view.setVisible(false);
	}

}
