package br.gov.lexml.swing.spellchecker.dialog;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.gov.lexml.swing.spellchecker.SpellcheckManager;

@SuppressWarnings("serial")
public class SpellcheckDialogAction extends AbstractAction {
	
	private static final Log log = LogFactory.getLog(SpellcheckDialogAction.class);
	
	private SpellcheckManager mgr;
	
	/**
	 * Cria action sem o SpellcheckManager.
	 * 
	 * Prefira utilizar SpellcheckDialogAction(SpellcheckManager mgr).
	 * Construtor necessário para utilização com o EditorHtml, evitando o problema do ovo e da galinha.
	 */
	public SpellcheckDialogAction() {
		putValue(Action.NAME, "Verificar ortografia");
		putValue(Action.SHORT_DESCRIPTION, "Ortografia");
		putValue(Action.LONG_DESCRIPTION, "Verificar ortografia");
		putValue(Action.SMALL_ICON, loadIcon());
	}

	public SpellcheckDialogAction(SpellcheckManager mgr) {
		this();
		this.mgr = mgr;
	}
	
	public void setSpellcheckManager(SpellcheckManager mgr) {
		this.mgr = mgr;
	}

	private ImageIcon loadIcon() {
		URL imageURL = getClass().getResource("/icons/Spellcheck.png");
		if (imageURL != null) {
			return new ImageIcon(Toolkit.getDefaultToolkit().getImage(imageURL));
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(mgr != null) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					new SpellcheckDialogControllerImpl(mgr).iniciar();
				}
				
			});
		}
		else {
			log.warn("SpellcheckManager não informado para a SpellcheckDialogAction.");
		}
	}

}
