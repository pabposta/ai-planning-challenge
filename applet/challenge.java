import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Iterator; 
import pathfinder.*; 
import java.util.LinkedList; 
import pathfinder.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class challenge extends PApplet {

/*
Controls: Click where you want to go. p or P pauses the game.

This is a sketch for the AI Planning course on Coursera. The goal is to reach the green square, while avoiding the hunting airplanes. There are some obstacles, indicating squares that cannot be visited.
The hunting planes have different strategies. The first one follows you around. It goes where you go. The second one is an interceptor that always tries to go between you and the goal. The third one tries to cut off posible escape routes. Its position depends on that of the other hunters. Together they form a coordinated team.
All hunters use A* for path finding. Path finding is performed on the squares (the grid), and is therefore quick enough to allow recalculation of the path in real time. Cost between horizontally or vertically adjacent squares is 1 and sqrt(2) for diagonally adjacent squares. The heuristic uses the euclidean distance. The implementation of A* is that of Peter Lager's Path finding library (http://www.lagers.org.uk/pfind/index.html).
All images used are public. List of sources of the images not created by me:
http://www.publicdomainpictures.net/view-image.php?image=66948&picture=chinese-illustration
http://www.widgetworx.com/spritelib/
*/



// constants
final int NUM_OBSTACLES = 40;
final int OBSTACLE_SPRITES = 4;
final double HERO_SPEED = 2.0f;
final double FOLLOWER_SPEED = 1.2f;
final double INTERCEPTOR_SPEED = 1.1f;
final double ROUTE_CUTTER_SPEED = 1.0f;
final int SCREEN_SIZE_X = 640;
final int SCREEN_SIZE_Y = 480;
final int WORLD_SIZE_X = 640;
final int WORLD_SIZE_Y = 480;
final int GRID_SIZE_X = 32;
final int GRID_SIZE_Y = 24;
final int WALKABLE_COLOR = 0xffFFD700;
final int NON_WALKABLE_COLOR = 0xffFF1000;

// images
final String WORLD_IMAGE = "chinese-illustration.jpg";
final String OBSTACLE_IMAGE = "obstacle.png";
final String HERO_IMAGE = "hero.png";
final String HUNTER_FOLLOWER_IMAGE = "hunter_follower.png";
final String HUNTER_INTERCEPTOR_IMAGE = "hunter_interceptor.png";
final String HUNTER_ROUTE_CUTTER_IMAGE = "hunter_route_cutter.png";
final String CLICK_TO_PLAY_IMAGE = "click_to_play.png";
final String YOU_WON_IMAGE = "you_won.png";
final String YOU_LOST_IMAGE = "you_lost.png";

// objects
World world;
View view;
Rules rules;
ArrayList<Obstacle> obstacles;
Hero hero;
ArrayList<Hunter> hunters;
// the next two lists contain the objects of the world to be drawn.
// the difference between the two is that animations can expire.
ArrayList<Drawable> drawables; 
ArrayList<Animation> animations;

// control flow
final int PLAYING = 0;
final int PAUSED = 1;
final int WON = 2;
final int LOST = 3;
int state;

// text images
PImage clickToPlay;
PImage youWon;
PImage youLost;


/**
one time init
**/
public void setup() {
  
  size(SCREEN_SIZE_X, SCREEN_SIZE_Y);
  background(0);
  imageMode(CORNER);
  
  // create the world
  PImage worldImage = loadImage(WORLD_IMAGE);
  world = new World(WORLD_SIZE_X, WORLD_SIZE_Y, GRID_SIZE_X, GRID_SIZE_Y, worldImage);
   
  // create the View
  view = new View(world);
 
  // create the obstacles
  PImage obstacleImage = loadImage(OBSTACLE_IMAGE); // http://www.widgetworx.com/spritelib/
  int spriteNumber = OBSTACLE_SPRITES;
  int spriteWidth = obstacleImage.width / spriteNumber;
  PImage[] obstacleSprites = new PImage[spriteNumber];
  for (int i = 0; i < spriteNumber; i++) {
    obstacleSprites[i] = obstacleImage.get(i * spriteWidth, 0, spriteWidth, obstacleImage.height);
  }
  obstacles = new ArrayList<Obstacle>();
  for (int i = 0; i < NUM_OBSTACLES; i++) {
    Obstacle obstacle = new Obstacle(null, obstacleSprites, world); // position will be assigned later, so set it to null for the time being
    obstacles.add(obstacle);
  }
 
  // load textImages
  clickToPlay = loadImage(CLICK_TO_PLAY_IMAGE);
  youWon = loadImage(YOU_WON_IMAGE);
  youLost = loadImage(YOU_LOST_IMAGE);
 
  // load and init objects that have to be initialized again for each new game
  load();
  
}

/**
load and init objects that have to be loaded for each new game
**/
public void load() {
  
  // create the rules and tell the world about them
  rules = new Rules(world);
  world.registerRules(rules);
  
  // position obstacles
  world.positionObstacles(obstacles);
  
  // create the hero and tell the world about it
  PImage heroImage = loadImage(HERO_IMAGE);
  hero = new Hero(rules.generateHeroStart(), HERO_SPEED, heroImage, world);
  world.registerHero(hero);
  
  // create the hunters and tell the world about them
  hunters = new ArrayList<Hunter>();
  // follower
  PImage followerImage = loadImage(HUNTER_FOLLOWER_IMAGE);
  Follower follower = new Follower(rules.generateHunterStart(hero), FOLLOWER_SPEED, followerImage, world);
  hunters.add(follower);
  // interceptor
  PImage interceptorImage = loadImage(HUNTER_INTERCEPTOR_IMAGE);
  Interceptor interceptor = new Interceptor(rules.generateHunterStart(hero), INTERCEPTOR_SPEED, interceptorImage, world);
  hunters.add(interceptor);
  // square
  PImage routeCutterImage = loadImage(HUNTER_ROUTE_CUTTER_IMAGE);
  RouteCutter routeCutter = new RouteCutter(rules.generateHunterStart(hero), ROUTE_CUTTER_SPEED, routeCutterImage, world);
  hunters.add(routeCutter);
  // tell world
  world.registerHunters(hunters);
  
  // create drawable list
  drawables = new ArrayList<Drawable>();
  
  // put drawables in list
  drawables.add(world);
  drawables.add(rules);
  for (Obstacle obstacle : obstacles) {
    drawables.add(obstacle);
  }
  drawables.add(hero);
  for (Hunter hunter : hunters) {
    drawables.add(hunter);
  }
  
  // create animation list
  animations = new ArrayList<Animation>();
  
  // set state
  state = PAUSED;
  
}

/**
main loop
**/
public void draw() {

  // do stuff
  if (state == PLAYING) {
    hero.doSomething();
    // we're nice and check for winning conditions first
    if (rules.checkWon()) {
      state = WON;
    }
    else {
      // only move the enemies if the player has not won
      for (Hunter hunter : hunters) {
        hunter.doSomething();
      }    
      // check if a hunter caught the player
      if (rules.checkLost()) {
        state = LOST;
      }
    }
  }

  // draw stuff
  
  // clear buffer
  background(0);

  // drawables
  for (Drawable drawable : drawables) {

    drawable.draw(view);
  }  

  // animations  
  Iterator<Animation> animationIterator = animations.iterator();
  while (animationIterator.hasNext()) {
    Animation animation = animationIterator.next();
    animation.draw(view);
    // animation objects die after their animation has ended
    if (animation.hasExpired()) {
      animationIterator.remove();
    }
  }
 
  // text
  if (state != PLAYING) {
    imageMode(CENTER);
    image(clickToPlay, SCREEN_SIZE_X / 2, SCREEN_SIZE_Y / 2);
    if (state == WON) {
      image(youWon, SCREEN_SIZE_X / 2, SCREEN_SIZE_Y / 2 - clickToPlay.height);
    }
    else if (state == LOST) {
      image(youLost, SCREEN_SIZE_X / 2, SCREEN_SIZE_Y / 2 - clickToPlay.height);
    }
  }

}

/**
mouse clicked event
**/
public void mouseClicked() {

  // if we are not currently playing, a mouse click starts the game
  if (state != PLAYING) {
    // if we just finished a game, load a new one
    if (state != PAUSED) {
      load();
    }
    state = PLAYING;
  }
  // otherwise, the click tells the hero where to go
  else {
    int colour;
    CoordinateInt gridPosition = view.gridPosition(new CoordinateInt(mouseX, mouseY));
    // we can only go to walkable squares. different colors for the clicking animation help the player
    if (world.isWalkable(gridPosition)) {
      colour = WALKABLE_COLOR;
      hero.setDestination(gridPosition);
    }
    else {
      colour = NON_WALKABLE_COLOR;
    }
    animations.add(new ClickAnimation(gridPosition, world.getTileSize(), colour));
  }

}

/**
key pressed event
**/
public void keyPressed() {
  if (key == 'p' || key == 'P') {
    state = PAUSED;
  }
}
/**
Agent parent class. The hero and the hunters will be subclasses of it.
**/
public abstract class Agent implements Drawable {
  
  private CoordinateDouble _position;
  private CoordinateInt _destination;
  private double _speed;
  private PImage _image;
  private World _world;
  private CoordinateDouble _direction; // vector pointing in the direction of the destination
  private float _lastTheta; // last angle the agent sprite was rotated. used to prevent the sprite from always facing the same direction once its destination has been reached
  
  /**
  Constructor
  **/
  public Agent(CoordinateInt position, Double speed, PImage image, World world) {
    _position = world.gridCenterToPosition(position);
    _destination = position;
    _direction = new CoordinateDouble(0, 0);
    _speed = speed;
    _image = image;
    _world = world;
    _lastTheta = atan2(0, 0) + PI/2;
  }
  
  /**
  Agent's main loop
  **/  
  public abstract void doSomething();
  
  /**
  Position getter
  **/
  public CoordinateDouble getPosition() {
    return _position;
  }
  
  /**
  Position setter
  **/
  public void setDestination(CoordinateInt destination) {
    _destination = destination;
  }
  
  /**
  Destination getter
  **/
  public CoordinateInt getDestination() {
    return _destination;
  }
  
  /**
  World getter
  **/
  public World getWorld() {
    return _world;
  }
  
  /**
  Move an agent in the direction of its current destination
  **/
  protected void move() {
    // calculate direction vector
    _direction = new CoordinateDouble(_destination.getX(), _destination.getY());
    _direction = _world.gridCenterToPosition(_destination);
    _direction.minus(_position);
    
    // usually, move one unit of speed of the agent. but if the destination is closer than one speed unit, limit the speed in order to not overshoot the destination
    if (_direction.magnitude() > _speed) {
      _direction.normalize();
      _direction.times(_speed);
    }
    
    // calculate the new position. do not step on not walkable squares
    CoordinateDouble newPosition = new CoordinateDouble(_position.getX(), _position.getY());
    newPosition.plus(_direction);
    if (_world.isWalkable(newPosition)) {
      _position = newPosition;
    }
  }
  
  /**
  Draw the agent
  **/
  public void draw(View view) {
    CoordinateInt position = view.convert(_position);
    
    // angle to draw sprite. if we have reached the destination, use the last angle to avoid the agent facing the direction of atan2(0, 0) + PI/2
    float theta;
    if (_direction.magnitude() == 0) {
      theta = _lastTheta;
    }
    else {
      theta = atan2((float) _direction.getY(), (float) _direction.getX()) + PI/2;
    }
    _lastTheta = theta;
    
    // draw the sprite
    pushMatrix();
    translate(position.getX(), position.getY());
    rotate(theta);
    imageMode(CENTER);
    image(_image, 0, 0);
    popMatrix();
  }
}
/**
Animations are Drawables that can expire
**/
public interface Animation extends Drawable {
  
  /**
  Tell if the animation has expired and should be deleted
  **/
  public boolean hasExpired();
}
/**
Animate a mouse click on a sqaure
**/
public class ClickAnimation implements Animation {
  
  private final int _WEIGHT = 6;
  private final int _HALF = _WEIGHT / 2;
  private final int _A = 255;
  private final int _DURATION = 30;
  
  private CoordinateInt _gridPosition;
  private CoordinateDouble _size;
  private CoordinateInt _viewPosition;
  private CoordinateInt _viewSize;
  private int _color;
  private int _counter;
  private int _alpha;
  
  /**
  Constructor
  **/
  public ClickAnimation(CoordinateInt gridPosition, CoordinateDouble size, int colour) {
    _gridPosition = gridPosition;
    _size = size;
    _color = colour;
    _viewPosition = null;
    _counter = _DURATION;
    _alpha = _A;
  }
  
  /**
  Draw the animation. It is a slowly fading unfilled rectangle of the size of the square
  **/
  public void draw(View view) {
    // init the position of where to draw on the first frame and save it for subsequent frames
    if (_viewPosition == null) {
      _viewPosition = view.convert(_gridPosition);
      _viewSize = view.convert(_size);
    }
    // draw the animation
    strokeWeight(_WEIGHT);
    noFill();
    stroke(_color, _alpha);
    rect(_viewPosition.getX() + _HALF, _viewPosition.getY() + _HALF, _viewSize.getX() - _WEIGHT, _viewSize.getY() - _WEIGHT);
    _counter--;
    _alpha -= _A / _DURATION;
  }
  
  /**
  Expire after the counter runs out (the rectangle has faded)
  **/
  public boolean hasExpired() {
    return _counter <= 0;
  }
}
/**
Class to hold a pair of double x, y coordinates. Unfortunately, Java generics do not support primitive types, so a class for each type is necessary.
**/
public class CoordinateDouble {
  private double _x, _y;
  
  /**
  Constructor
  **/
  public CoordinateDouble(double x, double y) {
    this.set(x, y);
  }
  
  /**
  Setter for both coordinates
  **/
  public void set(double x, double y) {
    _x = x;
    _y = y;
  }
  
  /**
  Setter for x
  **/
  public void setX(double x) {
    _x = x;
  }
  
  /**
  Setter for y
  **/
  public void setY(double y) {
    _y = y;
  }
  
  /**
  Getter for x
  **/
  public double getX() {
    return _x;
  }
  
  /**
  Getter for y
  **/
  public double getY() {
    return _y;
  }
  
  /**
  Add two coordinates. Save the result in this object.
  **/
  public void plus(CoordinateDouble other) {
    this.setX(this.getX() + other.getX());
    this.setY(this.getY() + other.getY());
  }
  
  /**
  Subtract two coordinates. Save the result in this object.
  **/
  public void minus(CoordinateDouble other) {
    this.setX(this.getX() - other.getX());
    this.setY(this.getY() - other.getY());
  }
  
  /**
  Multiply a coordinate by a scalar. Save the result in this object.
  **/
  public void times(double scalar) {
    this.setX(this.getX() * scalar);
    this.setY(this.getY() * scalar);
  }
  
  /**
  Divide a coordinate by a scalar. Save the result in this object.
  **/
  public void dividedBy(double scalar) {
    this.setX(this.getX() / scalar);
    this.setY(this.getY() / scalar);
  }
  
  /**
  Compare two coordinates
  **/
  public boolean equals(CoordinateDouble other) {
    return this.getX() == other.getX() && this.getY() == other.getY();
  }
  
  /**
  Calculate the magnitude of the coordinate (vector)
  **/
  public double magnitude() {
    return sqrt(pow((float) this.getX(), (float) 2) + pow((float) this.getY(), (float) 2));
  }
  
  /**
  Normalize the coordinate (vector)
  **/
  public void normalize() {
    if (this.magnitude() != 0) {
      this.dividedBy(this.magnitude());
    }
  } 
}
/**
Class to hold a pair of int x, y coordinates. Unfortunately, Java generics do not support primitive types, so a class for each type is necessary.
**/
public class CoordinateInt {
  private int _x, _y;
  
  /**
  Constructor
  **/
  public CoordinateInt(int x, int y) {
    this.set(x, y);
  }
  
  /**
  Setter for both coordinates
  **/
  public void set(int x, int y) {
    _x = x;
    _y = y;
  }
  
  /**
  Setter for x
  **/
  public void setX(int x) {
    _x = x;
  }
  
  /**
  Setter for y
  **/
  public void setY(int y) {
    _y = y;
  }
  
  /**
  Getter for x
  **/
  public int getX() {
    return _x;
  }
  
  /**
  Getter for y
  **/
  public int getY() {
    return _y;
  }
  
  /**
  Add two coordinates. Save the result in this object.
  **/
  public void plus(CoordinateInt other) {
    this.setX(this.getX() + other.getX());
    this.setY(this.getY() + other.getY());
  }
  
  /**
  Subtract two coordinates. Save the result in this object.
  **/
  public void minus(CoordinateInt other) {
    this.setX(this.getX() - other.getX());
    this.setY(this.getY() - other.getY());
  }
  
  /**
  Multiply a coordinate by a scalar. Save the result in this object.
  **/
  public void times(int scalar) {
    this.setX(this.getX() * scalar);
    this.setY(this.getY() * scalar);
  }
  
  /**
  Divide a coordinate by a scalar. Save the result in this object.
  **/
  public void dividedBy(int scalar) {
    this.setX(this.getX() / scalar);
    this.setY(this.getY() / scalar);
  }
  
  /**
  Compare two coordinates
  **/
  public boolean equals(CoordinateInt other) {
    return this.getX() == other.getX() && this.getY() == other.getY();
  }
  
  /**
  Calculate the magnitude of the coordinate (vector)
  **/
  public double magnitude() {
    return sqrt(pow((float) this.getX(), (float) 2) + pow((float) this.getY(), (float) 2));
  }
  
}
/**
Interface for objects that can be drawn
**/
interface Drawable {
  
  /**
  Method in which objects implement how they are drawn. They take a view to convert from world coordinates to screen coordinates
  **/
  public void draw(View view);
}
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



/**
Parent class for all the hunters
**/
public abstract class Hunter extends Agent {
  
  private GraphSearch_Astar _aStar;
  
  /**
  Constructor. Initializes A*
  **/
  public Hunter(CoordinateInt position, Double speed, PImage image, World world) {
    super(position, speed, image, world);
    _aStar = new GraphSearch_Astar(world.getGraph());
  }
  
  /**
  Main loop for Hunters. First, calculate the destination, which is different depending on the strategy the Hunter uses.
  Then apply A* to find a path to the destination. Next, go towards the closest node on the path.
  **/
  public void doSomething() {
    // have the child calculate the destination
    calculateDestination();
    
    // execute A* to get the next node on the path to the destination
    CoordinateInt gridPosition = this.getWorld().positionToGrid(this.getPosition()); 
    int from = gridPosition.getY() * this.getWorld().getGridSize().getX() + gridPosition.getX();
    int to = this.getDestination().getY() * this.getWorld().getGridSize().getX() + this.getDestination().getX();
    // don't do anything if we are already at the destination. the A* library does not seem to like this
    if (from == to) {
      return;
    }

    // the search can throw a null pointer exception. we have no path in that case, so the hunter can't move. we simply exit the method.
    LinkedList<GraphNode> nodes;
    try {
      nodes = _aStar.search(from, to);
    }
    catch (NullPointerException e) {
      return;
    }
    
    // try to reach the second element in the list (if it exists). the first one is our current position
    if (nodes.pollFirst() != null) {
      GraphNode next = nodes.pollFirst();
      if (next != null) {
        // calculate x and y from the node id
        int x = next.id() % this.getWorld().getGridSize().getX();
        int y = next.id() / this.getWorld().getGridSize().getX();
        // set the destination as the next node
        this.setDestination(new CoordinateInt(x, y));
        // move the hunter
        move();
      }
    }
  }
  
  /**
  Method to find the destination of a Hunter. It will depend on the Hunter's strategy.
  **/
  protected abstract void calculateDestination();
  
}
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
    _spriteStep = 0.16f;
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
/**
This Hunter tries to cut off escape routes by filling holes the other two hunters leave.
**/
public class RouteCutter extends Hunter {
  
  /**
  Constructor
  **/
  public RouteCutter(CoordinateInt position, Double speed, PImage image, World world) {
    super(position, speed, image, world);
  }
  
  /**
  The two other hunters form a square. They are on two of the corners and the other two are free.
  Go to the corner closer (Manhattan distance) to the goal.
  **/
  protected void calculateDestination() {
        
    // get the first hunter from the list
    ArrayList<Hunter> hunters = this.getWorld().getHunters();
    Iterator<Hunter> it = hunters.iterator();
    Hunter one = it.next();
    // check that we are not popping outselves
    if (one == this) {
      one = it.next();
    }
    
    // get the second hunter from the list
    Hunter two = it.next();
    // check that we are not popping outselves
    if (two == this) {
      two = it.next();
    }
    
    // get the relevant coordinates
    int x1 = this.getWorld().positionToGrid(one.getPosition()).getX();
    int y1 = this.getWorld().positionToGrid(one.getPosition()).getY();
    int x2 = this.getWorld().positionToGrid(two.getPosition()).getX();
    int y2 = this.getWorld().positionToGrid(two.getPosition()).getY();
    int gx = this.getWorld().getRules().getGoal().getX();
    int gy = this.getWorld().getRules().getGoal().getY();
    
    // calculate manhattan distance to goal of both points
    int distance1 = abs(gx - x1) + abs(gy - y2);
    int distance2 = abs(gx - x2) + abs(gy - y1);
    
    // choose the closer one
    CoordinateInt destination;
    if (distance1 < distance2) {
      destination = new CoordinateInt(x1, y2);
    }
    else {
      destination = new CoordinateInt(x2, y1);
    }
      
    // make sure the destination is walkable. if not, slowly approach the hero. this ensures the algorithm ends.
    while(!this.getWorld().isWalkable(destination)) {
      // take a look at which dimension is further away from hero and decrease that one 
      int gradientX = destination.getX() - this.getWorld().positionToGrid(this.getWorld().getHero().getPosition()).getX();
      int gradientY = destination.getY() - this.getWorld().positionToGrid(this.getWorld().getHero().getPosition()).getY();
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
/**
Class that implements the rules of the game
**/
public class Rules implements Drawable {
  
  private final int _GOAL_COLOR = 0xff00FF00;
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
    double positionX = (gridPosition.getX() + 0.5f) * _tileSize.getX();
    double positionY = (gridPosition.getY() + 0.5f) * _tileSize.getY();
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
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "challenge" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
