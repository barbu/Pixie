package org.pixie;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.pixie.Pixie.ToolDelegate;
import org.pixie.Tool.FillTool;
import org.pixie.Tool.CropTool;
import org.pixie.Tool.LineTool;
import org.pixie.Tool.PointTool;
import org.pixie.Tool.RectangleTool;
import org.pixie.Tool.SprayTool;

public class ToolPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	protected ToolDelegate del;
	protected JPanel toolGrid;
	protected ButtonGroup bg = new ButtonGroup();

	public ToolPanel(ToolDelegate del) {
		super();
		this.del = del;

		add(toolGrid = new JPanel(new GridLayout(0, 2)));

		AbstractButton sel;

		addButton(new ToolButton(Pixie.getIcon("pencil"),
				"Pencil - draws freehand strokes", new PointTool()));
		sel = addButton(new ToolButton(Pixie.getIcon("line"),
				"Line - draws a straight line", new LineTool()));
		addButton(new ToolButton(Pixie.getIcon("rect"),
				"Rect - draws a filled rectangle", new RectangleTool()));
		addButton(new ToolButton(Pixie.getIcon("color-fill"),
				"Fill - flood-fills a region", new FillTool()));
		addButton(new ToolButton(Pixie.getIcon("cut"),"Crop image",
				new CropTool()));
		addButton(new ToolButton(Pixie.getIcon("spray"), "Spray",
				new SprayTool()));
		
		sel.doClick();
	}

	public <K extends AbstractButton> K addButton(K b) {
		toolGrid.add(b);
		bg.add(b);
		b.addActionListener(this);
		return b;
	}

	public class ToolButton extends JToggleButton {
		private static final long serialVersionUID = 1L;

		public final Tool tool;

		public ToolButton(ImageIcon ico, Tool t) {
			this(ico, null, t);
		}

		public ToolButton(ImageIcon ico, String tip, Tool t) {
			super(ico);
			tool = t;
			setToolTipText(tip);
			setPreferredSize(new Dimension(32, 32));
		}
	}

	public void actionPerformed(ActionEvent e) {
		del.setTool(((ToolButton) e.getSource()).tool);
		return;
	}
}
