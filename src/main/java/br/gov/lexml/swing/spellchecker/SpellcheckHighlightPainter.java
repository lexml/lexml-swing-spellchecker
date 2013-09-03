package br.gov.lexml.swing.spellchecker;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;

/**
 * Marca a área destacada com sublinhado ondulado.
 */
public class SpellcheckHighlightPainter extends LayeredHighlighter.LayerPainter {

	private Color color = Color.RED;

	public SpellcheckHighlightPainter() {
	}

	public SpellcheckHighlightPainter(Color c) {
		color = c;
	}

	// --- HighlightPainter methods ---------------------------------------

	/**
	 * Paints a highlight.
	 * 
	 * @param g
	 *            the graphics context
	 * @param offs0
	 *            the starting model offset >= 0
	 * @param offs1
	 *            the ending model offset >= offs1
	 * @param bounds
	 *            the bounding box for the highlight
	 * @param c
	 *            the editor
	 */
	public void paint(Graphics g, int offs0, int offs1, Shape bounds,
			JTextComponent c) {
		Rectangle alloc = bounds.getBounds();
		try {
			// --- determine locations ---
			TextUI mapper = c.getUI();
			Rectangle p0 = mapper.modelToView(c, offs0);
			Rectangle p1 = mapper.modelToView(c, offs1);

			// --- render ---
			if (color == null) {
				g.setColor(c.getSelectionColor());
			} else {
				g.setColor(color);
			}
			if (p0.y == p1.y) {
				// same line, render a rectangle
				Rectangle r = p0.union(p1);
				g.fillRect(r.x, r.y, r.width, r.height);
			} else {
				// different lines
				int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
				g.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
				if ((p0.y + p0.height) != p1.y) {
					g.fillRect(alloc.x, p0.y + p0.height, alloc.width, p1.y
							- (p0.y + p0.height));
				}
				g.fillRect(alloc.x, p1.y, (p1.x - alloc.x), p1.height);
			}
		} catch (BadLocationException e) {
			// can't render
		}
	}

	// --- LayerPainter methods ----------------------------
	/**
	 * Paints a portion of a highlight.
	 * 
	 * @param g
	 *            the graphics context
	 * @param offs0
	 *            the starting model offset >= 0
	 * @param offs1
	 *            the ending model offset >= offs1
	 * @param bounds
	 *            the bounding box of the view, which is not necessarily the
	 *            region to paint.
	 * @param c
	 *            the editor
	 * @param view
	 *            View painting for
	 * @return region drawing occured in
	 */
	public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
			JTextComponent c, View view) {

		if (color == null) {
			g.setColor(c.getSelectionColor());
		} else {
			g.setColor(color);
		}
		
		try {
			// --- determine locations ---
			Shape shape = view.modelToView(offs0, Position.Bias.Forward,
					offs1, Position.Bias.Backward, bounds);
			Rectangle r = (shape instanceof Rectangle) ? (Rectangle) shape
					: shape.getBounds();
			int y = r.y + r.height;
			drawLine(g, r.x, y, r.width);
			return r;
		} catch (BadLocationException e) {
			// can't render
		}

		// Only if exception
		return null;
	}

	private void drawLine(Graphics g, int x, int y, int width) {
		boolean up = true;
		int largura = 2;
		int xMax = x + width - largura;
		int yMax = y - 1;
		int yMin = yMax - largura;
		
		for(; x <= xMax; x += largura) {
			if(up) {
				g.drawLine(x, yMax, x + largura, yMin);
			}
			else {
				g.drawLine(x, yMin, x + largura, yMax);
			}
			up = !up;
		}
	}

}
