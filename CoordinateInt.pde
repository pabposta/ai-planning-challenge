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
