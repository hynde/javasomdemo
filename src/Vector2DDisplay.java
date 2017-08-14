// Class Vector2DDisplay - component to display 2D vector
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   ** May 1996

import java.awt.*;

class Vector2DDisplay extends Canvas implements Vectorable {

  private int dimension;
  private double points[];
  private Object vec_class = null;

  public boolean border = true;
  public boolean editable = false;

  private Dimension preferredSize;
  final private Dimension minimumSize = new Dimension(10, 10);
  private boolean editing = false;
  private boolean mark = false;

  public Vector2DDisplay(Dimension prefSize) {
    preferredSize = prefSize;
    self_init();
  }

  private void self_init() {
    int i;
    dimension = 2;
    points = new double[dimension];
    for (i = 0; i < dimension; i++)
      points[i] = 0.0;
  }

  public Dimension minimumSize() {
    return preferredSize;
  }

  public Dimension preferredSize() {
    return preferredSize;
  }

  public void setpoints(double x, double y) {
    points[0] = x;
    points[1] = y;
  }

  public void paint(Graphics g) {
    int i, p, p_old, size, xc, yc;
    double x, y, x1, y1;
    Dimension s = size();
    Polygon poly = new Polygon();

    xc = s.width / 2;
    yc = s.height / 2;

    x = points[0];
    y = -points[1];

    poly.addPoint(xc + (int)(x * xc), yc + (int)(y * yc));
    poly.addPoint(xc + (int)(-y * 0.2 * xc), yc + (int)(0.2 * x * yc));
    poly.addPoint(xc - (int)(-y * 0.2 * xc), yc - (int)(0.2 * x * yc));

    if (border) 
      g.drawRect(0, 0, s.width - 1, s.height - 1);

    g.fillPolygon(poly);
  }

  public void setpoints(double pts[]) {
    points = pts;
    repaint();
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

    return true;
  }

  public boolean mouseDrag(Event e, int x, int y) {
    Dimension s;
    int xc, yc, w, h;
    if (!editing)
      return true;

    s = size();
    xc = s.width / 2;
    yc = s.height / 2;
    w = xc; h = -yc;

    points[0] = (double)(x - xc) / (double)w;
    points[1] = (double)(y - yc) / (double)h;
    repaint();
    return true;
  }

  public void editable(boolean edit) {
    editable = edit;
    if (!edit) 
      editing = false;
  }

  // Vectorable functions:

  public void set_class(Object c) {
    vec_class = c;
    // if it is a color, use it as background color
    if (c instanceof Color)
      setBackground((Color)c);
  }

  public Object get_class() {
    return vec_class;
  }

  public void set_points(double points[]) {
    this.points = points;
    dimension = points.length;
  }

  public double[] get_points() {
    return points;
  }

  public int dimension() {
    return dimension;
  }

  public void dimension(int d) {
    dimension = d;
  }

  public double distance(Vectorable other) {
    double dtmp, d = 0.0; 
    int i;
    double p2[] = other.get_points();
    
    for (i = 0; i < dimension; i++) {
      dtmp = points[i] - p2[i];
      d += dtmp * dtmp;
    }
    return d;
  }

  public void adapt(Vectorable other, double alpha) {
    int i;
    double o[] = other.get_points();
    double p[] = points;

    for (i = 0; i < dimension; i++) {
      p[i] += alpha * (o[i] - p[i]);
    }
  }

  public void randomize(double minimum, double maximum) {
    int i;
    double w = maximum - minimum;
    for (i = 0; i < dimension; i++) 
      points[i] = minimum + Math.random() * w;
  }

  public void set_mark(boolean mark) { this.mark = mark; }
  public boolean get_mark() { return mark; }

}
