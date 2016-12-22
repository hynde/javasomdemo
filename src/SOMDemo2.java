//
// Applet SOMDemo2 - Demonstration of the Self-Organizing Map algorithm,
//                   a more complex version with a 2D map.
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   ** May 1996
//
// 	$Id: SOMDemo2.java,v 1.24 1996/07/04 08:28:29 hynde Exp $	
//

import java.applet.Applet;
import java.awt.*;

import SliderBar;
import Vector2DDisplay;
import SliderPie;
import MapDisplay;
import IterationViewer;

public class SOMDemo2 extends Applet implements Runnable {

  static final String rcsid = "$Id: SOMDemo2.java,v 1.24 1996/07/04 08:28:29 hynde Exp $";

  // Debugging stuff
  final static boolean DEBUG = false;
  //static int foo_counter = 1;
  String name = "SOMDemo2";

  final private boolean setpriority = false;

  SliderBar nappi = null;
  Vector2DDisplay mapvectors[][] = null;

  private boolean border = true;
  Color lightborder = Color.white, darkborder = Color.black;

  Color nappicolors[] = {Color.red, Color.yellow, Color.green, Color.blue};
  SliderPie generators[] = null;
  int num_generators;
  MapDisplay generatormap = null;

  Dimension mapsize = new Dimension(5, 4);
  Dimension unitsize = new Dimension(60, 60);
  
  MapDisplay map = null;
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

  // pie mode
  private int piemode = SliderPie.PIEMODE_SECTOR;

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

  static String RunBS    = "Start";
  static String StopBS   = "Stop ";
  static String RandBS   = "Randomize map";
  static String ResetBS  = "Reset";
  static String ClassesS = "Classes";
  static String ProsS    = "%";
  static String MapS     = "Map";
  static String AlphaS   = "Alpha:";
  static String RadiusS  = "Radius:";

  final private boolean use_mappanel = true;

  public void init() {
    int x, y;

    System.out.println(getAppletInfo());

    get_parameters();

    font = new Font(fontname, fontstyle, fontsize);
    setFont(font);
      
    num_generators = nappicolors.length;
      
    //mapsize.width = 5; mapsize.height = 4;
    mapvectors = new Vector2DDisplay[mapsize.height][mapsize.width];
    for (y = 0; y < mapsize.height; y++)
      for (x = 0; x < mapsize.width; x++) {
	mapvectors[y][x] = new Vector2DDisplay(unitsize);
	mapvectors[y][x].editable(true);
	mapvectors[y][x].border = unitborder; // to border or not to border
      }
    
    AlphaView = new AlphaViewer(new Dimension(200, 24), iterations, alpha);
    RadiusView = new AlphaViewer(new Dimension(200, 24), iterations, radius, 1.0);
    AlphaTF  = new TextField(Double.toString(alpha), 4);
    RadiusTF = new TextField(Double.toString(radius), 4);
    do_layout();

    map.classify(generatormap);

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

    // frames per second
    if ((str = getParameter("ips")) != null) {
      fps = Integer.parseInt(str);
      if (fps <= 0)
	fps = 5;
    }
    set_fps(fps);

    // update delay
    if ((str = getParameter("delay")) != null) {
      update_delay = Integer.parseInt(str);
      if (update_delay <= 10)
	update_delay = 10;
    }

    // size of map
    if ((str = getParameter("mapw")) != null) {
      mapsize.width = Integer.parseInt(str);
      if (mapsize.width < 1)
	mapsize.width = 1;
    }

    if ((str = getParameter("maph")) != null) {
      mapsize.height = (str != null) ? Integer.parseInt(str) : 0;
      if (mapsize.height < 1)
	mapsize.height = 1;
    }

    // size of map units
    if ((str = getParameter("unitsize")) != null) {
      int i;
      i = Integer.parseInt(str);
      if (i < 1)
	i = 60;
      unitsize.width = unitsize.height = i;
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

    // CLASSES
    if ((str = getParameter("txtclasses")) != null)
      ClassesS = str;

    // PROS
    if ((str = getParameter("txtpros")) != null)
      ProsS = str;

    // MAP
    if ((str = getParameter("txtmap")) != null)
      MapS = str;

    // Font
    if ((str = getParameter("fontname")) != null)
      fontname = str;
    if ((str = getParameter("fontsize")) != null) {
      int size;
      size = Integer.parseInt(str);
      if (size > 0)
	fontsize = size;
    }

    // Pie Mode
    if ((str = getParameter("piemode")) != null) {
      if (str.equalsIgnoreCase("arc"))
	piemode = SliderPie.PIEMODE_ARC;
      else if (str.equalsIgnoreCase("sector"))
	piemode = SliderPie.PIEMODE_SECTOR;
    }

    if ((str = getParameter("topol")) != null) {
      if (str.equalsIgnoreCase("hexa"))
	topol = Sommable.TOPOL_HEXA;
      else if (str.equalsIgnoreCase("rect"))
	topol = Sommable.TOPOL_RECT;
    }

    if ((str = getParameter("neigh")) != null) {
      if (str.equalsIgnoreCase("bubble"))
	neigh = Sommable.NEIGH_BUBBLE;
      else if (str.equalsIgnoreCase("gaussian"))
	neigh = Sommable.NEIGH_GAUSSIAN;
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
    SliderPie pie;
    int i;
    Panel plop;

    //setFont(new Font("Helvetica", Font.PLAIN, 14));

    generators = new SliderPie[num_generators];
    SliderPie gens[][] = new SliderPie[num_generators][1];
    
    cent = 360.0 / (double)num_generators;
    wid = 0.5 * 360.0 / (double)num_generators;

    for (i = 0; i < num_generators; i++) {
      pie = new SliderPie(new Dimension(60, 60), (double)(i * cent), wid);
      generators[i] = pie;
      gens[i][0] = pie;
      pie.set_class(nappicolors[i]);
      pie.setPieMode(piemode);
      pie = null;
    }
    generatormap = new MapDisplay((Vectorable[][])gens);

    nappi = new SliderBar(new Dimension(50, 200), nappicolors);

    map = new MapDisplay((Vectorable[][])mapvectors);
    map.randomize(-1.0, 1.0);
    map.topology(topol);
    map.neighborhood(neigh);

    for (i = 0; i < num_generators; i++)
      generators[i].generate_new_vector(false);

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridbag);

    if (use_mappanel) {
      plop = make_mappanel();
    } else {
      plop = new Panel();
      plop.setLayout(new FlowLayout());
      plop.add(generatormap);
      
      plop.add(nappi);
      
      plop.add(map);
    }

    c.fill = GridBagConstraints.BOTH;
    //c.gridx++;
    c.weightx = 0.5;
    c.weighty = 0.5;
    c.gridwidth = GridBagConstraints.REMAINDER;
    //c.gridheight = GridBagConstraints.REMAINDER; //end column

    c.insets = new Insets(3,3,3,3);
    gridbag.setConstraints(plop, c);
    add(plop);

    Separator sep = new Separator(Separator.HORIZONTAL);
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(sep, c);
    add(sep);

    Panel p = make_alpharad();
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    //c.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(p, c);
    add(p);

    Separator sep2 = new Separator(Separator.HORIZONTAL);
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    gridbag.setConstraints(sep2, c);
    add(sep2);


    Panel cp = make_controlpanel();
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    gridbag.setConstraints(cp, c);
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


  // make map panel
  private Panel make_mappanel() {
    Panel p = new Panel();
    Label l1, l2, l3;
    Button b1, b2;

    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    p.setLayout(gb);
    //p.setFont(font);

    c.insets = new Insets(1,3,1,3);

    c.weightx = 0.5;
    c.weighty = 0.5;
    c.gridwidth = 1;
    
    l1 = new Label(ClassesS);
    gb.setConstraints(l1, c);
    p.add(l1);

    l2 = new Label(ProsS);
    gb.setConstraints(l2, c);
    p.add(l2);

    l3 = new Label(MapS);
    c.gridwidth = GridBagConstraints.REMAINDER;
    gb.setConstraints(l3, c);
    p.add(l3);

    c.gridwidth = 1;

    //c.fill = GridBagConstraints.BOTH;

    gb.setConstraints(generatormap, c);
    p.add(generatormap);

    c.fill = GridBagConstraints.BOTH;
    gb.setConstraints(nappi, c);
    p.add(nappi);

    Separator sep = new Separator(Separator.VERTICAL);
    gb.setConstraints(sep, c);
    p.add(sep);

    c.fill = GridBagConstraints.NONE;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gb.setConstraints(map, c);
    p.add(map);

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

    if (setpriority)
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

    while (Thread.currentThread() == animatorThread) {
      startTime = System.currentTimeMillis();
      
      // Get new teaching vector
      
      rnd = Math.random();
      for (i = 0; i < (num_generators - 1); i++) {
	if (rnd < nappi.pos[i])
	  break;
      }
      
      SliderPie pie = generators[i];
      
      alp = AlphaView.get_alpha();
      rad = RadiusView.get_alpha();

      maplock.lock();
      pie.generate_new_vector(); // pie is the teaching vector
      map.teach((Vectorable)pie, alp, rad);
      maplock.unlock();

      if (AlphaView.atend() && RadiusView.atend()) {
	animatorThread = null;
	scrupdateThread = null;
	frozen = true;
	RunTeach.setState(frozen);
      }

      //Delay depending on how far we are behind.
      startTime -= System.currentTimeMillis();
      while (startTime <= 0)
	startTime += delay;
      
      try {
	Thread.sleep(startTime);
      } catch (InterruptedException e) {
	break;
      }
    }
    
  }

  private void scrupdate_thread() {
    
    //Remember the starting time.
    long startTime;
    int i, winneridx, frameNumber = 0;

    while (Thread.currentThread() == scrupdateThread) {
      startTime = System.currentTimeMillis();
      maplock.lock();
      // classify map and repaint
      for (i = 0; i < num_generators; i++)
	generators[i].generate_new_vector(false);
      map.classify(generatormap);
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
    generators = null;
    generatormap = null;
    nappi = null;
    AlphaView = null;
    removeAll();
    frozen = true;
    if (DEBUG)
      System.out.println(name + ": destroy");
  }

  public void paint(Graphics g) {
    Dimension s = size();
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
      map.randomize(-1.0, 1.0);
      map.classify(generatormap);
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
    }

    return false;
  }

  final static private String[][] parameterinfo = {
    { "ips",      "int",    "teaching iterations per second" },
    { "delay",    "int",    "time between map redisplays in milliseconds" },
    { "unitsize", "int",    "size of unit" },
    { "mapw",     "int",    "map's width" },
    { "maph",     "int",    "map's height" },
    { "alpha",    "float",  "teaching parameter alpha" },
    { "radius",   "float",  "teaching neighborhood radius" },
    { "maxiter",  "int",    "number of teaching iterations" },
    { "topol",    "string", "map topology (RECT or HEXA)" },
    { "neigh",    "string", "neighborhood type (BUBBLE or GAUSSIAN)" },
    { "fontname", "string", "font name" },
    { "fontsize", "int",    "font size" },
    { "piemode",  "string", "mode of sliderpies (arc or sector)" },
    { "txtstart", "string", "text for runstop buttons (start)" },
    { "txtstop",  "string", "text for runstop buttons (stop)" },
    { "txtrand",  "string", "text for randomize button" },
    { "txtreset", "string", "text for reset button" },
    { "txtclasses", "string", "text for classes label" },
    { "txtpros",  "string", "text for class procentages label" },
    { "txtmap",   "string", "text for map label" },
    { "txtalpha", "string", "text for alpha label" },
    { "txtradius", "string", "text for radius label" }};
  
  public String[][] getParameterInfo() {
    return parameterinfo;
  }

  public String getAppletInfo() {
    String info = "SOMDemo2 - More complex Self-Organizing Map Demo\n";
    info = info + " Author: Jussi Hynninen (http://www.iki.fi/~hynde/)\n";
    info = info + " version: " + rcsid;
    return info;
  }

}

class Separator extends Canvas {
  
  static final int HORIZONTAL = 1;
  static final int VERTICAL = 2;
  static final int UP = 3;  
  static final int DOWN = 4;

  private int mode = HORIZONTAL;
  private int upmode = DOWN;

  public Separator(int mode) {
    this.mode = mode;
  }

  public Separator(int mode, int upmode) {
    this.mode = mode;
    this.upmode = upmode;
  }

  public void paint(Graphics g) {
    Dimension s = size();
    Color c1, c2;
    if (upmode == UP) {
      c1 = Color.white;
      c2 = Color.black;
    } else {
      c1 = Color.black;
      c2 = Color.white;
    }

    if (mode == HORIZONTAL) {
      int h = s.height/2;
      g.setColor(c1);
      g.drawLine(0, h, s.width - 1, h);
      g.setColor(c2);
      g.drawLine(0, h + 1, s.width - 1, h + 1);
  } else {
      int w = s.width/2;
      g.setColor(c1);
      g.drawLine(w, 0, w, s.height - 1);
      g.setColor(c2);
      g.drawLine(w + 1, 0, w + 1, s.height - 1);
    }
  }

}

