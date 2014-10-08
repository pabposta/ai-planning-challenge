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
