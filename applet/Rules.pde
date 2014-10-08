/**
Class that implements the rules of the game
**/
public class Rules implements Drawable {
  
  private final int _GOAL_COLOR = #00FF00;
  private final int _GOAL_ALPHA_HIGH = 250;
  private final int _GOAL_ALPHA_LOW = 20;
    
  private World _world;
  private CoordinateInt _goal;
  private int _alpha;
  private int _alphaStep;
  
  /**
  Constructor
  **/
  public Rules(World world) {
    _world = world;
    _alpha = _GOAL_ALPHA_HIGH;
    _alphaStep = -1;
    generateGoal();
  }
  
  /**
  Place the goal
  **/
  private void generateGoal() {
    // goal is a random location on the last column
    int x = _world.getGridSize().getX() - 1;
    int y = (int) random(_world.getGridSize().getY());
    _goal = new CoordinateInt(x, y);
  }
  
  /**
  Place the Hero
  **/
  public CoordinateInt generateHeroStart() {
     // hero start is a random location on the first column
    int y = (int) random(_world.getGridSize().getY());
    return new CoordinateInt(0, y);
  }
  
  /**
  Place a hunter
  **/
  public CoordinateInt generateHunterStart(Hero hero) {
    // hunters start int the center top or center bottom, opposite to where the hero is
    CoordinateInt start;
    // loop to make sure the hunter's position is walkable
    do { 
      int x = (int) (_world.getGridSize().getX() / 2) + (int) random(_world.getGridSize().getX() / 4 + 1) - (int) (_world.getGridSize().getX() / 8);
      int y = (int) random(_world.getGridSize().getY() / 8 + 1);
      // see if the hero is in the top half and put hunters in botton half in that case
      if (_world.positionToGrid(hero.getPosition()).getY() < _world.getGridSize().getY() / 2) {
        y = _world.getGridSize().getY() - 1 - y;
      }
      start = new CoordinateInt(x, y);
    } while (!_world.isWalkable(start));
        
    return start;
  }
  
  /**
  Place an obstacle
  **/
  public CoordinateInt generateObstaclePosition() {
     // obstacles are placed on random positions except the first and last column
     CoordinateInt position;
     // loop to prevent assigning two obstacles to the same grid position
     do {
       int x = (int) random(_world.getGridSize().getX() - 2) + 1;
       int y = (int) random(_world.getGridSize().getY());
       position = new CoordinateInt(x, y); 
     } while (!_world.isWalkable(position));
     
     return position;
  }
  
  /**
  Draw the goal. It is a flashing green square
  **/
  public void draw(View view) {
    // alpha varies between two boundaries. it goes back and forth
    if (_alpha >= _GOAL_ALPHA_HIGH) {
      _alphaStep = -4;
    }
    else if (_alpha <= _GOAL_ALPHA_LOW) {
      _alphaStep = 4;
    }
    _alpha += _alphaStep;
    
    // draw the goal tile
    imageMode(CORNER);
    CoordinateInt position = view.convert(_goal);
    noStroke();
    fill(_GOAL_COLOR, _alpha);
    rect(position.getX(), position.getY(), view.convert(_world.getTileSize()).getX(), view.convert(_world.getTileSize()).getY());
  }
  
  /**
  Check winning condition, i.e. the hero has reached the goal.
  Position is checked against the grid.
  **/
  public boolean checkWon() {
    return _world.positionToGrid(_world.getHero().getPosition()).equals(_goal);
  }
  
  /**
  Check losing condition, i.e. a hunter has caught the hero.
  Position is checked against the grid.
  **/
  public boolean checkLost() {
    CoordinateInt heroPosition = _world.positionToGrid(_world.getHero().getPosition());
    for (Hunter hunter : _world.getHunters()) {
      if (_world.positionToGrid(hunter.getPosition()).equals(heroPosition)) {
        return true;
      } 
    }
    return false;
  }
  
  /**
  Goal getter
  **/
  public CoordinateInt getGoal() {
    return _goal;
  }
}
