/**
Animations are Drawables that can expire
**/
public interface Animation extends Drawable {
  
  /**
  Tell if the animation has expired and should be deleted
  **/
  public boolean hasExpired();
}
