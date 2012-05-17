package org.pixie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

public class Pixie implements ActionListener {
	private final JFrame frame;
	private JButton bUndo, bZoomIn, bZoomOut, bRotate90Right, bRotate90Left,
			bMirror;
	private JToggleButton bGrid;
	private final JScrollPane scroll;

	public File file;
	public Canvas canvas;
	public Palette pal;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	private final JPanel toolPanel;
	public final String TITLE = "Pixie ";

	public Pixie(BufferedImage image) {
		if (image == null)
			image = createWhiteBufferedImage(32, 32);
		pal = new Palette();
		canvas = new Canvas(image);
		scroll = new JScrollPane(canvas);
		toolPanel = new ToolPanel(new ToolDelegate(canvas));

		JPanel p = new JPanel(new BorderLayout());
		p.add(makeToolBar(), BorderLayout.NORTH);
		p.add(toolPanel, BorderLayout.WEST);
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		scroll.getHorizontalScrollBar().setUnitIncrement(10);
		p.add(scroll, BorderLayout.CENTER);
		p.add(pal, BorderLayout.SOUTH);

		frame = new JFrame();
		frame.setJMenuBar(makeMenuBar());
		frame.setContentPane(p);
		frame.setMinimumSize(new Dimension(500, 500));
		updateTitle();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doClose();
			}
		});
	}

	public void updateTitle() {
		if (file == null)
			frame.setTitle(TITLE + "<untitled>");
		else
			frame.setTitle(TITLE + file.getName());
	}

	public JMenuBar makeMenuBar() {
		menuBar = new JMenuBar();
		JMenu fm = new JMenu("File");
		menuBar.add(fm);
		addMenuItem(fm, "New", getIcon("new"));
		addMenuItem(fm, "Open", getIcon("open"));
		addMenuItem(fm, "Save", getIcon("save"));
		addMenuItem(fm, "Save As", getIcon("save-as"));
		addMenuItem(fm, "Exit", getIcon("cancel"));

		menuBar.add(new EffectsMenu(this));
		return menuBar;
	}

	public void addMenuItem(JMenu menu, String name, ImageIcon icon) {
		JMenuItem mi = new JMenuItem(name, icon);
		mi.setActionCommand(name);
		mi.addActionListener(this);
		menu.add(mi);
	}

	public JToolBar makeToolBar() {
		toolBar = new JToolBar();
		toolBar.setFloatable(false);

		bUndo = addButton(toolBar, new JButton("Undo", getIcon("undo")));
		bGrid = addButton(toolBar, new JToggleButton("Grid", true));

		bZoomOut = addButton(toolBar, new JButton(getIcon("zoom-out")));
		bZoomIn = addButton(toolBar, new JButton(getIcon("zoom-in")));

		bRotate90Right = addButton(toolBar, new JButton(getIcon("rotateright")));
		bRotate90Left = addButton(toolBar, new JButton(getIcon("rotateleft")));
		bMirror = addButton(toolBar, new JButton(getIcon("mirror")));

		return toolBar;
	}

	public <K extends AbstractButton> K addButton(Container c, K b) {
		c.add(b);
		b.addActionListener(this);
		return b;
	}

	public static ImageIcon getIcon(String name) {
		String location = "org/pixie/icons/actions/" + name + ".png";
		URL url = Pixie.class.getClassLoader().getResource(location);
		if (url == null)
			return new ImageIcon(location);
		return new ImageIcon(url);
	}

	protected class ToolDelegate extends MouseAdapter {
		protected Tool tool;
		protected Canvas canvas;

		public ToolDelegate(Canvas canvas) {
			this.canvas = canvas;
			canvas.addMouseListener(this);
			canvas.addMouseMotionListener(this);
			canvas.addMouseWheelListener(this);
		}

		public void setTool(Tool t) {
			if (tool != null)
				tool.finish(canvas, pal);
			tool = t;
		}

		protected MouseEvent refactor(MouseEvent e) {
			int x = e.getX() / canvas.getZoom();
			int y = e.getY() / canvas.getZoom();
			return new MouseEvent((Component) e.getSource(), e.getID(),
					e.getWhen(), e.getModifiers(), x, y, e.getClickCount(),
					e.isPopupTrigger(), e.getButton());
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (tool != null)
				tool.mousePress(refactor(e), canvas, pal);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (tool != null)
				tool.mouseRelease(refactor(e), canvas, pal);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (tool != null)
				tool.mouseMove(refactor(e), canvas, pal, true);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (tool != null)
				tool.mouseMove(refactor(e), canvas, pal, false);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown()) {
				int rot = e.getWheelRotation();
				if (rot < 0)
					canvas.zoomIn();
				else if (rot > 0)
					canvas.zoomOut();
			}
		}
	}

	public static void main(String[] args) {
		System.setProperty("sun.java2d.d3d", "false");

		BufferedImage bi = null;
		try {
			bi = ImageIO.read(Pixie.class.getResource("/pixie.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Pixie j = new Pixie(bi);
		j.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	public static BufferedImage createWhiteBufferedImage(int w, int h) {
		BufferedImage image = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);
		return image;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bUndo) {
			if (!canvas.acts.isEmpty()) {
				canvas.acts.removeLast();
				canvas.redrawCache();
			}
			return;
		}
		if (e.getSource() == bRotate90Right) {
			System.out.println("blabla");
			canvas.rotate90Right();
			return;
		}
		if (e.getSource() == bRotate90Left) {
			System.out.println("blabla");
			canvas.rotate90Left();
			return;
		}

		if (e.getSource() == bMirror) {
			System.out.println("blabla");
			canvas.mirror();
			return;
		}

		if (e.getSource() == bGrid) {
			canvas.isGridDrawn = bGrid.isSelected();
			canvas.repaint();
			return;
		}
		if (e.getSource() == bZoomIn) {
			canvas.zoomIn();
			return;
		}
		if (e.getSource() == bZoomOut) {
			canvas.zoomOut();
			return;
		}
		String act = e.getActionCommand();
		if (act.equals("New")) {
			doNew();
			return;
		}
		if (act.equals("Open")) {
			doOpen();
			return;
		}
		if (act.equals("Save")) {
			doSave(false);
			return;
		}
		if (act.equals("Save As")) {
			doSave(true);
			return;
		}
		if (act.equals("Exit")) {
			doClose();
			return;
		}
	}

	public boolean hasChanged() {
		return !canvas.acts.isEmpty();
	}

	public boolean doNew() {
		if (!checkSave())
			return false;
		// TODO: Ask for sizes
		file = null;
		BufferedImage img = createWhiteBufferedImage(120, 120);
		canvas.setImage(img);
		scroll.updateUI();
		updateTitle();
		return true;
	}

	public void doClose() {
		if (!hasChanged())
			System.exit(0);
		int c = JOptionPane.showConfirmDialog(frame,
				"Do you want to save changes?");
		if (c == JOptionPane.CANCEL_OPTION)
			return;
		if (c == JOptionPane.OK_OPTION)
			doSave(false);
		System.exit(0);
	}

	/**
	 * @return false if the action was canceled
	 */
	public boolean checkSave() {
		if (hasChanged()) {
			int c = JOptionPane.showConfirmDialog(frame,
					"Image has been modified. Would you like to save first?");
			if (c == JOptionPane.CANCEL_OPTION)
				return false;
			if (c == JOptionPane.OK_OPTION)
				doSave(false);
		}
		return true;
	}

	public boolean doOpen() {
		if (!checkSave())
			return false;
		File f = getFile(false);
		if (f == null)
			return false;
		try {
			BufferedImage img = ImageIO.read(f);
			canvas.setImage(img);
			file = f;
			scroll.updateUI();
			updateTitle();
			return true;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame,
					"Cannot load file \"" + f.getPath() + "\"", "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		return false;
	}

	public boolean doSave(boolean saveAs) {
		File f = file;
		if (saveAs || file == null) {
			f = getFile(true);
			if (f == null)
				return false;
			// just use PNG..
			String name = f.getName().toLowerCase();
			if (!name.endsWith(".png")) {
				if (name.contains("."))
					name = name.substring(0, name.lastIndexOf('.'));
				f = new File(f.getParentFile(), name + ".png");
			}
		}
		try {
			ImageIO.write(canvas.getRenderImage(), "PNG", f);
			file = f;
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public File getFile(final boolean save) {
		final JFileChooser fc = new JFileChooser(
				(file != null) ? file.getParent() : null);
		fc.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Image Files";
			}

			@Override
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;

				String[] filters;
				if (save)
					filters = ImageIO.getWriterFileSuffixes();
				else
					filters = ImageIO.getReaderFileSuffixes();
				String name = f.getName().toLowerCase();
				for (String s : filters)
					if (name.endsWith(s.toLowerCase()))
						return true;
				return false;
			}
		});
		int result;
		if (save)
			result = fc.showSaveDialog(frame);
		else
			result = fc.showOpenDialog(frame);
		if (result != JFileChooser.APPROVE_OPTION)
			return null;
		File f = fc.getSelectedFile();
		if (f == null || !save || !f.exists())
			return f;
		int o = JOptionPane.showConfirmDialog(fc, "File " + f.getName()
				+ " already exists. Replace?");
		if (o == JOptionPane.YES_OPTION)
			return f;
		if (o == JOptionPane.NO_OPTION)
			return getFile(save);
		return null;
	}
}
