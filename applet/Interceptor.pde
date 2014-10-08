/**
Interceptor type Hunter. It goes to a point halfway between the hero and the goal.
**/
public class Interceptor extends Hunter {
  
  /**
  Constructor
  **/
  public Interceptor(CoordinateInt position, Double speed, PImage image, World world) {
    super(position, speed, image, world);
  }
  
  /**
  Calculate the halfway point between hero and goal.
  **/
  protected void calculateDestination() {
    // try to intercept hero halfway between his current position and the goal
    // calculate half way point
    CoordinateInt hero = this.getWorld().positionToGrid(this.getWorld().getHero().getPosition());
    CoordinateInt goal = this.getWorld().getRules().getGoal(); 
    int x = (hero.getX() + goal.getX()) / 2;
    int y = (hero.getY() + goal.getY()) / 2;
    CoordinateInt destination = new CoordinateInt(x, y);
   
    // make sure the destination is walkable. if not, choose a destination closer to the hero.
    // the algorithm ends, because the last destination picked is the position of the hero, which is always walkable
    while (!this.getWorld().isWalkable(destination)) {
      // take a look at which dimension is further away from hero and decrease that one 
      int gradientX = destination.getX() - hero.getX();
      int gradientY = destination.getY() - hero.getY();
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
