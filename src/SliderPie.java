// Class SliderPie - editable pie
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   June 1996

import java.awt.*;

class SliderPie extends Canvas implements Vectorable {

  private boolean editable = true;
  public boolean border = true;
  public double center = 0.0;   // center angle (in degrees)
  public double width  = 90.0;  // arc length in (in degrees)

  private double angle[] = {0.0, 0.0 };

  private double rotrad = 0.75; /* using mouse inside rotrad times radius 
				   pixels from the center rotates pie, outside
				   moves sector's edges */
  private double arcrad = 0.75; /* the arc's width is 1.0 - arcrad times 
				   the radius of the component */

  final static public int PIEMODE_SECTOR = 1; // shows a sector in the circle
  final static public int PIEMODE_ARC = 2;    // shows an arc 
  private int piemode = PIEMODE_SECTOR;

  private Dimension preferredSize;
  final private Dimension minimumSize = new Dimension(10, 10);

  final static private int MODE_ROTATEPIE = 1; // move pie
  final static private int MODE_MOVESTART = 2;  // move left corner
  final static private int MODE_MOVEEND = 3; // move right corner
  private int editmode = 0;
  private boolean mark = false;

  // vector thingies
  private Object vec_class = null;
  private double points[] = {0.0, 0.0};
  private final int dimension = 2;
  
  private Color bgcolor = getBackground();

  public SliderPie(Dimension prefSize) {
    preferredSize = prefSize;
    set_angles();
  }

  public SliderPie(Dimension prefSize, double center, double width) {
    preferredSize = prefSize;
    this.center = center;
    this.width = width;
    set_angles();
  }

  public Dimension minimumSize() {
    return preferredSize;
  }

  public Dimension preferredSize() {
    return preferredSize;
  }

  public void paint(Graphics g) {
    Dimension s = size();


    g.setColor(getForeground());
    g.fillArc(0, 0, s.width - 1, s.height - 1, 
	      (int)angle[0], (int)(-width));

    if (piemode == PIEMODE_ARC) {
      int aw = (int)((s.width * 0.5) * (1.0 - arcrad));
      g.setColor(getBackground());
      g.fillArc(aw, aw, s.width - 1 - 2 * aw, s.height - 1 - 2 * aw, 
		(int)angle[0], (int)(-width));
    }

    if (border) {
      g.setColor(Color.black);
      if (piemode == PIEMODE_SECTOR)
	g.drawOval(0,0, s.width - 1, s.height - 1);
      else {
	int aw = (int)((s.width * 0.5) * (1.0 - arcrad) * 0.5);
	g.drawOval(aw, aw, s.width - 1 - 2 * aw, s.height - 1 - 2 * aw);
      }
    }

  }
  
  private void set_angles() {
    angle[0] = normalize_angle(center + 0.5 * width);
    angle[1] = normalize_angle(center - 0.5 * width);
  }

  public void set_angles(double start, double end) {
    angle[0] = normalize_angle(start);
    angle[1] = normalize_angle(end);
    set_center_width();
  }

  private void set_center_width() {
    width = normalize_angle(angle[0] - angle[1]);
    center = normalize_angle(angle[0] - 0.5 * width);
  }

  public void set_center_width(double c, double w) {
    center = normalize_angle(c);
    width = normalize_angle(w);
    set_angles();
  }

  private double[] get_angle(double x, double y) {
    double rtheta[] = { 1.0, 45.0 }, r;
    //System.out.println("mouse: " + x + " " + y);
    r = Math.atan2(y, x);
    //System.out.println("atan: " + r);
    rtheta[0] = Math.sqrt(x * x + y * y);
    rtheta[1] = r;
    //System.out.println("angle: " + rtheta[0] + " " + rtheta[1] + " ");
    if (rtheta[1] < 0.0) 
      rtheta[1] = 2.0 * Math.PI + rtheta[1];
    rtheta[1] *= 180.0 / Math.PI;
    //System.out.println("angle2: " + rtheta[0] + " " + rtheta[1] + " ");
    return rtheta;
  }

  private double normalize_angle(double r) {
    if (r < 0.0)
      r += 360.0;
    else if (r >= 360.0)
      r -= 360.0;
    return r;
  }

  private double angle_dist(double r1, double r2) {
    double r;

    r = Math.abs(r1 - r2);
    if (r > 180.0)
      r = 360.0 - r;

    return r;
  }

  public boolean mouseDown(Event e, int x, int y) {
    double x2, y2, w, h, rtheta[];
    Dimension s = size();
    if (!editable)
      return true;
    w = s.width / 2; h = s.height / 2;

    x2 = (double)(x - w) / (double)w;
    y2 = (double)(h - y) / (double)h;
    rtheta = get_angle(x2, y2);
    if (rtheta[0] < rotrad) {
      // using mouse near center rotates whole pie
      editmode = MODE_ROTATEPIE;
      center = rtheta[1];
      set_angles();
      //System.out.println("rotatepie");
    } else {
      // using mouse near radius moves start or end angle (whichever is closer)
      double rs, re;
      rs = angle_dist(rtheta[1], angle[0]);
      re = angle_dist(rtheta[1], angle[1]);
      if (rs < re) {
	editmode = MODE_MOVESTART;
	angle[0] = rtheta[1];
	//System.out.println("movestart");
      } else {
	editmode = MODE_MOVEEND;
	angle[1] = rtheta[1];
	//System.out.println("moveend");
      }
      set_center_width();
    }
    repaint();
    if (editable) {
      Event new_e = new Event(this, Event.ACTION_EVENT, this);
      deliverEvent(new_e);
    }
    return true;
  }

  public boolean mouseDrag(Event e, int x, int y) {
    double x2, y2, w, h, rtheta[];
    Dimension s = size();
    if (!editable)
      return true;

    w = s.width / 2; h = s.height / 2;

    x2 = (double)(x - w) / (double)w;
    y2 = (double)(h - y) / (double)h;
    rtheta = get_angle(x2, y2);
    if (editmode == MODE_ROTATEPIE) {
      center = rtheta[1];
      set_angles();
    } else if (editmode == MODE_MOVESTART) {
      angle[0] = rtheta[1];
      set_center_width();
    } else if (editmode == MODE_MOVEEND) {
      angle[1] = rtheta[1];
      set_center_width();
    }
    repaint();
    if (editable) {
      Event new_e = new Event(this, Event.ACTION_EVENT, this);
      deliverEvent(new_e);
    }
    return true;
  }

  public boolean mouseUp(Event e, int x, int y) {
    editmode = 0;
    if (editable) {
      Event new_e = new Event(this, Event.ACTION_EVENT, this);
      deliverEvent(new_e);
    }
    return true;
  }

  public void editable(boolean edit) {
    editable = edit;
  }

  public synchronized void enable() {
    editable = true;
  }

  public synchronized void disable() {
    editable = false;
  }

  public void setPieMode(int mode) { piemode = mode; }
   
  // Vectorable functions:

  public void set_class(Object c) {
    vec_class = c;
    // if it is a color, use it as background color
    if (c instanceof Color)
      setForeground((Color)c);
      //bgcolor = (Color)c;
  }

  public Object get_class() {
    return vec_class;
  }

  public void set_points(double points[]) {
    return;
  }

  public double[] get_points() {
    return points;
  }

  public void generate_new_vector() {
    generate_new_vector(true);
  }
  public void generate_new_vector(boolean variance) {
    double a = center; // center angle of sector
    double r = 1.0;    // radius of new point
    if (variance) {
      a += width * (Math.random() - 0.5);
      if (piemode == PIEMODE_SECTOR)
	r = Math.sqrt(Math.random());
    }
    a *= (Math.PI / 180.0);
    points[0] = r * Math.cos(a);
    points[1] = r * Math.sin(a);
  }

  public int dimension() {
    return dimension;
  }

  public void dimension(int d) {
    return;
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
    return;
  }

  public void randomize(double minimum, double maximum) {
    return;
  }

  public void set_mark(boolean mark) { this.mark = mark; }
  public boolean get_mark() { return mark; }

}
