/**
The world is divided into several tiles, which make up the grid.
**/
public class Tile implements Drawable {
  
  private PImage _image;
  private CoordinateDouble _size;
  private CoordinateDouble _position;
  
  /**
  Constructor
  **/  
  public Tile(PImage image, CoordinateDouble size, CoordinateDouble position) {
    _image = image;
    _size = size;
    _position = position;
  }
  
  /**
  Draw the tile image
  **/
  public void draw(View view) {
    CoordinateInt position = view.convert(_position);
    CoordinateInt size = view.convert(_size);
    imageMode(CORNER);
    image(_image, position.getX(), position.getY(), size.getX(), size.getY());
  }
}
