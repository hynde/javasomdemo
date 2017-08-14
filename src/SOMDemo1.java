//
// Applet SOMDemo1 - Demonstration of the Self-Organizing Map algorithm
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   ** May 1996

import java.applet.Applet;
import java.awt.*;

public class SOMDemo1 extends Applet implements Runnable {

  static final String rcsid = "$Id: SOMDemo1.java,v 1.16 1996/07/12 10:07:44 hynde Exp $";

  // Debugging stuff
  final static boolean DEBUG = false;
  //static int foo_counter = 1;
  String name = "SOMDemo1";

  final private boolean setpriority = false;

  Vector1DDisplay inputvector = null;    // The input vector
  Vector1DDisplay mapvectors[][] = null; // Map vectors
  
  Dimension mapsize = new Dimension(5, 1);     // Size of map
  Dimension unitsize = new Dimension(30, 200); // Size of map units

  MapDisplay map = null;         // Map component
  Thread animatorThread = null;  // Teaching thread
  
  boolean frozen = true;
  int delay, fps = 5;

  double maxalpha = 0.5;
  double alpha = 0.1;
  double radius = 1.1;

  // Font
  private int fontstyle   = Font.PLAIN;
  private String fontname = "Helvetica";
  private int fontsize    = 12;
  private Font font       = null;

  // Control buttons and their texts
  ToggleButton RunstopButton;
  Button RandButton;
  static String RunBS  = " Start ";
  static String StopBS = " Stop  ";
  static String RandBS = " Randomize ";

  TextField AlphaTF;  // textfield for alpha entry

  int bordersize = -1;  // size of unit borders, -1 means use default

  public void init() {
    int x, y;

    System.out.println(getAppletInfo());

    frozen = true; // frozen initially

    get_parameters();

    font = new Font(fontname, fontstyle, fontsize);
    setFont(font);

    // create map units
    mapvectors = new Vector1DDisplay[mapsize.height][mapsize.width];
    for (y = 0; y < mapsize.height; y++)
      for (x = 0; x < mapsize.width; x++) {
	mapvectors[y][x] = new Vector1DDisplay(unitsize, true);
	mapvectors[y][x].editable(true);
	if (bordersize >= 0)
	  mapvectors[y][x].bordersize = bordersize;
      }

    // create input vector
    inputvector = new Vector1DDisplay(unitsize, false);
    inputvector.editable(true);
    if (bordersize >= 0)
      inputvector.bordersize = bordersize;
    inputvector.setBackground(Color.pink);

    // layout components
    do_layout();

    if (DEBUG)
      System.out.println(name + ": init done");

  }

  private String name() {
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

    // number of vectors
    if ((str = getParameter("units")) != null) {
      mapsize.width = Integer.parseInt(str);
      if (mapsize.width <= 0)
	mapsize.width = 5;
    }
    
    if ((str = getParameter("unitw")) != null) {
      unitsize.width = Integer.parseInt(str);
      if (unitsize.width < 10)
	unitsize.width = 10;
    }

    if ((str = getParameter("unith")) != null) {
      unitsize.height = Integer.parseInt(str);
      if (unitsize.height < 10)
	unitsize.height = 10;
    }
    
    // maxalpha
    if ((str = getParameter("maxalpha")) != null) 
      try {
	maxalpha = Double.valueOf(str).doubleValue();
      } catch (NumberFormatException e) {
      };
    
    // alpha
    if ((str = getParameter("alpha")) != null) 
      try {
	double a;
	a = Double.valueOf(str).doubleValue();
	if (a <= maxalpha)
	  alpha = a;
      } catch (NumberFormatException e) {
      };

    // radius
    if ((str = getParameter("radius")) != null)
      try {
	radius = Double.valueOf(str).doubleValue();
      } catch (NumberFormatException e) {
      };

    // button texts 

    // RUN
    if ((str = getParameter("txtstart")) != null)
      RunBS = str;
    // STOP
    if ((str = getParameter("txtstop")) != null)
      StopBS = str;
    // RANDOMIZE
    if ((str = getParameter("txtrand")) != null)
      RandBS = str;

    // Font
    if ((str = getParameter("fontname")) != null)
      fontname = str;
    if ((str = getParameter("fontsize")) != null) {
      int size;
      size = Integer.parseInt(str);
      if (size > 0)
	fontsize = size;
    }

    // bordersize for units
    if ((str = getParameter("bordersize")) != null) {
      bordersize = Integer.parseInt(str);
      if (bordersize < 0)
	bordersize = -1;
    }

  }
  
  // set frames per second and corresponding delay
  private void set_fps(int f) {
    if (f <= 0)
      fps = 1;
    else 
      fps = f;
    delay = (fps > 0) ? (1000 / fps) : 100;
  }
    

  // do the applet's layout
  private void do_layout() {
    Label l;

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridbag);

    Panel buttonpanel = new Panel();
    buttonpanel.setLayout(new FlowLayout());
    //buttonpanel.setFont(font);

    RunstopButton = new ToggleButton(StopBS, RunBS, frozen);
    buttonpanel.add(RunstopButton);
    RandButton = new Button(RandBS);
    buttonpanel.add(RandButton);

    l = new Label("Alpha:");
    buttonpanel.add(l);
    
    AlphaTF = new TextField(Double.toString(alpha), 4);
    AlphaTF.setEditable(true);
    buttonpanel.add(AlphaTF);

    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0.2;
    c.weighty = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER; //end row
    c.anchor = GridBagConstraints.CENTER;
    gridbag.setConstraints(buttonpanel, c);
    add(buttonpanel);

    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 0.5;
    c.weighty = 0.5;
    Separator sep = new Separator(Separator.HORIZONTAL);

    gridbag.setConstraints(sep, c);
    add(sep);

    // input vector
    inputvector.randomize(0.0, 1.0);

    c.fill = GridBagConstraints.NONE;
    c.gridwidth = 1; //GridBagConstraints.RELATIVE;
    //c.weightx = 0.0;
    c.weightx = 0.5;
    c.weighty = 0.8;
    c.anchor = GridBagConstraints.CENTER;

    gridbag.setConstraints(inputvector, c);
    add(inputvector);

    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 0.1;
    c.weighty = 0.5;
    Separator sep2 = new Separator(Separator.VERTICAL);

    gridbag.setConstraints(sep2, c);
    add(sep2);

    map = new MapDisplay((Vectorable[][])mapvectors);
    map.randomize(0.0, 1.0);

    c.gridwidth = GridBagConstraints.REMAINDER; //end row
    //c.weightx = 0.0;
    c.weightx = 0.5;
    c.weighty = 0.8;
    c.gridx++;
    c.anchor = GridBagConstraints.CENTER;
    c.fill = GridBagConstraints.NONE;

    add(map);

    //validate();
  }

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

  public void destroy() {
    if (DEBUG)
      System.out.println(name + ": destroy");
    stop_thread();
    inputvector = null;
    mapvectors = null;
    map = null;
    removeAll(); // remove all components from container
    if (DEBUG)
      System.out.println(name + ": destroy done");
  }
  
  // start thread
  private void start_thread() {
    if (animatorThread == null) {
      animatorThread = new Thread(this, name + " teacher");
      //animatorThread.setPriority(Thread.MIN_PRIORITY);
      animatorThread.start();
    }
  }

  // stop thread
  private void stop_thread() {
    if (animatorThread != null) {
      animatorThread.stop();
      animatorThread = null;
    }
  }

  // animation thread
  public void run() {
    //Remember the starting time.
    long startTime;
    
    if (setpriority)
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    while (Thread.currentThread() == animatorThread) {
      startTime = System.currentTimeMillis();
      
      // Get new teaching vector
      
      inputvector.randomize(0.0, 1.0);
      inputvector.repaint();

      do_iteration();

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
    // end of thread
  }

  // do one teaching iteration
  private void do_iteration() {
    int i;
    // reset history of adaptations, otherwise old changes are left for
    // those vectors that were not adapted during this iteration
    for (i = 0; i < mapsize.width; i++)
      mapvectors[0][i].reset_history();
    
    map.teach((Vectorable)inputvector, alpha, radius);
    map.repaint();
  }

  // set run/stop button text
  private void set_runstop() {
    RunstopButton.setState(frozen);
    }
  
  public boolean keyDown(Event evt, int key) {
    // pressing space does one teaching iteration with a random vector
    if (key == ' ') {
      // first stop thread if running
      if (!frozen) {
	frozen = true;
	stop_thread();
	set_runstop();
      }
      
      // generate new vector and teach
      inputvector.randomize(0.0, 1.0);
      inputvector.repaint();
      do_iteration();
      return true;
    } else if (key == Event.UP) {
      // cursor up increases framerate
      set_fps(fps + 1);
      return true;
    }
    else if (key == Event.DOWN) {
      // cursor down decreases framerate
      set_fps(fps - 1);
      return true;
    }
    
    return false;
  }
  
  public boolean action(Event e, Object arg) {
    if (e.target == inputvector) {
      // user clicked on inputvector -> use it to teach map
      do_iteration();
      return true;
    } else if (e.target == RunstopButton) {
      frozen = !frozen;
      if (frozen) 
	stop_thread();
      else
	start_thread();
      set_runstop();
      return true;
    } else if (e.target == RandButton) {
      if (!frozen) {
	frozen = true;
	stop_thread();
	set_runstop();
      }
      map.randomize(0.0, 1.0);
      map.repaint();
      return true;
    } else if (e.target == AlphaTF) {
      double val;
      try {
	val = Double.valueOf(AlphaTF.getText()).doubleValue();
	if (DEBUG)
	  System.out.println("alpha set to " + val);
	if (val <= maxalpha)
	  alpha = val;
	else 
	  AlphaTF.setText(Double.toString(alpha));
      } catch (NumberFormatException ex) {
	if (DEBUG)
	  System.out.println("numberformatexception");
      }
      return true;
    }
    return false;
  }
  
  final static private String[][] parameterinfo = {
    { "ips",       "int",    "teaching iterations per second" },
    { "units",     "int",    "number of map units" },
    { "unitw",     "int",    "map unit's width" },
    { "unith",     "int",    "map unit's height" },
    { "alpha",     "float",  "teaching parameter alpha" },
    { "maxalpha",  "float",  "maximum value of user-settable alpha" },
    { "radius",    "float",  "teaching neighborhood radius" },
    { "fontname",  "string", "font name" },
    { "fontsize",  "int",    "font size" },
    { "txtstart",  "string", "text for runstop button (start)" },
    { "txtstop",   "string", "text for runstop button (stop)" },
    { "txtrand",   "string", "text for randomize button" },
    { "bordersize","int",    "border size" }};
  
  public String[][] getParameterInfo() {
    return parameterinfo;
  }

  public String getAppletInfo() {
    String info = "SOMDemo1 - Self-Organizing Map Algorithm Demo\n";
    info = info + " Author: Jussi Hynninen (http://www.iki.fi/~hynde/)\n";
    info = info + " version: " + rcsid;
    return info;
  }
}

