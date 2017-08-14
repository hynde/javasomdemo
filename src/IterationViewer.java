// Class IterationViewer - component to view alpha/radius
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   May 1996

import java.awt.*;

class IterationViewer extends Canvas {

  // constants:
  public static final int NONE = 0;
  //  LINEAR - linearly decreasing
  public static final int LINEAR = 1;

  public int iteration = 0;
  public int max_iterations = 100;

  public boolean running = true;
  private int prev_x = -1;
  public int mode = LINEAR;
  public Color mark_color = Color.red;
  private Color lightborder = Color.white, darkborder = Color.black;

  public boolean border = true;
  public boolean editable = true;
  public boolean events_drag = false;
  public boolean events_mouseup = true;
  public boolean stoppable_mouse = false;
  public boolean show_value = true;
  private boolean editing = false;
  
  private Dimension preferredSize;

  public IterationViewer(Dimension prefSize, int max) {
    preferredSize = prefSize;
    max_iterations = max;
  }

  public Dimension minimumSize() {
    return preferredSize;
  }

  public Dimension preferredSize() {
    return preferredSize;
  }

  private void set_colors(Color c) {
    if (c != null) {
      lightborder = c.brighter();
      darkborder = c.darker();
    }
  }

  public void setBackground(Color c) {
    super.setBackground(c);
    set_colors(c);
  }

  public void paint(Graphics g) {
    int w, offset, x;
    boolean new_border = false;
    Dimension s = size();
    w = s.width; offset = 0;

    if (border) {
      w -= 2;
      offset = 1;
    }

    if (mode == LINEAR) 
      g.drawLine(offset,offset, s.width - offset - 1, s.height - offset - 1);

    if (show_value) {
      // draw value
      String str = toString();
      int xp = 4, yp = 1;
      g.setColor(getForeground());
      yp += g.getFontMetrics().getDescent();
      g.drawString(str, xp, s.height - yp - 1);
    }

    g.setColor(mark_color);
    x = (int)((w - 1) * (double)iteration / (double)max_iterations);
    prev_x = x;
    g.drawLine(x + offset, offset, x + offset, s.height - 1 - offset);

    // draw border
    if (border) {
      if (new_border) {
	g.draw3DRect(0, 0, s.width, s.height, true);
      } else {
	g.setColor(lightborder);
	g.drawLine(0, 0, 0, s.height - 1);
	g.drawLine(0, 0, s.width - 1, 0);
	g.setColor(darkborder);
	g.drawLine(s.width - 1, s.height - 1, s.width - 1, 1);
	g.drawLine(s.width - 1, s.height - 1, 1, s.height - 1);
      }
    }
  }

  public boolean mouseDown(Event e, int x, int y) {
    if (!editable)
      return true;
    if (stoppable_mouse) {
      int nx = x;
      if (border)
	nx--;
      if (Math.abs(x - prev_x) < 16) {
	editing = true;
	mouseDrag(e, x, y);
      } else {
	// toggle running/stopped
	running = !running;
      }
    } else {
      editing = true;
      mouseDrag(e, x, y);
    }

    return true;
  }

  public boolean mouseUp(Event e, int x, int y) {
    if (!editable)
      return true;
    if (editing) {
      editing = false;
      if (events_mouseup) {
	Event new_e = new Event(this, Event.ACTION_EVENT, this);
	deliverEvent(new_e);
      }
    }
    return true;
  }

  public boolean mouseDrag(Event e, int x, int y) {
    Dimension s = size();
    int w, offset, iter;
    double p;
    if (!editing)
      return true;

    w = s.width; offset = 0;
    if (border) {
      w -= 2; offset = 1;
    }
    x -= offset;

    if ((x < 0) || (y < 0) || (x >= w) || (y >= s.height))
      return true;

    iter = (int)(max_iterations * ((double)x / (double)(w - 1)));
    iteration = iter;
    prev_x = x;
    repaint();
    
    if (events_drag) {
      Event new_e = new Event(this, Event.ACTION_EVENT, this);
      deliverEvent(new_e);
    }
    return true;
  }

  synchronized public void set_iteration(int iter) {
    Dimension s = size();
    int w, offset, x;
    double p;

    if (iter < 0) 
      iter = 0;
    if (iter > max_iterations)
      iter = max_iterations;
    iteration = iter;
    w = s.width; 
    if (border)
      w -= 2; 
    
    if (w > 0) {
      x = (int)((w - 1) * (double)iteration / (double)max_iterations);
      if (prev_x != x)
	repaint();
    } else {
      prev_x = -1;
    }
    
  }

  public void stop() {
    running = false;
  }

  public void start() {
    running = true;
  }

  public void reset() {
    set_iteration(0);
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  synchronized public int get_iteration() {
    if (running) {
      int iter = iteration + 1;
      if (iter <= max_iterations)
	set_iteration(iter);
    }

    return iteration;
  }

  public String toString() { return Integer.toString(iteration); }

  public synchronized void setmax(int m) {
    max_iterations = m;
    if (iteration > m)
      iteration = m;
    set_iteration(iteration);
  }      

  public boolean atend() {
    return (iteration >= max_iterations) ? true : false;
  }

  public void editable(boolean edit) {
    editable = edit;
    if (!edit) 
      editing = false;
  }

}

class AlphaViewer extends IterationViewer {

  double minalpha = 0.0, maxalpha = 1.0;

  public AlphaViewer(Dimension prefSize, int max, double maxalpha) {
    super(prefSize, max);
    this.maxalpha = maxalpha;
  }

  public AlphaViewer(Dimension prefSize, int max, double maxalpha, double minalpha) {
    super(prefSize, max);
    this.maxalpha = maxalpha;
    this.minalpha = minalpha;
  }

  public void setMinimum(double m) {
    minalpha = m;
  };

  public void setMaximum(double m) {
    maxalpha = m;
  };

  public double getMaximum() { return maxalpha; }
  public double getMinimum() { return minalpha; }
  
  public double get_alpha() {
    int iter = max_iterations - get_iteration();
    double d = (double)iter / (double)max_iterations;

    return d * (maxalpha - minalpha) + minalpha;
  }

  public synchronized void set_alpha(double alpha) {
    double a;
    int iter;

    a = (alpha - minalpha) / (maxalpha - minalpha);
    iter = (int)(max_iterations * (1.0 - a));
    set_iteration(iter);
  }

  public String toString() { 
    double a;
    int iter = max_iterations - iteration;
    double d = (double)iter / (double)max_iterations;

    a = d * (maxalpha - minalpha) + minalpha;
    return Double.toString(a); 
  }

}
