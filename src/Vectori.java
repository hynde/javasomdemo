// Class Vectori - a basic vector
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   June 1996

class Vectori implements Vectorable {

  private int dimension;
  private double points[];
  private Object vec_class = null;
  private boolean mark = false;

  public Vectori(int dim) {
    int i;
    dimension = dim;
    points = new double[dimension];
    for (i = 0; i < dimension; i++)
      points[i] = 0.0;
  }

  // Vectorable functions:

  public void set_class(Object c) {
    vec_class = c;
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
