package org.pixie;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.BevelBorder;

public class Palette extends JPanel {
	private static final long serialVersionUID = 1L;

	private Color left = Color.BLACK, right = Color.WHITE;
	private CurrentColor cur;

	public Palette() {
		super();
		add(cur = new CurrentColor());
		add(new JSeparator(JSeparator.VERTICAL));
		add(new WellPalette());
	}

	public void setLeft(Color c) {
		left = c;
		cur.repaint();
	}

	public void setRight(Color c) {
		right = c;
		cur.repaint();
	}

	public Color getLeft() {
		return left;
	}

	public Color getRight() {
		return right;
	}

	public void setSelectedColor(int button, Color c) {
		switch (button) {
		case MouseEvent.BUTTON1:
			setLeft(c);
			return;
		case MouseEvent.BUTTON3:
			setRight(c);
			return;
		default:
			if ((button & (MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON1_DOWN_MASK)) != 0)
				setLeft(c);
			if ((button & (MouseEvent.BUTTON3_MASK | MouseEvent.BUTTON3_DOWN_MASK)) != 0)
				setRight(c);
		}
	}

	public Color getSelectedColor(int button) {
		switch (button) {
		case MouseEvent.BUTTON1:
			return left;
		case MouseEvent.BUTTON3:
			return right;
		default:
			if ((button & (MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON1_DOWN_MASK)) != 0)
				return left;
			if ((button & (MouseEvent.BUTTON3_MASK | MouseEvent.BUTTON3_DOWN_MASK)) != 0)
				return right;
			return null;
		}
	}

	protected class WellPalette extends JPanel {
		private static final long serialVersionUID = 1L;

		public WellPalette() {
			super(new GridLayout(2, 0));

			add(new Well(Color.BLACK));
			add(new Well(Color.GRAY));
			add(new Well(Color.RED));
			add(new Well(Color.ORANGE));
			add(new Well(Color.YELLOW));
			add(new Well(Color.GREEN));
			add(new Well(Color.CYAN));
			add(new Well(Color.BLUE));
			add(new Well(Color.MAGENTA));

			add(new Well(Color.WHITE));
			add(new Well(Color.LIGHT_GRAY));
			add(new Well(0x800000));
			add(new Well(0x804000));
			add(new Well(0x808000));
			add(new Well(0x008000));
			add(new Well(0x008080));
			add(new Well(0x000080));
			add(new Well(0x800080));
		}
	}

	protected class Well extends JPanel {
		private static final long serialVersionUID = 1L;

		private Color selectedColor;

		public Well(int rgb) {
			this(new Color(rgb));
		}

		public Well(Color col) {
			super();
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			enableEvents(AWTEvent.MOUSE_EVENT_MASK);
			setPreferredSize(new Dimension(24, 24));
			setBackground(selectedColor = col);
		}

		@Override
		public void processMouseEvent(MouseEvent e) {
			if (e.getID() == MouseEvent.MOUSE_CLICKED) {
				if (e.getClickCount() == 2) {
					Color newcol = JColorChooser.showDialog(getParent(),
							"Select Color", selectedColor);
					if (newcol != null)
						setBackground(selectedColor = newcol);
				} else
					setSelectedColor(e.getButton(), selectedColor);
			}
			super.processMouseEvent(e); // for listeners, probably
		}
	}

	protected class CurrentColor extends JLabel {
		private static final long serialVersionUID = 6026758209604170064L;

		public CurrentColor() {
			super();
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			setBackground(Color.WHITE);
			setPreferredSize(new Dimension(48, 48));
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(right);
			int s = 20;
			int f1 = 8;
			int f2 = f1 + (int) (s * 0.6);
			g.fillRect(f2, f2, s, s);
			g.setColor(Color.BLACK);
			g.drawRect(f2, f2, s, s);
			g.setColor(left);
			g.fillRect(f1, f1, s, s);
			g.setColor(Color.BLACK);
			g.drawRect(f1, f1, s, s);
		}
	}
}
