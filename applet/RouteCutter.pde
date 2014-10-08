/**
This Hunter tries to cut off escape routes by filling holes the other two hunters leave.
**/
public class RouteCutter extends Hunter {
  
  /**
  Constructor
  **/
  public RouteCutter(CoordinateInt position, Double speed, PImage image, World world) {
    super(position, speed, image, world);
  }
  
  /**
  The two other hunters form a square. They are on two of the corners and the other two are free.
  Go to the corner closer (Manhattan distance) to the goal.
  **/
  protected void calculateDestination() {
        
    // get the first hunter from the list
    ArrayList<Hunter> hunters = this.getWorld().getHunters();
    Iterator<Hunter> it = hunters.iterator();
    Hunter one = it.next();
    // check that we are not popping outselves
    if (one == this) {
      one = it.next();
    }
    
    // get the second hunter from the list
    Hunter two = it.next();
    // check that we are not popping outselves
    if (two == this) {
      two = it.next();
    }
    
    // get the relevant coordinates
    int x1 = this.getWorld().positionToGrid(one.getPosition()).getX();
    int y1 = this.getWorld().positionToGrid(one.getPosition()).getY();
    int x2 = this.getWorld().positionToGrid(two.getPosition()).getX();
    int y2 = this.getWorld().positionToGrid(two.getPosition()).getY();
    int gx = this.getWorld().getRules().getGoal().getX();
    int gy = this.getWorld().getRules().getGoal().getY();
    
    // calculate manhattan distance to goal of both points
    int distance1 = abs(gx - x1) + abs(gy - y2);
    int distance2 = abs(gx - x2) + abs(gy - y1);
    
    // choose the closer one
    CoordinateInt destination;
    if (distance1 < distance2) {
      destination = new CoordinateInt(x1, y2);
    }
    else {
      destination = new CoordinateInt(x2, y1);
    }
      
    // make sure the destination is walkable. if not, slowly approach the hero. this ensures the algorithm ends.
    while(!this.getWorld().isWalkable(destination)) {
      // take a look at which dimension is further away from hero and decrease that one 
      int gradientX = destination.getX() - this.getWorld().positionToGrid(this.getWorld().getHero().getPosition()).getX();
      int gradientY = destination.getY() - this.getWorld().positionToGrid(this.getWorld().getHero().getPosition()).getY();
      // change x coordinate
      if (abs(gradientX) > abs(gradientY)) {
        int unit = gradientX > 0 ? 1 : -1;
        destination.set(destination.getX() - unit, destination.getY());
      }
      // change y coordinate
      else {
        int unit = gradientY > 0 ? 1 : -1;
        destination.set(destination.getX(), destination.getY() - unit);
      }
    }
      
    this.setDestination(destination);
  }
  
}
