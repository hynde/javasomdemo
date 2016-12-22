//
// Applet SOMDemo3 - 
//                   
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   July 1996
//
// 	$Id: SOMDemo3.java,v 1.3 1996/07/18 10:18:49 hynde Exp $	
//

import java.applet.Applet;
import java.awt.*;

import Vectori;
import GridDisplay;
import IterationViewer;
//import Separator;
import Lock;

public class SOMDemo3 extends Applet implements Runnable {

  static final String rcsid = "$Id: SOMDemo3.java,v 1.3 1996/07/18 10:18:49 hynde Exp $";

  // Debugging stuff
  final static boolean DEBUG = false;
  //static int foo_counter = 1;
  String name = "SOMDemo3";
  static final private boolean print_stats = false;
  private long teach_time = 0, teach_iters = 0;

  static final private boolean setpriority = false;

  private Vectori mapvectors[][] = null;
  private Dimension mapsize = new Dimension(5,4);
  private boolean border = true;
  private Color lightborder = Color.white, darkborder = Color.black;

  private int num_generators;

  GridDisplay map = null;
  Thread animatorThread = null, scrupdateThread = null;
  private Lock maplock = new Lock();

  private boolean unitborder = false;
  private boolean init_done = false;
  boolean frozen = true;
  int delay, fps = 20;
  int update_delay = 1000;  // update screen once per second

  private double alpha = 0.1;
  private double radius = 3.0;
  private int iterations = 1000;

  private int topol = Sommable.TOPOL_RECT;
  private int neigh = Sommable.NEIGH_BUBBLE;

  private int randmode = GridDisplay.DISTR_RECT;

  // Font
  private int fontstyle   = Font.PLAIN;
  private String fontname = "Helvetica";
  private int fontsize    = 12;
  private Font font       = null;

  private ToggleButton RunAlpha, RunRadius;
  private Button ResetAlpha, ResetRadius;
  private AlphaViewer AlphaView = null, RadiusView = null;

  private TextField RadiusTF = null, AlphaTF = null;

  private ToggleButton RunTeach;
  private Button RandButton, ResetButton;
  private Choice distr_choice, topol_choice, neigh_choice;

  static String RunBS    = "Start";
  static String StopBS   = "Stop ";
  static String RandBS   = "Randomize map";
  static String ResetBS  = "Reset";
  static String AlphaS   = "Alpha:";
  static String RadiusS  = "Radius:";

  final private boolean use_mappanel = true;

  public void init() {
    int x, y;

    System.out.println(getAppletInfo());

    get_parameters();

    font = new Font(fontname, fontstyle, fontsize);
    setFont(font);
      
    mapvectors = new Vectori[mapsize.height][mapsize.width];
    for (y = 0; y < mapsize.height; y++)
      for (x = 0; x < mapsize.width; x++) {
	mapvectors[y][x] = new Vectori(2);
      }
    
    AlphaView = new AlphaViewer(new Dimension(200, 24), iterations, alpha);
    RadiusView = new AlphaViewer(new Dimension(200, 24), iterations, radius, 1.0);
    AlphaTF  = new TextField(Double.toString(alpha), 4);
    RadiusTF = new TextField(Double.toString(radius), 4);
    do_layout();

    reset();
    if (DEBUG)
      System.out.println(name + ": init done");
  }

  public String name() {
    return Thread.currentThread().getName();
  }

  // parse applet parameters
  private void get_parameters() {
    String str;

    // teaching iterations per second
    if ((str = getParameter("ips")) != null) {
      fps = Integer.parseInt(str);
      if (fps <= 0)
	fps = 5;
    }
    set_fps(fps);

    // screen update delay (milliseconds between map displays)
    if ((str = getParameter("delay")) != null) {
      update_delay = Integer.parseInt(str);
      if (update_delay <= 40)
	update_delay = 40;
    }

    // size of map, width
    if ((str = getParameter("mapw")) != null) {
      mapsize.width = Integer.parseInt(str);
      if (mapsize.width < 1)
	mapsize.width = 1;
    }

    // size of map, height
    if ((str = getParameter("maph")) != null) {
      mapsize.height = (str != null) ? Integer.parseInt(str) : 0;
      if (mapsize.height < 1)
	mapsize.height = 1;
    }

    // alpha
    try {
      if ((str = getParameter("alpha")) != null) 
	alpha = Double.valueOf(str).doubleValue();
    } catch (NumberFormatException e) {
    };

    // radius
    try {
      if ((str = getParameter("radius")) != null)
	radius = Double.valueOf(str).doubleValue();
    } catch (NumberFormatException e) {
    };

    // number of iterations in teaching process
    if ((str = getParameter("maxiter")) != null) {
      int i;
      i = Integer.parseInt(str);
      if (i < 0) 
	i = 100;
      iterations = i;
    }

    
    // Texts for buttons and labels for localization

    // START
    if ((str = getParameter("txtstart")) != null)
      RunBS = str;
    // STOP
    if ((str = getParameter("txtstop")) != null)
      StopBS = str;
    // RANDOMIZE
    if ((str = getParameter("txtrand")) != null)
      RandBS = str;

    // RESET
    if ((str = getParameter("txtreset")) != null)
      ResetBS = str;

    // ALPHA
    if ((str = getParameter("txtalpha")) != null)
      AlphaS = str;

    // RADIUS
    if ((str = getParameter("txtradius")) != null)
      RadiusS = str;

    // Font
    if ((str = getParameter("fontname")) != null)
      fontname = str;
    if ((str = getParameter("fontsize")) != null) {
      int size;
      size = Integer.parseInt(str);
      if (size > 0)
	fontsize = size;
    }

    // map topology type
    if ((str = getParameter("topol")) != null) {
      if (str.equalsIgnoreCase("hexa"))
	topol = Sommable.TOPOL_HEXA;
      else if (str.equalsIgnoreCase("rect"))
	topol = Sommable.TOPOL_RECT;
    }

    // map neighborhood type
    if ((str = getParameter("neigh")) != null) {
      if (str.equalsIgnoreCase("bubble"))
	neigh = Sommable.NEIGH_BUBBLE;
      else if (str.equalsIgnoreCase("gaussian"))
	neigh = Sommable.NEIGH_GAUSSIAN;
    }

    // distribution type
    if ((str = getParameter("distr")) != null) {
      if (str.equalsIgnoreCase("square"))
	randmode = GridDisplay.DISTR_RECT;
      else if (str.equalsIgnoreCase("triangle"))
	randmode = GridDisplay.DISTR_TRI;
      else if (str.equalsIgnoreCase("circle"))
	randmode = GridDisplay.DISTR_CIRC;
    }

  }

  private void set_fps(int f) {
    if (f <= 0)
      fps = 1;
    else 
      fps = f;
    delay = (fps > 0) ? (1000 / fps) : 100;
  }

  private void do_layout() {
    double cent, wid;
    int i;

    map = new GridDisplay((Vectorable[][])mapvectors);
    map.randomize(0.0, 1.0);
    map.setMinMax(-0.1, 1.1, -0.1, 1.1);
    map.setBackground(Color.white);
    map.topology(topol);
    map.neighborhood(neigh);
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridbag);

    c.fill = GridBagConstraints.BOTH;
    //c.gridx++;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    //c.gridheight = GridBagConstraints.REMAINDER; //end column

    c.insets = new Insets(3,3,3,3);
    gridbag.setConstraints(map, c);
    add(map);

    Separator sep = new Separator(Separator.HORIZONTAL);
    //c.weighty = 0.1;
    c.weighty = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridbag.setConstraints(sep, c);
    add(sep);

    Panel p = make_alpharad();
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    //c.fill = GridBagConstraints.BOTH;
    //c.weighty = 0.3;
    gridbag.setConstraints(p, c);
    add(p);

    Separator sep2 = new Separator(Separator.HORIZONTAL);
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    //c.weighty = 0.1;
    gridbag.setConstraints(sep2, c);
    add(sep2);


    Panel cp = make_controlpanel();
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    gridbag.setConstraints(cp, c);
    //c.weighty = 0.3;
    add(cp);

    
    //validate();
  }

  // make alpha and radius control panel
  private Panel make_alpharad() {
    Panel p = new Panel();
    Label l1, l2;
    Button b1, b2;

    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    p.setLayout(gb);

    //p.setFont(font);
    c.insets = new Insets(1,2,1,2);

    // alpha
    c.gridwidth = 1;
    
    l1 = new Label(AlphaS);
    gb.setConstraints(l1, c);
    p.add(l1);

    gb.setConstraints(AlphaTF, c);
    p.add(AlphaTF);

    gb.setConstraints(AlphaView, c);
    p.add(AlphaView);
    
    RunAlpha = new ToggleButton(RunBS, StopBS, true);
    gb.setConstraints(RunAlpha, c);
    p.add(RunAlpha);

    c.gridwidth = GridBagConstraints.REMAINDER;
    ResetAlpha = new Button(ResetBS);
    gb.setConstraints(ResetAlpha, c);
    p.add(ResetAlpha);

    // radius
    c.gridwidth = 1;
    
    l2 = new Label(RadiusS);
    gb.setConstraints(l2, c);
    p.add(l2);

    gb.setConstraints(RadiusTF, c);
    p.add(RadiusTF);

    gb.setConstraints(RadiusView, c);
    p.add(RadiusView);
    
    RunRadius = new ToggleButton(RunBS, StopBS, true);
    gb.setConstraints(RunRadius, c);
    p.add(RunRadius);

    c.gridwidth = GridBagConstraints.REMAINDER;
    ResetRadius = new Button(ResetBS);
    gb.setConstraints(ResetRadius, c);
    p.add(ResetRadius);
    
    return p;
  };

  // make control panel 
  private Panel make_controlpanel() {
    Panel p = new Panel();
    Label l1, l2;
    Button b1, b2;

    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    p.setLayout(gb);
    //p.setFont(font);

    c.insets = new Insets(1,2,1,2);

    // alpha
    c.gridwidth = 1;
    
    distr_choice = new Choice();
    distr_choice.addItem("Square");
    distr_choice.addItem("Triangle");
    distr_choice.addItem("Circle");
    gb.setConstraints(distr_choice, c);
    if (randmode >= 0)
      map.setDistr(randmode);
    p.add(distr_choice);

    topol_choice = new Choice();
    topol_choice.addItem("Hexa");
    topol_choice.addItem("Rect");
    topol_choice.select((topol == Sommable.TOPOL_HEXA) ? 0 : 1);
    gb.setConstraints(topol_choice, c);
    p.add(topol_choice);

    neigh_choice = new Choice();
    neigh_choice.addItem("Gauss ");
    neigh_choice.addItem("Bubble");
    neigh_choice.select((neigh == Sommable.NEIGH_GAUSSIAN) ? 0 : 1);
    gb.setConstraints(neigh_choice, c);
    p.add(neigh_choice);


    RunTeach = new ToggleButton(StopBS, RunBS, frozen);
    gb.setConstraints(RunTeach, c);
    p.add(RunTeach);

    RandButton = new Button(RandBS);
    gb.setConstraints(RandButton, c);
    p.add(RandButton);

    ResetButton = new Button(ResetBS);
    c.gridwidth = GridBagConstraints.REMAINDER;
    gb.setConstraints(ResetButton, c);
    p.add(ResetButton);
    
    return p;
  };
  
  public void start() {
    if (DEBUG)
      System.out.println(name + ": start");
    if (!frozen)
      start_thread();
  }

  public void stop() {
    if (DEBUG)
      System.out.println(name + ": stop");
    stop_thread();
    
  }

  // start thread
  synchronized private void start_thread() {
    maplock.unlock();
    if (animatorThread == null) {
      animatorThread = new Thread(this, name + " teaching");
      //animatorThread.setPriority(Thread.MIN_PRIORITY);
      animatorThread.start();
      if (print_stats) {
	teach_iters = 0; teach_time = 0;
      }
    }

    if (scrupdateThread == null) {
      scrupdateThread = new Thread(this, name + "screenupdate");
      //scrupdateThread.setPriority(Thread.MIN_PRIORITY);
      scrupdateThread.start();
    }

  }

  // stop thread
  synchronized private void stop_thread() {
    Thread ta = animatorThread, tu = scrupdateThread;

    if (ta != null) {
      ta.stop();
      animatorThread = null;
    }

    if (tu != null) {
      tu.stop();
      scrupdateThread = null;
    }
    maplock.unlock();

    if (print_stats) {
      double ips;
      if (teach_iters > 0) 
	ips = (double)teach_time / (double)teach_iters;
      else 
	ips = 0.0;
      System.out.println("avg. teach time: " + ips + " msec, " + teach_iters + " iterations");
      teach_iters = 0; teach_time = 0;
    }
  }
  
  // animation thread
  public void run() {
    // do whichever thread we are running 
    if (Thread.currentThread() == animatorThread) {
      if (DEBUG)
	System.out.println(name() + ": start teach");
      teach_thread();
      if (DEBUG)
	System.out.println(name() + ": done teach");
    }
    else if (Thread.currentThread() == scrupdateThread) {
      if (DEBUG)
	System.out.println(name() + ": start update");
      scrupdate_thread();
      if (DEBUG)
	System.out.println(name() + ": done update");
    }
  }
  
  // set alphas etc to default values:
  private void reset() {
    reset_radius();
    reset_alpha();
  }

  private void reset_alpha() {
    AlphaView.reset();
    AlphaView.start();
  }

  private void reset_radius() {
    RadiusView.reset();
    RadiusView.start();
  }
  
  private void teach_thread() {
    //Remember the starting time.
    long startTime;
    double rnd, alp, rad;
    int i, winneridx;
    Vectori tvec = new Vectori(2);

    if (setpriority)
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    while (Thread.currentThread() == animatorThread) {
      startTime = System.currentTimeMillis();
      
      // Get new teaching vector

      //tvec.randomize(0.0, 1.0);
      get_newvalue(tvec, randmode);

      // pie is the teaching vector
      
      alp = AlphaView.get_alpha();
      rad = RadiusView.get_alpha();

      maplock.lock(); // reserve map
      map.teach(tvec, alp, rad);
      maplock.unlock(); // unreserve map

      if (AlphaView.atend() && RadiusView.atend()) {
	animatorThread = null;
	scrupdateThread = null;
	frozen = true;
	RunTeach.setState(frozen);
      }

      //Delay depending on how far we are behind.
      startTime -= System.currentTimeMillis();
      // stats for speed
      if (print_stats) {
	teach_time -= startTime; teach_iters++;
      }
      while (startTime <= 0)
	startTime += delay;
      
      try {
	Thread.sleep(startTime);
      } catch (InterruptedException e) {
	break;
      }
    }
    
  }

  // thread to update the map display at regular intervals
  private void scrupdate_thread() {
    
    //Remember the starting time.
    long startTime;
    //int i, winneridx, frameNumber = 0;

    while (Thread.currentThread() == scrupdateThread) {
      startTime = System.currentTimeMillis();

      // lock map for drawing (doesn't actually help because repaint()
      //  only schedules the repaint to be done as soon as possible)
      maplock.lock();
      map.repaint();
      maplock.unlock();

      //Delay depending on how far we are behind.
      startTime -= System.currentTimeMillis();
      while (startTime <= 0)
	startTime += update_delay;
      
      try {
	Thread.sleep(startTime);
      } catch (InterruptedException e) {
	break;
      }
    }

  }

  public void destroy() {
    stop_thread();
    mapvectors = null;
    AlphaView = null;
    removeAll();
    frozen = true;
    if (DEBUG)
      System.out.println(name + ": destroy");
  }

  public void paint(Graphics g) {
    Dimension s = size();

    // paint a border around the applet
    if (border) {
      g.setColor(lightborder);
      g.drawLine(0, 0, 0, s.height - 1);
      g.drawLine(0, 0, s.width - 1, 0);
      g.setColor(darkborder);
      g.drawLine(s.width - 1, s.height - 1, s.width - 1, 1);
      g.drawLine(s.width - 1, s.height - 1, 1, s.height - 1);
    }
  }

  public boolean action(Event e, Object arg) {
    boolean state;
    if (e.target == RunAlpha) {
      state = RunAlpha.toggleState();
      AlphaView.setRunning(state);
      return true;
    } else if (e.target == RunRadius) {
      state = RunRadius.toggleState();
      RadiusView.setRunning(state);
      return true;
    } else if (e.target == ResetRadius) {
      reset_radius();
      RunRadius.setState(true);
      return true;
    } else if (e.target == ResetAlpha) {
      reset_alpha();
      RunAlpha.setState(true);
      return true;
    } else if (e.target == AlphaTF) {
      double val;
      String str;

      try {
	val = Double.valueOf(AlphaTF.getText()).doubleValue();
	AlphaView.setMaximum(val);
	alpha = val;
	if (DEBUG)
	  System.out.println("alpha set to " + AlphaView.getMaximum());
      } catch (NumberFormatException ex) {
      };
      return true;
      
    } else if (e.target == RadiusTF) {
      double val;
      String str;

      try {
	val = Double.valueOf(RadiusTF.getText()).doubleValue();
	RadiusView.setMaximum(val);
	radius = val;
	if (DEBUG)
	  System.out.println("radius set to " + RadiusView.getMaximum());
      } catch (NumberFormatException ex) {
      };
      return true;
    } else if (e.target == RandButton) {
      // first stop
      if (!frozen) {
	frozen = true;
	RunTeach.setState(frozen);
	stop_thread();
      }
      map.randomize(0.0, 1.0);
      map.repaint();
      return true;
    } else if (e.target == RunTeach) {
      
      if (frozen) {
	frozen = false;
	start_thread();
      } else {
	stop_thread();
	frozen = true;
      }
      RunTeach.setState(frozen);
      return true;
    } else if (e.target == ResetButton) {
      reset();
      RunAlpha.setState(true);
      RunRadius.setState(true);
      return true;
    } else if (e.target == distr_choice) {
      randmode = distr_choice.getSelectedIndex();
      map.setDistr(randmode);
      map.repaint();
      return true;
    } else if (e.target == topol_choice) {
      map.topology((topol_choice.getSelectedIndex() == 0) ? Sommable.TOPOL_HEXA : Sommable.TOPOL_RECT);
      map.repaint();
      return true;
    } else if (e.target == neigh_choice) {
      map.neighborhood((neigh_choice.getSelectedIndex() == 0) ? Sommable.NEIGH_GAUSSIAN : Sommable.NEIGH_BUBBLE);
      map.repaint();
      return true;
    }
    return false;
  }

  // get new teaching vector
  private void get_newvalue(Vectorable vec, int type) {
    double[] points = vec.get_points();
    if (type == GridDisplay.DISTR_TRI) {
      // triangle distribution
      points[0] = Math.random();
      points[1] = Math.random();
      if (points[1] > points[0]) {
	double tmp = points[0];
	points[0] = points[1];
	points[1] = tmp;
      }
      points[0] -= (0.5  * points[1]);
    } else if (type == GridDisplay.DISTR_CIRC) {
      // circle distribution
      double angl = Math.random() * 2.0 * Math.PI;
      double rad = Math.sqrt(Math.random());
      points[0] = Math.cos(angl) * rad * 0.5 + 0.5;
      points[1] = Math.sin(angl) * rad * 0.5 + 0.5;
    } else {
      // square distribution
      points[0] = Math.random();
      points[1] = Math.random();
    }
  }


  final static private String[][] parameterinfo = {
    { "ips",      "int",    "teaching iterations per second" },
    { "mapw",     "int",    "map's width" },
    { "maph",     "int",    "map's height" },
    { "alpha",    "float",  "teaching parameter alpha" },
    { "radius",   "float",  "teaching neighborhood radius" },
    { "maxiter",  "int",    "number of teaching iterations" },
    { "topol",    "string", "map topology (RECT or HEXA)" },
    { "neigh",    "string", "neighborhood type (BUBBLE or GAUSSIAN)" },
    { "distr",    "string", "data distr. type (SQUARE / TRIANGLE / CIRCLE)" },
    { "fontname", "string", "font name" },
    { "fontsize", "int",    "font size" },
    { "txtstart", "string", "text for runstop buttons (start)" },
    { "txtstop",  "string", "text for runstop buttons (stop)" },
    { "txtrand",  "string", "text for randomize button" },
    { "txtreset", "string", "text for reset button" },
    { "txtalpha", "string", "text for alpha label" },
    { "txtradius", "string", "text for radius label" }};
  
  public String[][] getParameterInfo() {
    return parameterinfo;
  }

  public String getAppletInfo() {
    String info = "SOMDemo3 - Yet another Self-Organizing Map Demo\n";
    info = info + " Author: Jussi Hynninen (http://www.iki.fi/~hynde/)\n";
    info = info + " version: " + rcsid;
    return info;
  }

}

