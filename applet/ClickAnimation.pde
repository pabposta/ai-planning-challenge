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
