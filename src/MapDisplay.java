// Class MapDisplay - SOM map displayer
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   ** May 1996
//
// 	$Id: MapDisplay.java,v 1.4 1996/06/28 09:26:03 hynde Exp $	
//

import java.awt.*;
import Sommable;

class MapDisplay extends Panel implements Sommable {

  public Dimension mapsize;
  public int num_vectors;
  public Dimension unitsize;
  public Vectorable vectors[][] = null;
  
  public int neigh = Sommable.NEIGH_BUBBLE;
  public int topol = Sommable.TOPOL_RECT;

  public MapDisplay(Vectorable vecs[][]) {
    super();
    int x, y;
    mapsize = new Dimension();
    mapsize.height = vecs.length;
    mapsize.width = vecs[0].length;

    vectors = vecs;
    num_vectors = mapsize.width * mapsize.height;

    setLayout(new GridLayout(mapsize.height, mapsize.width, 2, 2));
    for (y = 0; y < mapsize.height; y++)
      for (x = 0; x < mapsize.width; x++) {
	add((Component)vecs[y][x]);
      }

  }

  public void repaint() {
    int x, y;
    for (y = 0; y < mapsize.height; y++)
      for (x = 0; x < mapsize.width; x++) 
	((Component)vectors[y][x]).repaint();
  }
  
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

  private void teach_old(Vectorable vec, double alpha, double radius) {
    int winidx, y, x, wx, wy;
    double dist;
    radius *= radius;

    winidx = find_winner(vec);
    wy = winidx / mapsize.width;
    wx = winidx % mapsize.width;

    // only bubble for now
    for (y = 0; y < mapsize.height; y++)
      for (x = 0; x < mapsize.width; x++) {
	dist = (double)((x - wx) * (x - wx) + (y - wy) * (y - wy));
	if (dist <= radius)
	  vectors[y][x].adapt(vec, alpha);
	
      }

    return;
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
