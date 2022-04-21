package fr.unice.polytech.si3.qgl.merj.pathfinding;

import fr.unice.polytech.si3.qgl.merj.boat.Ship;
import fr.unice.polytech.si3.qgl.merj.entities.seaEntities.Checkpoint;
import fr.unice.polytech.si3.qgl.merj.entities.seaEntities.Reef;

import fr.unice.polytech.si3.qgl.merj.entities.seaEntities.SeaEntity;
import fr.unice.polytech.si3.qgl.merj.geometry.Point;
import fr.unice.polytech.si3.qgl.merj.geometry.Segment;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Circle;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Position;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Rectangle;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Objective {
    private Stack<Checkpoint> checkpoints = new Stack<>();
    private Checkpoint current;
    private Ship ship;

    // Constructor
    public Objective(Ship ship) {
        current = null;
        this.ship = ship;
    }

    // Accessors
    public int size() {
        return checkpoints.size();
    }

    public Stack<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public Checkpoint getCurrent() {
        return current;
    }

    public void setCurrent(Checkpoint current) {
        this.current = current;
    }

    // Methods
    public void addCheckpoint(Checkpoint checkpoint) {
        checkpoints.push(checkpoint);
    }

    public void addCheckpoints(List<Checkpoint> checkpointsToAdd) {
        for (Checkpoint checkpoint : checkpointsToAdd) {
            addCheckpoint(checkpoint);
        }
    }

    public void addPositionsAsCheckpoints(List<Position> positionsToAdd) {
        // Circle shape = new Circle(50);

        for (Position position : positionsToAdd) {
            Circle shape = new Circle(position, 50);
            addCheckpoint(new Checkpoint(position, shape));
        }
    }

    public Checkpoint getNextCheckpoint() {
        if (checkpoints.isEmpty()) {
            return null;
        }
        current = checkpoints.pop();
        return current;
    }

    public void clear() {
        checkpoints.clear();
    }

    public Checkpoint nextFurthestCheckpointToReach(Position ship, List<SeaEntity> seaEntities) {
        Checkpoint furthest = null;
        while (!checkpoints.isEmpty()) {
            Checkpoint checkpoint = checkpoints.peek();

            System.out.println("Potential furthest = " + checkpoint);
            if (someSeaEntitiesAreBetweenShipAndCheckPointToReach(ship, checkpoint.getPosition(), seaEntities)) {
                return furthest;
            } else {
                furthest = checkpoints.pop();

            }
        }
        return furthest;
    }

    public boolean someSeaEntitiesAreBetweenShipAndCheckPointToReach(Position ship, Position checkpointPosition,
            List<SeaEntity> seaEntities) {
        System.out.println("------- IN SOME SEA ENTITIES ARE BETWEEN SHIP AND CHECKPOINT -------");
        if (seaEntities == null || seaEntities.isEmpty()) {
            return false;
        }

        for (SeaEntity currentEntity : seaEntities) {
            System.out.println("A sea entity is between ship and checkpoint " + checkpointPosition + " ? ");
            boolean aSeaEntityIsOnTheWayWithSegmentIntersection = aSeaEntityIsOnTheWayWithSegmentIntersection(ship,
                    checkpointPosition, currentEntity);
            System.out.println("aSeaEntityIsOnTheWayWithSegmentIntersection for seaEntity "
                    + currentEntity.getPosition() + " = " + aSeaEntityIsOnTheWayWithSegmentIntersection);
            System.out.println("currentEntity.getShape = " + currentEntity.getShape());
            if (aSeaEntityIsOnTheWayWithSegmentIntersection) {

                return true;
            }
        }
        return false;
    }

    public boolean aReefIsOnTheWayWithSegmentIntersection(Position ship, Position checkpointToReach,
            Position reefToAvoidPos, Shape reefToAvoidShape) {
        System.out.println("------- IN A REEF IN ON THE WAY WITH segment intersections -------");
        if (reefToAvoidPos == null || reefToAvoidShape == null) {
            return false;
        }
        Shape reefShape = null;
        if (reefToAvoidShape instanceof Rectangle rectangle) {
            reefShape = new Rectangle(rectangle.getWidth(), rectangle.getHeight(), rectangle.getPosition());
            reefShape.increaseSizeByAmount(30);
            System.out.println(reefShape);

        } else if (reefToAvoidShape instanceof Circle circle) {
            reefShape = new Rectangle(circle.getRadius() * 2, circle.getRadius() * 2, circle.getPosition());
            // reefShape = circle;
            reefShape.increaseSizeByAmount(30);
        }

        Point a = new Point(ship.getX(), ship.getY());
        Point b = new Point(checkpointToReach.getX(), checkpointToReach.getY());
        Segment segmentShipToCheckpoint = new Segment(a, b);
        assert reefShape != null;
        return reefShape.intersects(segmentShipToCheckpoint);
    }

    public boolean aSeaEntityIsOnTheWayWithSegmentIntersection(Position shipPosition,
            Position checkpointToReachPosition, SeaEntity seaEntity) {
        if (seaEntity == null) {
            return false;
        }
        System.out.println("------- IN A REEF IN ON THE WAY WITH segment intersections -------");
        if (seaEntity.getPosition() == null || seaEntity.getShape() == null) {
            return false;
        }
        Rectangle seaEntityShape = null;
        if (seaEntity.getShape() instanceof Rectangle rectangle) {
            seaEntityShape = new Rectangle(rectangle.getWidth(), rectangle.getHeight(), rectangle.getPosition());
            if (seaEntity instanceof Reef) {
                seaEntityShape.increaseSizeByAmount(100);
            }
        } else if (seaEntity.getShape() instanceof Circle circle) {
            seaEntityShape = new Rectangle(circle.getRadius() * 2, circle.getRadius() * 2, new Position());
            if (seaEntity instanceof Reef) {
                seaEntityShape.increaseSizeByAmount(100);
            }
        }

        Point a = new Point(shipPosition.getX(), shipPosition.getY());
        Point b = new Point(checkpointToReachPosition.getX(), checkpointToReachPosition.getY());
        Segment segmentShipToCheckpoint = new Segment(a, b);
        assert seaEntityShape != null;
        return seaEntityShape.intersects(segmentShipToCheckpoint);
    }

}
