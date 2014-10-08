/*
Controls: Click where you want to go. p or P pauses the game.

This is a sketch for the AI Planning course on Coursera. The goal is to reach the green square, while avoiding the hunting airplanes. There are some obstacles, indicating squares that cannot be visited.
The hunting planes have different strategies. The first one follows you around. It goes where you go. The second one is an interceptor that always tries to go between you and the goal. The third one tries to cut off posible escape routes. Its position depends on that of the other hunters. Together they form a coordinated team.
All hunters use A* for path finding. Path finding is performed on the squares (the grid), and is therefore quick enough to allow recalculation of the path in real time. Cost between horizontally or vertically adjacent squares is 1 and sqrt(2) for diagonally adjacent squares. The heuristic uses the euclidean distance. The implementation of A* is that of Peter Lager's Path finding library (http://www.lagers.org.uk/pfind/index.html).
All images used are public. List of sources of the images not created by me:
http://www.publicdomainpictures.net/view-image.php?image=66948&picture=chinese-illustration
http://www.widgetworx.com/spritelib/
*/

import java.util.Iterator;

// constants
final int NUM_OBSTACLES = 40;
final int OBSTACLE_SPRITES = 4;
final double HERO_SPEED = 2.0;
final double FOLLOWER_SPEED = 1.2;
final double INTERCEPTOR_SPEED = 1.1;
final double ROUTE_CUTTER_SPEED = 1.0;
final int SCREEN_SIZE_X = 640;
final int SCREEN_SIZE_Y = 480;
final int WORLD_SIZE_X = 640;
final int WORLD_SIZE_Y = 480;
final int GRID_SIZE_X = 32;
final int GRID_SIZE_Y = 24;
final int WALKABLE_COLOR = #FFD700;
final int NON_WALKABLE_COLOR = #FF1000;

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
void setup() {
  
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
void load() {
  
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
void draw() {

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
void mouseClicked() {

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
void keyPressed() {
  if (key == 'p' || key == 'P') {
    state = PAUSED;
  }
}
