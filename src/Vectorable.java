// Interface Vectorable - interface for vector operations
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   ** May 1996

interface Vectorable {
  void adapt(Vectorable other, double alpha); // adapt vector
  double distance(Vectorable other);          // distance to other
  int dimension();                            // get dimension
  void dimension(int dim);                    // set dimension
  double[] get_points();                      // get points
  void set_points(double points[]);           // set vector
  void set_class(Object c);
  Object get_class();
  void randomize(double minimum, double maximum); // randomize vector
  void set_mark(boolean mark);                // set mark (used to indicate winner)
  boolean get_mark();                         // get mark
}
