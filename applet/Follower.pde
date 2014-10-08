/**
The Follower type Hunter
**/
public class Follower extends Hunter {
  
  /**
  Constructor
  **/
  public Follower(CoordinateInt position, Double speed, PImage image, World world) {
    super(position, speed, image, world);
  }
  
  /**
  Go to where the hero is
  **/
  protected void calculateDestination() {
    this.setDestination(this.getWorld().positionToGrid(this.getWorld().getHero().getPosition()));
  }
}
