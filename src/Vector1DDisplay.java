// Class Vector1DDisplay - component to display 1D vector
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   ** May 1996

import java.awt.*;

class Vector1DDisplay extends Canvas implements Vectorable {

  final private int dimension = 1;
  private double point = 0.0;
  private double point_prev = 0.0;
  private double points[] = {0.0};

  private Object vec_class = null;

  public Color prev_color = Color.yellow;
  public Color mark_color = Color.red;
  private Color lightborder = Color.white, darkborder = Color.black;

  public boolean showprev = true;
  public int     bordersize = 1;      // size of border (0 means no border)
  public boolean editable = false;
  public boolean events_drag = false;
  public boolean events_mouseup = true;

  private boolean editing = false;
  
  private Dimension preferredSize;
  private boolean mark = false;

  public Vector1DDisplay(Dimension prefSize) {
    preferredSize = prefSize;
    showprev = true;
  }

  public Vector1DDisplay(Dimension prefSize, boolean showprev) {
    preferredSize = prefSize;
    this.showprev = showprev;
  }

  public Dimension minimumSize() {
    return preferredSize;
  }

  public Dimension preferredSize() {
    return preferredSize;
  }

  private void set_colors() {
    set_colors(getBackground());
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

  // draw border
  private void drawBorder(Graphics g, Dimension s) {
    int i;
    if (bordersize > 0)
      for (i = 0; i < bordersize; i++)
	{
	  g.setColor(lightborder);
	  g.drawLine(i, i, i, s.height - i - 1);
	  g.drawLine(i, i, s.width - i - 1, i);
	  g.setColor(darkborder);
	  g.drawLine(s.width - i - 1, s.height - i - 1, s.width - i - 1, i + 1);
	  g.drawLine(s.width - i - 1, s.height - i - 1, i + 1, s.height - i - 1);
	}
  }

  public void paint(Graphics g) {
    int i, p, p_old, size, xc, yc;
    double x, y, x1, y1;
    Dimension s = size();
    Dimension s2;
    s2 = new Dimension(s.width - 2 * bordersize, s.height - 2 * bordersize);

    p = (int)((s2.height - 1) * (1.0 - point));

    // show difference to previous if wanted
    if (showprev) {
      g.setColor(prev_color);
      p_old = (int)((s2.height - 1) * (1.0 - point_prev));
      size = p_old - p;
      if (size > 0) 
	g.fillRect(2 * bordersize, p + bordersize, s2.width - 2 * bordersize, size);
      else if (size < 0)
	g.fillRect(2 * bordersize, p_old + bordersize, s2.width - 2 * bordersize, -size);
    }

    drawBorder(g, s);
    g.setColor(mark ? mark_color : getForeground());

    // draw vector
    p_old = p - bordersize;
    p += bordersize;
    if (p_old < 0)
      p_old = 0;
    if (p > (s2.height - 1))
      p = s2.height - 1;
    g.fillRect(2 * bordersize, p_old + bordersize, s2.width - 2 *bordersize, p - p_old);
  }

  public boolean mouseDown(Event e, int x, int y) {
    if (!editable)
      return true;
    editing = true;
    mouseDrag(e, x, y);
    return true;
  }

  public boolean mouseUp(Event e, int x, int y) {
    if (!editable)
      return true;
    editing = false;
    if (events_mouseup) {
      Event new_e = new Event(this, Event.ACTION_EVENT, this);
      deliverEvent(new_e);
    }

    return true;
  }

  public boolean mouseDrag(Event e, int x, int y) {
    Dimension s = size();
    double p;
    if (!editing)
      return true;

    x -= bordersize; y -= bordersize;
    s.width -= 2* bordersize; s.height -= 2 * bordersize;

    if ((x < 0) || (y < 0) || (x >= s.width) || (y >= s.height))
      return true;

    p = (double)(s.height - y - 1) / (double)(s.height - 1);
    point_prev = p;
    setpoint(p);
    set_mark(false);  // clear mark
    repaint();
    
    if (events_drag) {
      Event new_e = new Event(this, Event.ACTION_EVENT, this);
      deliverEvent(new_e);
    }

    return true;
  }

  public void editable(boolean edit) {
    editable = edit;
    if (!edit) 
      editing = false;
  }

  public void reset_history() {
    point_prev = point;
    set_mark(false);
  }

  private void setpoint(double x) {
    point = x;
    points[0] = point;
  }

  // Vectorable functions:

  public void set_class(Object c) {
    vec_class = c;
  }

  public Object get_class() {
    return vec_class;
  }

  public void set_points(double points[]) {
    setpoint(points[0]);
  }

  public double[] get_points() {
    return points;
  }

  public int dimension() {
    return dimension;
  }

  public void dimension(int d) {
    return;
  }

  public double distance(Vectorable other) {
    double d;
    double p2[] = other.get_points();
    
    d = point - p2[0];
    return d * d;
  }

  public void adapt(Vectorable other, double alpha) {
    int i;
    double o[] = other.get_points();
    double p = point;
    point_prev = p;

    p += alpha * (o[0] - p);
    setpoint(p);
  }

  public void randomize(double minimum, double maximum) {
    double w = maximum - minimum;
    setpoint(minimum + Math.random() * w);
    point_prev = point;
    set_mark(false); // clear mark
  }

  public void set_mark(boolean mark) { this.mark = mark; }
  public boolean get_mark() { return mark; }
    
}
