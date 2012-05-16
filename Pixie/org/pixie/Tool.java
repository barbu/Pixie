package org.pixie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import org.pixie.ImageAction.FillAction;
import org.pixie.ImageAction.LineAction;
import org.pixie.ImageAction.PointAction;
import org.pixie.ImageAction.RectangleAction;

public interface Tool
	{
	void mousePress(MouseEvent e, Canvas c, Palette p);

	void mouseRelease(MouseEvent e, Canvas c, Palette p);

	void mouseMove(MouseEvent e, Canvas c, Palette p, boolean drag);

	void finish(Canvas c, Palette p);

	public static abstract class GenericTool<K extends ImageAction> implements Tool
		{
		protected K active;

		public void finish(Canvas c, Palette p)
			{
			if (active == null) return;
			c.acts.add(active);
			c.active = active = null;
			c.redrawCache();
			}

		public static boolean isValid(MouseEvent e, Canvas c, Palette pal)
			{
			if (pal != null && pal.getSelectedColor(e.getButton()) == null) return false;
			Point p = e.getPoint();
			if (p.x < 0 || p.y < 0) return false;
			Dimension d = c.getImageSize();
			if (p.x >= d.width || p.y >= d.height) return false;
			return true;
			}

		public void cancel(Canvas c)
			{
			c.active = active = null;
			c.repaint();
			}
		}

	public static class LineTool extends GenericTool<LineAction>
		{
		long mouseTime;
		int button;

		public void mousePress(MouseEvent e, Canvas canvas, Palette p)
			{
			if (active != null)
				{
				if (e.getButton() == button)
					finish(canvas,p);
				else
					cancel(canvas);
				return;
				}
			if (!isValid(e,canvas,p)) return;
			button = e.getButton();
			mouseTime = e.getWhen();
			canvas.active = active = new LineAction(e.getPoint(),p.getSelectedColor(button));
			canvas.repaint();
			}

		public void mouseRelease(MouseEvent e, Canvas canvas, Palette pal)
			{
			if (e.getWhen() - mouseTime < 200) return;

			if (active != null) active.p2 = e.getPoint();
			finish(canvas,pal);
			}

		public void mouseMove(MouseEvent e, Canvas canvas, Palette p, boolean drag)
			{
			if (active != null && !active.p2.equals(e.getPoint()))
				{
				Rectangle r = new Rectangle(active.p2); //previous value
				active.p2 = e.getPoint();
				r.add(active.p1);
				r.add(active.p2);
				canvas.repaint(r);
				}
			}
		}

	public static class PointTool extends GenericTool<PointAction>
		{
		public void mousePress(MouseEvent e, Canvas c, Palette p)
			{
			if (active != null)
				{
				cancel(c);
				return;
				}
			if (!isValid(e,c,p)) return;
			c.active = active = new PointAction(p.getSelectedColor(e.getButton()));
			active.add(e.getPoint());
			c.repaint();
			}

		public void mouseRelease(MouseEvent e, Canvas c, Palette p)
			{
			finish(c,p);
			}

		public void mouseMove(MouseEvent e, Canvas c, Palette p, boolean drag)
			{
			if (active != null && isValid(e,c,null))
				{
				Point pt = e.getPoint();
				if (!active.pts.isEmpty() && active.pts.getLast().equals(pt)) return;
				Rectangle r = new Rectangle(pt);
				if (!active.pts.isEmpty()) r.add(active.pts.getLast());
				active.add(pt);
				c.repaint(r);
				}
			}
		}

	public static class RectangleTool extends GenericTool<RectangleAction>
		{
		long mouseTime;
		int button;

		public void mousePress(MouseEvent e, Canvas canvas, Palette p)
			{
			if (active != null)
				{
				if (e.getButton() == button)
					finish(canvas,p);
				else
					cancel(canvas);
				return;
				}
			if (!isValid(e,canvas,p)) return;
			button = e.getButton();
			mouseTime = e.getWhen();
			Color c1 = p.getLeft();
			Color c2 = p.getRight();
			if (button != MouseEvent.BUTTON1)
				{
				c1 = c2;
				c2 = p.getLeft();
				}
			canvas.active = active = new RectangleAction(e.getPoint(),c1,c2);
			canvas.repaint();
			}

		public void mouseRelease(MouseEvent e, Canvas canvas, Palette pal)
			{
			if (e.getWhen() - mouseTime < 200) return;

			if (active != null) active.p2 = e.getPoint();
			finish(canvas,pal);
			}

		public void mouseMove(MouseEvent e, Canvas canvas, Palette p, boolean drag)
			{
			if (active != null && !active.p2.equals(e.getPoint()))
				{
				Rectangle r = new Rectangle(active.p2); //previous value
				active.p2 = e.getPoint();
				r.add(active.p1);
				r.add(active.p2);
				canvas.repaint(r);
				}
			}
		}

	public static class FillTool extends GenericTool<FillAction>
		{
		public void mousePress(MouseEvent e, Canvas c, Palette p)
			{
			if (active != null)
				{
				cancel(c);
				return;
				}
			if (!isValid(e,c,p)) return;
			c.active = active = new FillAction(c.getRenderImage(),e.getPoint(),
					p.getSelectedColor(e.getButton()),0);
			c.repaint();
			}

		public void mouseRelease(MouseEvent e, Canvas c, Palette p)
			{
			finish(c,p);
			}

		// nefolosit
		public void mouseMove(MouseEvent e, Canvas c, Palette p, boolean drag)
			{ // nefolosit
			}
		}
	}
