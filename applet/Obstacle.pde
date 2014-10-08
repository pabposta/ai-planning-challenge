/**
An obstacle to mark squares that cannot be visited.
**/
public class Obstacle implements Drawable {
  
  private CoordinateInt _position;
  private PImage[] _sprites;
  private double _currentSprite;
  private double _spriteStep; 
  private World _world;
  
  /**
  Constructor
  **/
  public Obstacle(CoordinateInt position, PImage[] sprites, World world) {
    _position = position;
    _sprites = sprites;
    _world = world;
    _currentSprite = 0;
    _spriteStep = 0.16;
  }
  
  /**
  Position setter
  **/
  public void setPosition(CoordinateInt position) {
    _position = position;
  }
  
  /**
  Draw the obstacle sprite. The obstacle has a short animation. 
  **/
  public void draw(View view) {
    imageMode(CENTER);
    CoordinateInt position = view.convert(_world.gridCenterToPosition(_position)); 
    // display the current sprite
    image(_sprites[(int) _currentSprite], position.getX(), position.getY());
    // next step in animation
    _currentSprite += _spriteStep;
    // the animation loops indefinitely
    if (_currentSprite >= _sprites.length) {
      _currentSprite = 0;
    }
  }
  
}
