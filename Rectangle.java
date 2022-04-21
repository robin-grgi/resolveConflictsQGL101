package fr.unice.polytech.si3.qgl.merj.geometry.shapes2;

import fr.unice.polytech.si3.qgl.merj.geometry.Point;
import fr.unice.polytech.si3.qgl.merj.geometry.Segment;

import java.util.List;
import java.util.Objects;

public class Rectangle extends Shape{

    private double width;
    private double height;

    // Constructors

    public Rectangle() {
        super();
    }

    public Rectangle(double width, double height, Position position) {
        super(position);
        this.width = width;
        this.height = height;
    }

    // Accessors
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }


    // Methods
    @Override
    public Rectangle rotatedShape(Position position) {
        this.position = position;
        Position newPosition = new Position(this.position.getX(), this.position.getY(), position.getOrientation());
        return new Rectangle(this.width, this.height, newPosition);
    }

    @Override
    public boolean containsPoint(Point point) {
        Point rotatedPoint = point.rotation(this.position, this.getOrientation());
        if(rotatedPoint.getX() > this.position.getX() + height/2) {
            return false;
        } if(rotatedPoint.getX() < this.position.getX() - height/2){
            return false;
        } if (rotatedPoint.getY() > this.position.getY() + width/2){
            return false;
        } if (rotatedPoint.getY() < this.position.getY() - width/2){
            return false;
        }
        return true;
    }

    @Override
    public boolean intersects(Segment segment) {
        System.out.println("intersectionPointExists: :" + segment.intersectionPointsExist(this.getSegments()));
        System.out.println("containsPoint1 : " + this.containsPoint(segment.getPointA()));
        System.out.println("containsPoint2 : " + this.containsPoint(segment.getPointB()));
        return segment.intersectionPointsExist(this.getSegments()) || (this.containsPoint(segment.getPointA()) && this.containsPoint(segment.getPointB()));
    }

    public List<Segment> getSegments() {
        Point p1 = getVertices().get(0);
        Point p2 = getVertices().get(1);
        Point p3 = getVertices().get(2);
        Point p4 = getVertices().get(3);

        Segment s1 = new Segment(p1, p2);
        Segment s2 = new Segment(p2, p3);
        Segment s3 = new Segment(p3, p4);
        Segment s4 = new Segment(p4, p1);
        return List.of(s1, s2, s3, s4);
    }

    @Override
    public void increaseSizeByAmount(double amount){
        this.height += amount;
        this.width += amount;
    }

    @Override
    public String toString() {
        return "Rectangle{" +
                "width=" + width +
                ", height=" + height +
                ", orientation=" + this.getOrientation() +
                ", position=" + this.getPosition() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Rectangle rectangle) {
            return width == rectangle.getWidth() &&
                    height == rectangle.getHeight() &&
                    this.getOrientation() == rectangle.getOrientation();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, this.getOrientation());
    }

    public List<Point> getVertices() {
        Point p1 = new Point(this.position.getX() + this.height/2, this.position.getY() + this.width/2).rotation(this.position, 1*this.getOrientation());
        Point p2 = new Point(this.position.getX() + this.height/2, this.position.getY() - this.width/2).rotation(this.position, 1*this.getOrientation());
        Point p3 = new Point(this.position.getX() - this.height/2, this.position.getY() - this.width/2).rotation(this.position, 1*this.getOrientation());
        Point p4 = new Point(this.position.getX() - this.height/2, this.position.getY() + this.width/2).rotation(this.position, 1*this.getOrientation());
        return List.of(p1, p2, p3, p4);
    }
}

