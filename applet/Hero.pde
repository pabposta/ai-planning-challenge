/**
The hero who must flee from the hunters
**/
public class Hero extends Agent {
  
  /**
  Constructor
  **/  
  public Hero(CoordinateInt position, Double speed, PImage image, World world) {
    super(position, speed, image, world);
  }
  
  /**
  A hero only moves, since his destination is defined by the player through mouse clicks
  **/
  public void doSomething() {
    move();
  }
  
}
