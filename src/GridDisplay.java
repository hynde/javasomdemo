// Class GridDisplay - SOM map displayer, shows a one or two dimensional map
//                     with 2-dimensional vectors as a grid
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   June 1996
//
// 	$Id: GridDisplay.java,v 1.3 1996/07/05 12:34:26 hynde Exp $	
//

import java.awt.*;
import Sommable;

class GridDisplay extends Canvas implements Sommable {

  public Dimension mapsize;
  public int num_vectors;
  public Dimension unitsize;
  public Vectorable vectors[][] = null;
  public double minx, miny, maxx, maxy;

  public int neigh = Sommable.NEIGH_BUBBLE;
  public int topol = Sommable.TOPOL_RECT;
  public Color unit_color = Color.red;
  public boolean show_units = true;

  public static final int DISTR_NONE = -1;
  public static final int DISTR_RECT = 0;
  public static final int DISTR_TRI = 1;
  public static final int DISTR_CIRC = 2;

  public int distr_type = DISTR_NONE;
  public boolean show_distr = true;
  public Color distr_color = new Color(192, 208, 255);

  public GridDisplay(Vectorable vecs[][]) {
    super();
    int x, y;
    mapsize = new Dimension();
    mapsize.height = vecs.length;
    mapsize.width = vecs[0].length;

    vectors = vecs;
    num_vectors = mapsize.width * mapsize.height;

    minx = 0.0; miny = 0.0; maxx = 1.0; maxy = 1.0;
  }

  public void setMinMax(double minx, double maxx, double miny, double maxy) {
    this.minx = minx;
    this.miny = miny;
    this.maxx = maxx;
    this.maxy = maxy;
  }

  public void paint(Graphics g) {
    int x, y;
    int xn, yn, xp, yp;
    Dimension s = size();
    double[] p;

    if (show_distr)
      paint_distr(g);

    if (mapsize.height > 0) 
      for (y = 0; y < mapsize.height; y++) {
	p = vectors[y][0].get_points();
	xp = (int)((p[0] - minx) / (maxx - minx) * s.width);
	yp = (int)((p[1] - miny) / (maxy - miny) * s.height);
	for (x = 1; x < mapsize.width; x++) {
	  p = vectors[y][x].get_points();
	  xn = (int)((p[0] - minx) / (maxx - minx) * s.width);
	  yn = (int)((p[1] - miny) / (maxy - miny) * s.height);
	  g.drawLine(xp, s.height - yp, xn, s.height - yn);
	  xp = xn; yp = yn;
	}
      }

    if (mapsize.width > 1) 
      for (x = 0; x < mapsize.width; x++) {
	p = vectors[0][x].get_points();
	xp = (int)((p[0] - minx) / (maxx - minx) * s.width);
	yp = (int)((p[1] - miny) / (maxy - miny) * s.height);
	for (y = 1; y < mapsize.height; y++) {
	  p = vectors[y][x].get_points();
	  xn = (int)((p[0] - minx) / (maxx - minx) * s.width);
	  yn = (int)((p[1] - miny) / (maxy - miny) * s.height);
	  g.drawLine(xp, s.height - yp, xn, s.height - yn);
	  xp = xn; yp = yn;
	}
      }

    if ((mapsize.width > 1) && (mapsize.height > 1) && (topol == Sommable.TOPOL_HEXA))
      for (x = 1; x < mapsize.width; x++) {
	p = vectors[0][x].get_points();
	xp = (int)((p[0] - minx) / (maxx - minx) * s.width);
	yp = (int)((p[1] - miny) / (maxy - miny) * s.height);
	for (y = 1; y < mapsize.height; y++) {
	  p = vectors[y][x - (y % 2)].get_points();
	  xn = (int)((p[0] - minx) / (maxx - minx) * s.width);
	  yn = (int)((p[1] - miny) / (maxy - miny) * s.height);
	  g.drawLine(xp, s.height - yp, xn, s.height - yn);
	  xp = xn; yp = yn;
	}
      }

    if (show_units) {
      g.setColor(unit_color);
      for (y = 0; y < mapsize.height; y++) {
	for (x = 0; x < mapsize.width; x++) {
	  p = vectors[y][x].get_points();
	  xn = (int)((p[0] - minx) / (maxx - minx) * s.width);
	  yn = (int)((p[1] - miny) / (maxy - miny) * s.height);
	  g.drawLine(xn, s.height - yn, xn, s.height - yn);
	}
      }
    }
  }

  private void paint_distr(Graphics g) {
    Dimension s = size();
    int xp0, yp0, xp1, yp1;
    g.setColor(distr_color);
    
    xp0 = (int)((0.0 - minx) / (maxx - minx) * s.width);
    yp0 = (int)((0.0 - miny) / (maxy - miny) * s.height);

    xp1 = (int)((1.0 - minx) / (maxx - minx) * s.width);
    yp1 = (int)((1.0 - miny) / (maxy - miny) * s.height);
    
    switch (distr_type) {
    case DISTR_RECT:
      g.fillRect(xp0, s.height - yp1, xp1 - xp0, yp1 - yp0);
      break;
    case DISTR_TRI:
      Polygon p = new Polygon();
      p.addPoint(xp0, s.height - yp0);
      p.addPoint(xp1, s.height - yp0);
      p.addPoint((xp0 + xp1) / 2, s.height - yp1);
      g.fillPolygon(p);
      break;
    case DISTR_CIRC:
      g.fillOval(xp0, s.height - yp1, xp1 - xp0, yp1 - yp0);
      break;
    default:
      break;
    }
    g.setColor(getForeground());
  }

  public void setDistr(int d) { distr_type = d; }

  // SOMMABLE functions

  public void topology(int topol) {
    this.topol = topol;
  }

  public int topology() {
    return topol;
  }

  public void neighborhood(int neigh) {
    this.neigh = neigh;
  }

  public int neighborhood() {
    return neigh;
  }

  public Dimension mapsize() {
    return mapsize;
  }

  public int find_winner(Vectorable vec) {
    int x, y, px, py, idx;
    double diff, diffm;
    
    idx = -1; px = -1; py = -1; diffm = Double.MAX_VALUE;
    for (y = 0; y < mapsize.height; y++)
      for (x = 0; x < mapsize.width; x++) {
	diff = vec.distance(vectors[y][x]);
	if (diff < diffm) {
	  if (idx >= 0)
	    vectors[py][px].set_mark(false);
	  idx = y * mapsize.width + x;
	  diffm = diff;
	  px = x; py = y;
	  vectors[y][x].set_mark(true);
	} else
	  vectors[y][x].set_mark(false);
      }
    
    return idx;
  }

  public void randomize(double minimum, double maximum) {
    int x, y;
    for (y = 0; y < mapsize.height; y++)
      for (x = 0; x < mapsize.width; x++) {
	vectors[y][x].randomize(minimum, maximum);
      }
  }

  public void teach(Vectorable vec, double alpha, double radius) {
    int winidx, y, x, wx, wy;
    double dist, rad, alp;
    radius *= radius;
    
    rad = -0.5 / radius;
    winidx = find_winner(vec);
    wy = winidx / mapsize.width;
    wx = winidx % mapsize.width;

    for (y = 0; y < mapsize.height; y++)
      for (x = 0; x < mapsize.width; x++) {
	dist = (topol == Sommable.TOPOL_RECT) ? rect_dist(wx, wy, x, y) : hexa_dist(wx, wy, x, y);
	if (neigh == Sommable.NEIGH_BUBBLE) {
	  // BUBBLE neighborhood
	  if (dist <= radius)
	    vectors[y][x].adapt(vec, alpha);
	} else {
	  // GAUSSIAN neighborhood
	  alp = alpha * Math.exp(dist * rad);
	  vectors[y][x].adapt(vec, alp);
	}
      }
    
    return;
  }

  private double hexa_dist(int x1, int y1, int x2, int y2) {
    double diff;

    diff = (double)(x1 - x2);
    if (((y1 - y2) % 2) != 0) {
      if ((y1 % 2) == 0) {
	diff -= 0.5;
      }
      else {
	diff += 0.5;
      }
    }
    diff *= diff;
    diff += 0.75 * (y1 - y2) * (y1 - y2);
    return diff;
  }

  private double rect_dist(int x1, int y1, int x2, int y2) {
    int dist;
    dist = (x1 - x2) * (x1 - x2);
    dist += (y1 - y2) * (y1 - y2);
    return (double)dist;
  }

  public Vectorable get_vector(int idx) {
    int x, y;
    y = idx / mapsize.width;
    x = idx % mapsize.width;
    return vectors[y][x];
  }    

  public void classify(Sommable other) {
    int x, y, windex;
    Vectorable vec;
    for (y = 0; y < mapsize.height; y++)
      for (x = 0; x < mapsize.width; x++) {
	windex = other.find_winner(vectors[y][x]);
	vec = other.get_vector(windex);
	vectors[y][x].set_class(vec.get_class());
      }
  }
  
}
