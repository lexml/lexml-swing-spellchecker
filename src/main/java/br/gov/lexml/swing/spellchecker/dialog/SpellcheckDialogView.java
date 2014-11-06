package br.gov.lexml.swing.spellchecker.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import br.gov.lexml.swing.spellchecker.SpellcheckManager.WordInContext;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class SpellcheckDialogView extends JDialog implements ActionListener, ListSelectionListener {
	
	private SpellcheckDialogController ctl;
	private JTextPane txtNaoConsta;
	private JButton btnIgnorar;
	private JButton btnAdicionar;
	private JLabel lblSugestoes;
	private JList lstSugestoes;
	private DefaultListModel modelSugestoes;
	private JButton btnSubstituir;
	private JButton btnSubstituirTodas;
	private JButton btnFechar;
	private JButton btnIgnorarSempre;
	
	public SpellcheckDialogView(SpellcheckDialogController ctl) {
		this.ctl = ctl;
		initialize();
	}
	
	private void initialize() {

		setModal(true);
		setSize(new Dimension(500, 310));
		setResizable(false);
		setTitle("Ortografia");
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				ctl.fechar();
			}
			
		});
		
		getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("bottom:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("bottom:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:50dlu"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		JLabel lblNaoConsta = new JLabel("Não consta do dicionário:");
		getContentPane().add(lblNaoConsta, "2, 2");
		
		txtNaoConsta = new JTextPane();
		txtNaoConsta.setContentType("text/html");
		txtNaoConsta.setFocusable(false);
		txtNaoConsta.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		getContentPane().add(txtNaoConsta, "2, 4, 1, 3, fill, fill");
		
		btnIgnorar = new JButton("Ignorar");
		btnIgnorar.addActionListener(this);
		getContentPane().add(btnIgnorar, "4, 2");
		
		btnIgnorarSempre = new JButton("Ignorar sempre");
		btnIgnorarSempre.addActionListener(this);
		getContentPane().add(btnIgnorarSempre, "4, 4");
		
		btnAdicionar = new JButton("Adicionar");
		btnAdicionar.addActionListener(this);
		getContentPane().add(btnAdicionar, "4, 6");
		
		lblSugestoes = new JLabel("Sugestões:");
		getContentPane().add(lblSugestoes, "2, 8");
		
		modelSugestoes = new DefaultListModel();
		
		lstSugestoes = new JList(modelSugestoes);
		lstSugestoes.addListSelectionListener(this);
		lstSugestoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane spSugestoes = new JScrollPane(lstSugestoes);
		getContentPane().add(spSugestoes, "2, 10, 1, 5, fill, fill");
		
		btnSubstituir = new JButton("Substituir");
		btnSubstituir.addActionListener(this);
		getContentPane().add(btnSubstituir, "4, 10");
		
		btnSubstituirTodas = new JButton("Substituir todas");
		btnSubstituirTodas.addActionListener(this);
		getContentPane().add(btnSubstituirTodas, "4, 12");
		
		btnFechar = new JButton("Fechar");
		btnFechar.addActionListener(this);
		getContentPane().add(btnFechar, "4, 14");
		
		habilitaBotoes();
		
	}

	public void habilitaBotoes() {
		boolean temPalavra = !StringUtils.isEmpty(txtNaoConsta.getText());
		btnIgnorar.setEnabled(temPalavra);
		btnAdicionar.setEnabled(temPalavra);
		
		boolean temSugestao = lstSugestoes.getSelectedIndex() >= 0;
		btnSubstituir.setEnabled(temSugestao);
		btnSubstituirTodas.setEnabled(temSugestao);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == btnIgnorar) {
			ctl.ignorar();
		}
		else if(e.getSource() == btnIgnorarSempre) {
			ctl.ignorarSempre();
		}
		else if(e.getSource() == btnAdicionar) {
			ctl.adicionar();
		}
		else if(e.getSource() == btnSubstituir) {
			ctl.substituir();
		}
		else if(e.getSource() == btnSubstituirTodas) {
			ctl.substituirTodas();
		}
		else if(e.getSource() == btnFechar) {
			ctl.fechar();
		}
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		habilitaBotoes();
	}

	public void centraliza() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width - getWidth()) / 2, (d.height - getHeight()) / 2);
	}

	public void setWordInContext(WordInContext wic) {
		StringBuilder sb = new StringBuilder(wic.context);
		sb.insert(wic.endOffset, "</span>");
		sb.insert(wic.startOffset, "<span style='color: red; font-weight: bold;'>");
		txtNaoConsta.setText(sb.toString());
	}

	public void setSuggestions(List<String> suggestions) {
		modelSugestoes.clear();
		for(String w: suggestions) {
			modelSugestoes.addElement(w);
		}
	}

	public String getSuggestion() {
		return (String) lstSugestoes.getSelectedValue();
	}

}
