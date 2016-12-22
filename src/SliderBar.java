// Class SliderBar - editable colorbars component
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   ** May 1996
//
// 	$Id: SliderBar.java,v 1.6 1996/06/07 06:09:52 hynde Exp $	
//

import java.awt.*;

//public class SliderBar extends Canvas {
class SliderBar extends Canvas {

  public int    sectors  = 0;    /* number of sectors in bar */
  public Color  color[]  = null; /* colors of bars */
  public double amount[] = null; /* size of bar (between 0 and 1) */
  public double pos[]    = null; /* positions of bars (between 0 and 1) */

  private int selected_pos = -1;
  private boolean editable = true;

  private Dimension preferredSize;
  final private Dimension minimumSize = new Dimension(10, 30);

  private Color lightborder = Color.white, darkborder = Color.black;
  public boolean border = true;

  public SliderBar(Dimension prefSize, Color colors[]) {
    preferredSize = prefSize;
    sectors = colors.length;
    color = colors;
    self_init();
  }

  private void self_init() {
    int i;

    if (color == null) {
      color = new Color[sectors];
    }

    if (amount == null) {
      amount = new double[sectors];
    }
    
    if (pos == null) {
      pos = new double[sectors - 1];
      for (i = 1; i < sectors; i++)
	pos[i - 1] = (double)i / (double)sectors;
    }
    update_amounts();
  }

  // update sizes of sectors
  private void update_amounts() {
    int i;
    double prev = 0.0;

    for (i = 0; i < (sectors - 1); i++) {
      amount[i] = pos[i] - prev;
      prev = pos[i];
    }
    amount[sectors - 1] = 1.0 - prev;
  }
  
  public Dimension minimumSize() {
    return minimumSize;
  }

  public Dimension preferredSize() {
    return preferredSize;
  }

  public void update(Graphics g) {
    int i, p, p_old, size, offset;
    Dimension s = size();
    size = s.height; offset = 0;
    if (border) {
      size -= 2;
      offset = 1;
    }

    p_old = 0;
    for (i = 0; i < (sectors - 1); i++) {
      p = (int)(pos[i] * size);
      if (p == p_old) 
	continue;
      g.setColor(color[i]);
      g.fillRect(0, p_old + offset, s.width, p - p_old);
      p_old = p;
    }
    if (p_old < size) {
      g.setColor(color[i]);
      g.fillRect(0, p_old + offset, s.width, size - p_old);
    }

    // draw border
    if (border) {
      g.setColor(lightborder);
      g.drawLine(0, 0, 0, s.height - 1);
      g.drawLine(0, 0, s.width - 1, 0);
      g.setColor(darkborder);
      g.drawLine(s.width - 1, s.height - 1, s.width - 1, 1);
      g.drawLine(s.width - 1, s.height - 1, 1, s.height - 1);
    }
    //g.drawRect(0, 0, s.width - 1, s.height - 1);

  }
  
  public void paint(Graphics g) {
    update(g);
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

  public boolean mouseDown(Event e, int x, int y) {
    int size = size().height;
    int i, idx; 
    double p, diff, diffm;

    if (!editable)
      return true;

    p = (double)(y - 1) / (double)size;

    diffm = 10.0; idx = -1;
    
    for (i = 0; i < (sectors - 1); i++) {
      diff = Math.abs(pos[i] - p);
      if (diff < diffm) {
	diffm = diff; 
	idx = i;
      }
    }

    selected_pos = idx;
    mouseDrag(e, x, y);
    return true;
  }

  public boolean mouseDrag(Event e, int x, int y) {
    int size = size().height;
    int i;
    double p, diff, diffm;
    boolean ok;

    if ((selected_pos < 0) || !editable)
      return true;

    ok = true;
    p = (double)(y - 1) / (double)size;

    if (selected_pos < (sectors - 2)) {
      if (p >= pos[selected_pos + 1])
	ok = false;
    }
    if (selected_pos > 0) {
      if (p <= pos[selected_pos - 1])
	ok = false;
    }

    if (ok) {
      Event new_e = new Event(this, Event.ACTION_EVENT, this);
      pos[selected_pos] = p;
      update_amounts();
      repaint();
      deliverEvent(new_e);
    }
    return true;
  }

  public boolean mouseUp(Event e, int x, int y) {
    selected_pos = -1;
    if (editable) {
      Event new_e = new Event(this, Event.ACTION_EVENT, this);
      deliverEvent(new_e);
    }
    return true;
  }

  public void editable(boolean edit) {
    editable = edit;
    selected_pos = -1;
  }

  public synchronized void enable() {
    editable = true;
  }

  public synchronized void disable() {
    editable = false;
    selected_pos = -1;
  }
}
