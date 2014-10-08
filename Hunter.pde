import pathfinder.*;
import java.util.LinkedList;

/**
Parent class for all the hunters
**/
public abstract class Hunter extends Agent {
  
  private GraphSearch_Astar _aStar;
  
  /**
  Constructor. Initializes A*
  **/
  public Hunter(CoordinateInt position, Double speed, PImage image, World world) {
    super(position, speed, image, world);
    _aStar = new GraphSearch_Astar(world.getGraph());
  }
  
  /**
  Main loop for Hunters. First, calculate the destination, which is different depending on the strategy the Hunter uses.
  Then apply A* to find a path to the destination. Next, go towards the closest node on the path.
  **/
  public void doSomething() {
    // have the child calculate the destination
    calculateDestination();
    
    // execute A* to get the next node on the path to the destination
    CoordinateInt gridPosition = this.getWorld().positionToGrid(this.getPosition()); 
    int from = gridPosition.getY() * this.getWorld().getGridSize().getX() + gridPosition.getX();
    int to = this.getDestination().getY() * this.getWorld().getGridSize().getX() + this.getDestination().getX();
    // don't do anything if we are already at the destination. the A* library does not seem to like this
    if (from == to) {
      return;
    }

    // the search can throw a null pointer exception. we have no path in that case, so the hunter can't move. we simply exit the method.
    LinkedList<GraphNode> nodes;
    try {
      nodes = _aStar.search(from, to);
    }
    catch (NullPointerException e) {
      return;
    }
    
    // try to reach the second element in the list (if it exists). the first one is our current position
    if (nodes.pollFirst() != null) {
      GraphNode next = nodes.pollFirst();
      if (next != null) {
        // calculate x and y from the node id
        int x = next.id() % this.getWorld().getGridSize().getX();
        int y = next.id() / this.getWorld().getGridSize().getX();
        // set the destination as the next node
        this.setDestination(new CoordinateInt(x, y));
        // move the hunter
        move();
      }
    }
  }
  
  /**
  Method to find the destination of a Hunter. It will depend on the Hunter's strategy.
  **/
  protected abstract void calculateDestination();
  
}
