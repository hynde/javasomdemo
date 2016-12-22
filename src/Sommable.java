// Interface Sommable - interface for SOM maps
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   ** May 1996
//
// 	$Id: Sommable.java,v 1.2 1996/05/14 15:43:30 hynde Exp $	
//
import java.awt.Dimension;

public interface Sommable {
 
  final int TOPOL_RECT = 1;
  final int TOPOL_HEXA = 2;
  final int NEIGH_BUBBLE = 1;
  final int NEIGH_GAUSSIAN = 2;

  void topology(int topol); // set topology
  int  topology(); // get topology
  void neighborhood(int neigh); // set neighborhood
  int neighborhood(); // get neighborhood
  Dimension mapsize();  // get mapsize
  int find_winner(Vectorable vec); // find winner
  void randomize(double minimum, double maximum); // randomize map
  void teach(Vectorable vec, double alpha, double radius);
  Vectorable get_vector(int i);   //get vector
  void classify(Sommable other);  // classify vector
}
