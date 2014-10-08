/**
Class that converts from world coordinates to screen coordinates
**/
public class View {
  
  private World _world;
  private double _xFactor;
  private double _yFactor;
  
  /**
  Constructor
  **/
  public View(World world) {
    _world = world;
    _xFactor = width / _world.getSize().getX();
    _yFactor = height / _world.getSize().getY();
  }
  
  /**
  Convert a world coordinate to screen coordinate
  **/
  public CoordinateInt convert(CoordinateDouble coordinate) {
    return new CoordinateInt((int) (coordinate.getX() * _xFactor), (int) (coordinate.getY() * _yFactor));
  }
 
  /**
  Convert a grid coordinate to screen coordinate.
  **/  
  public CoordinateInt convert(CoordinateInt gridCoordinate) {
    return convert(_world.gridToPosition(gridCoordinate));
  }
  
  /**
  Convert a screen coordinate to a grid coordinate.
  **/
  public CoordinateInt gridPosition(CoordinateInt cursorPosition) {
    return _world.positionToGrid(new CoordinateDouble(cursorPosition.getX() / _xFactor, cursorPosition.getY() / _yFactor));
  }
}
