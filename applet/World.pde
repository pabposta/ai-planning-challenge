import pathfinder.*;

/**
Class that holds the information about our gaming world. It also acts as a mediator between the objects in it.
**/
public class World implements Drawable {
  private CoordinateDouble _size;
  private CoordinateInt _gridSize;
  private CoordinateDouble _tileSize;
  private Tile[][] _tiles;
  private boolean[][] _walkable;
  private Hero _hero;
  private ArrayList<Hunter> _hunters;
  private Graph _graph;
  private Rules _rules;
  
  /**
  Constructor
  **/
  public World(double worldWidth, double worldHeight, int gridWidth, int gridHeight, PImage backgroundImage) {
    _size = new CoordinateDouble(worldWidth, worldHeight);
    _gridSize = new CoordinateInt(gridWidth, gridHeight);
        
    // divide the world into tiles (grid)
    _tiles = new Tile[gridWidth][gridHeight];
    _tileSize = new CoordinateDouble(worldWidth / (double) gridWidth, worldHeight / (double) gridHeight);
    double tileImageWidth = backgroundImage.width / gridWidth;
    double tileImageHeight = backgroundImage.height / gridHeight;
    // assign each tile the corresponding part of the background image and its position in the world
    for (int x = 0; x < gridWidth; x++) {
      for (int y = 0; y < gridHeight; y++) {
        PImage image = backgroundImage.get((int) (x * tileImageWidth), (int) (y * tileImageHeight), (int) tileImageWidth, (int) tileImageHeight);
        CoordinateDouble position = new CoordinateDouble(x * _tileSize.getX(), y * _tileSize.getY());
        _tiles[x][y] = new Tile(image, _tileSize, position);
      }
    }
    
    // create empty walkable matrix. the values will be set when the objects are placed 
    _walkable = new boolean[gridWidth][gridHeight];
  }
  
  /**
  Place the obstacles according to the Rules and build the walkable matrix.
  Call the method to build the graph representation of the world.
  **/  
  public void positionObstacles(ArrayList<Obstacle> obstacles) {
    
    // reset walkable matrix
    for (int x = 0; x < _gridSize.getX(); x++) {
      for (int y = 0; y < _gridSize.getY(); y++) {
        _walkable[x][y] = true;
      }
    }
    
    for (Obstacle obstacle : obstacles) {
      // get a position from the rules
      CoordinateInt position = _rules.generateObstaclePosition();
            
      // assign position to obstacle and walkable matrix
      obstacle.setPosition(position);
      _walkable[position.getX()][position.getY()] = false;
    }
    
    // build graph for path finding library
    buildGraph();
  }
  
  /**
  Build representation of the world as a graph for the path finding library.
  **/
  protected void buildGraph() {
    _graph = new Graph(_gridSize.getX() * _gridSize.getY());
    
    // create nodes. one node for each tile. they are not connected yet.
    int c = 0; // node id counter
    CoordinateInt gridPosition = new CoordinateInt(0, 0);
    for (int y = 0; y < _gridSize.getY(); y++) {
      for (int x = 0; x < _gridSize.getX(); x++) {
        _graph.addNode(new GraphNode(c));
        c++;
      }
    }
    
    // create the edges. step through the nodes and connect them to the neighbors
    c = 0;
    double sqrtTwo = sqrt(2);
    for (int y = 0; y < _gridSize.getY(); y++) {
      for (int x = 0; x < _gridSize.getX(); x++) {
        // make sure we are on a walkable edge
        gridPosition.set(x, y);
        if (!this.isWalkable(gridPosition)) {
          c++;
          continue;
        }
        // we need to connect to the 3 nodes on the right and the one below. this way we get all of them without repeats
        // right
        if (x < _gridSize.getX() - 1) {
          // upper right
          if (y > 0) {
            // make sure the hunter won't get stuck, so diagonal edges may not have adjacent obstacles
            // upper and right positions need to be walkable
            gridPosition.set(x, y - 1);
            if (this.isWalkable(gridPosition)) {
              gridPosition.set(x + 1, y);
              if (this.isWalkable(gridPosition)) {
                // of course, the destination square, i.e. the upper right one needs to be walkbable as well
                gridPosition.set(x + 1, y - 1);
                if (this.isWalkable(gridPosition)) {
                  // it is, so create the edge
                  _graph.addEdge(c, c - _gridSize.getX() + 1, sqrtTwo, sqrtTwo);
                }
              }
            }
          }
          // plain right
          gridPosition.set(x + 1, y);
          if (this.isWalkable(gridPosition)) {
            _graph.addEdge(c, c + 1, 1, 1);
          }
          // lower right
          if (y < _gridSize.getY() - 1) {
            // make sure the hunter won't get stuck, so diagonal edges may not have adjacent obstacles
            // lower and right positions need to be walkable
            gridPosition.set(x, y + 1);
            if (this.isWalkable(gridPosition)) {
              gridPosition.set(x + 1, y);
              if (this.isWalkable(gridPosition)) {
                // of course, the destination square, i.e. the lower right one needs to be walkbable as well
                gridPosition.set(x + 1, y + 1);
                if (this.isWalkable(gridPosition)) {
                  _graph.addEdge(c, c + _gridSize.getX() + 1, sqrtTwo, sqrtTwo);
                }
              }
            }
          }
        }
        // below
        if (y < _gridSize.getY() - 1) {
          gridPosition.set(x, y + 1);
          if (this.isWalkable(gridPosition)) {
            _graph.addEdge(c, c + _gridSize.getX(), 1, 1);
          }
        }
        c++; // increase node id to go to next node    
      }
    }
  }
  
  /**
  Tell the world about the hero.
  **/
  public void registerHero(Hero hero) {
    _hero = hero;
  }
  
  /**
  Tell the world about a hunter.
  **/
  public void registerHunters(ArrayList<Hunter> hunters) {
    _hunters = hunters;
  }
  
  /**
  Tell the world about the rules.
  **/
  public void registerRules(Rules rules) {
    _rules = rules;
  }
  
  /**
  Transform a world position to a grid position.
  **/
  public CoordinateInt positionToGrid(CoordinateDouble position) {
    int gridPositionX = (int) (position.getX() / _tileSize.getX());
    int gridPositionY = (int) (position.getY() / _tileSize.getY());
    return new CoordinateInt(gridPositionX, gridPositionY); 
  }
  
  /**
  Transform a grid position to a world position. The world position is the upper left corner of the tile.
  **/
  public CoordinateDouble gridToPosition(CoordinateInt gridPosition) {
    double positionX = gridPosition.getX() * _tileSize.getX();
    double positionY = gridPosition.getY() * _tileSize.getY();
    return new CoordinateDouble(positionX, positionY); 
  }
  
  /**
  Transform a grid position to a world position. The world position is the center of the tile.
  **/
  public CoordinateDouble gridCenterToPosition(CoordinateInt gridPosition) {
    double positionX = (gridPosition.getX() + 0.5) * _tileSize.getX();
    double positionY = (gridPosition.getY() + 0.5) * _tileSize.getY();
    return new CoordinateDouble(positionX, positionY); 
  }
  
  /**
  Query if a tile can be visited.
  **/
  public boolean isWalkable(CoordinateInt position) {
    return _walkable[position.getX()][position.getY()];
  }
  
  /**
  Query if a world position can be visited.
  **/
  public boolean isWalkable(CoordinateDouble position) {
    return isWalkable(this.positionToGrid(position));
  }
  
  /**
  Draw the world, i.e. the tiles.
  **/
  public void draw(View view) {
    for (int x = 0; x < _tiles.length; x++) {
      for (int y = 0; y < _tiles[x].length; y++) {
        _tiles[x][y].draw(view);
      }
    }
  }
  
  /**
  World size getter
  **/ 
  public CoordinateDouble getSize() {
    return _size;
  }
  
  /**
  Grid size getter
  **/
  public CoordinateInt getGridSize() {
    return _gridSize;
  }
  
  /**
  Tile size getter
  **/
  public CoordinateDouble getTileSize() {
    return _tileSize;
  }
  
  /**
  Hero getter
  **/
  public Hero getHero() {
    return _hero;
  }
  
  /**
  Hunters getter
  **/
  public ArrayList<Hunter> getHunters() {
    return _hunters;
  }
  
  /**
  Graphh getter
  **/
  public Graph getGraph() {
    return _graph;
  }
  
  /**
  Rules getter
  **/ 
  public Rules getRules() {
    return _rules;
  }
  
}
