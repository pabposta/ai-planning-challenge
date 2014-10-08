/**
Interface for objects that can be drawn
**/
interface Drawable {
  
  /**
  Method in which objects implement how they are drawn. They take a view to convert from world coordinates to screen coordinates
  **/
  public void draw(View view);
}
