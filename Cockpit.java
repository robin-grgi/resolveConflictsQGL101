package fr.unice.polytech.si3.qgl.merj;

import java.util.*;
import java.util.logging.Level;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.si3.qgl.merj.actions.Action;
import fr.unice.polytech.si3.qgl.merj.boat.Sailor;
import fr.unice.polytech.si3.qgl.merj.boat.Ship;
import fr.unice.polytech.si3.qgl.merj.entities.boatEntities.BoatEntity;
import fr.unice.polytech.si3.qgl.merj.entities.boatEntities.usage.EntityUsage;
import fr.unice.polytech.si3.qgl.merj.entities.seaEntities.*;
import fr.unice.polytech.si3.qgl.merj.geometry.Segment;
import fr.unice.polytech.si3.qgl.merj.initializers.entities.seaEntities.*;
import fr.unice.polytech.si3.qgl.merj.initializers.pathfinding.ObjectiveInitializer;
import fr.unice.polytech.si3.qgl.merj.initializers.boat.ShipInitializer;
import fr.unice.polytech.si3.qgl.merj.initializers.pathfinding.PathfinderInitializer;
import fr.unice.polytech.si3.qgl.merj.pathfinding.Objective;
import fr.unice.polytech.si3.qgl.merj.pathfinding.Pathfinder;
import fr.unice.polytech.si3.qgl.merj.pathfinding.Tile;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Circle;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Rectangle;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Shape;
import fr.unice.polytech.si3.qgl.merj.utils.ConsoleLog;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Position;
import fr.unice.polytech.si3.qgl.regatta.cockpit.ICockpit;

public class Cockpit implements ICockpit {
    private final ObjectMapper objectMapper = new ObjectMapper();
    protected Ship ship;
    protected AllCheckpoints allCheckpoints = new AllCheckpoints();
    protected AllSeaEntities allSeaEntities = new AllSeaEntities();
    protected Wind wind;
    protected Objective objective;
    protected Pathfinder pathfinder;

    public void initGame(String game) {
        ConsoleLog.setup(Level.SEVERE);
        try {
            JsonNode gameJsonNode = objectMapper.readTree(game);

            ship = new ShipInitializer().initWithJson(gameJsonNode);
            allCheckpoints = new AllCheckpointsInitializer().initWithJson(gameJsonNode.get("goal").get("checkpoints"));
            objective = new ObjectiveInitializer(allCheckpoints, ship).getObjective();
        } catch (Exception e) {
            ConsoleLog.log(e.getMessage());
        }
    }

    public String nextRound(String round) {
        String nextActions = "[]";
        try {
            resetAttributes();
            JsonNode roundNode = objectMapper.readTree(round);
            updateRound(roundNode);

            Checkpoint current = allCheckpoints.getCurrent();

            Checkpoint nextCheckpoint = allCheckpoints.nextCheckpointToValidate(ship);

            if (nextCheckpoint.hasReachedCircleCheckpoint(ship)) {
                nextCheckpoint.status(true);
                allCheckpoints.setThereIsNewCheckpoint(true);
                //nextCheckpoint = allCheckpoints.nextCheckpointToValidate(ship);
            }

            System.out.println("Next checkpoint = " + nextCheckpoint);

            usePathfindingIfNeeded(current, nextCheckpoint);

            Checkpoint objectiveCheckpoint = objective.getCurrent();
            Position objectivePosition = objectiveCheckpoint.getPosition();

            double wantedOrientation = ship.getPosition().getOrientationDifference(objectivePosition);
            double windOrientation = wind.getOrientation();

            Map<String, EntityUsage> entityUsage = ship.getDeck().estimateEntityUsage(wantedOrientation, ship.getCrew().size(), windOrientation, ship.getPosition());

            List<Action> actions = ship.getCrew().getWantedActions(ship.getDeck().getEntities(), entityUsage, ship.getDeck().getWidth());
            System.out.println(actions);
            nextActions = objectMapper.writerFor(new TypeReference<List<Action>>() {
            }).writeValueAsString(actions);

        } catch (Exception e) {
            ConsoleLog.log(e.getMessage());
        }
        return nextActions;
    }

    protected void updateRound(JsonNode roundJson) throws Exception {
        if (roundJson.has("wind"))
            wind = new WindInitializer().initWithJson(roundJson.get("wind"));
        //if the visible entities array is set and if there is a stream in this array
        if (roundJson.has("visibleEntities")) {

            int currentReefCount = allSeaEntities.getReefs().size();
            int currentVisibleEntities = allSeaEntities.size();
            allSeaEntities.addAll(new VisibleSeaEntitiesInitializer().initWithJson(roundJson.get("visibleEntities")));
            allSeaEntities.setThereAreNewReefs(currentReefCount < allSeaEntities.getReefs().size());
        }

        JsonNode shipJson = roundJson.get("ship");
        JsonNode position = objectMapper.readTree(String.valueOf(shipJson.get("position")));
        ship.setPosition(new Position(
                position.get("x").asDouble(),
                position.get("y").asDouble(),
                position.get("orientation").asDouble()));
    }

    protected void resetAttributes() {
        ship.getCrew().forEach(Sailor::free);
        ship.getDeck().getEntities().forEach((name, list) -> list.forEach(BoatEntity::stopUsing));
    }

    protected void usePathfindingIfNeeded(Checkpoint current, Checkpoint nextCheckpoint) {
        if (current == null || !current.equals(nextCheckpoint)) {
            allSeaEntities.setThereAreNewSeaEntities(true);
            if (current != null) {
                current.status(true);
            }
        }

        if (allSeaEntities.areThereNewSeaEntities() || allCheckpoints.isThereNewCheckpoint()) {
            pathfinding(nextCheckpoint);
            getObjectiveCheckpoint();
            allSeaEntities.setThereAreNewSeaEntities(false);
            allCheckpoints.setThereIsNewCheckpoint(false);
        } else {
            System.out.println("BEFORE REACHED CIRCLE FOR INTERMEDIATE CHECKPOINT");
            if (objective.getCurrent().hasReachedCircleCheckpoint(ship) || hasShipCrossedOverCheckpoint(objective.getCurrent())) {
                getObjectiveCheckpoint();
            }
        }

        System.out.println("Objective current checkpoint = " + objective.getCurrent());
    }

    public boolean hasShipCrossedOverCheckpoint(Checkpoint checkpoint) {
        if (ship.getLastPosition().equals(ship.getPosition())) {
            return false;
        }
        Segment segment = new Segment(ship.getLastPosition(), ship.getPosition());

        boolean res = checkpoint.getShape().intersects(segment);
        System.out.println("hasShipCrossedOverCheckpoint = " + res);
        return res;
    }

    protected void pathfinding(Checkpoint nextCheckpoint) {
        objective.clear();

        Position nextCheckpointPosition = nextCheckpoint.getPosition();


        System.out.println("All sea entities size = " + allSeaEntities.size());

        if (!objective.someSeaEntitiesAreBetweenShipAndCheckPointToReach(ship.getPosition(), nextCheckpointPosition, allSeaEntities.stream().toList())) {
            System.out.println("No sea entities in the way");
            objective.addCheckpoint(nextCheckpoint);
        } else {
            System.out.println("/!\\ Sea entities are in the way");

            ArrayList<Checkpoint> allCheckpointInMap = new ArrayList<>();
            allCheckpointInMap.add(nextCheckpoint);

            for (SeaEntity currentSeaEntity : allSeaEntities) {
                Checkpoint checkpoint = new Checkpoint(currentSeaEntity.getPosition(), currentSeaEntity.getShape());
                allCheckpointInMap.add(checkpoint);
            }

            this.pathfinder = new PathfinderInitializer(allCheckpointInMap, ship.getPosition()).getPathfinder();

            //TODO : modify Pathfinder.addReefsToMap() to consider stream (use commented code below)
            //Consider Streams and fake remove reefs
  /*
            for(SeaEntity currentEntity: allSeaEntities) {
                int cpt = 0;
                for(Tile currentTile : pathfinder.getMap().getNodes()) {
                    Shape entityShape = null;
                    if(currentEntity.getShape() instanceof Rectangle rectangle) {
                        entityShape = new Rectangle(rectangle.getWidth(), rectangle.getHeight(), rectangle.getPosition());
                    } else if(currentEntity.getShape() instanceof Circle circle) {
                        entityShape = new Rectangle(circle.getRadius() * 2, circle.getRadius() * 2, new Position());
                    }

                    if(entityShape != null && currentEntity instanceof Reef) {
                        entityShape.increaseSizeByAmount(100);
                        if(entityShape.containsPoint(currentTile.getPosition())) {
                            currentTile.setReef(true);
                            cpt++;
                        }
                    } else if(entityShape != null && currentEntity instanceof Stream stream) {
                        if(entityShape.containsPoint(currentTile.getPosition())) {
                            currentTile.addStream(stream);
                            cpt++;
                        }
                    }
                }
                System.out.println("Entity : " + currentEntity + " has " + cpt + " tiles inside");
            }
            */
            //Consider Streams and fake remove reefs
          
            ArrayList<Reef> reefs = new ArrayList<>(allSeaEntities.getReefs());
            this.pathfinder.addReefsToMap(reefs);

            System.out.println("Ship position = " + ship.getPosition());
            Tile start = pathfinder.getMap().getClosestNode(ship.getPosition());
            System.out.println("Start tile = " + start);
            Tile end = pathfinder.getMap().getClosestNode(nextCheckpointPosition);
            System.out.println("End tile = " + end);
            pathfinder.pathfinding(start, end, ship.getSpeed(wind, allSeaEntities.getStreams()));
            System.out.println("Pathfinding is finished !");

            ArrayList<Position> positions = new ArrayList<>();
            for (Tile tile : pathfinder.getPath()) {
                positions.add(tile.getPosition());
                System.out.println("PATH = " + tile.getPosition());
            }

            positions.add(nextCheckpointPosition);

            Collections.reverse(positions);
            objective.addPositionsAsCheckpoints(positions);
        }
    }

    protected void getObjectiveCheckpoint() {
        Checkpoint checkpoint = objective.nextFurthestCheckpointToReach(ship.getPosition(), new ArrayList<>(allSeaEntities));
        if (checkpoint != null) {
            objective.setCurrent(checkpoint);
        } else {
            if (!objective.getCheckpoints().isEmpty()) {
                System.out.println("Reef in between intermediate checkpoints");
                pathfinding(objective.getCheckpoints().pop());

                Checkpoint checkpoint2 = objective.nextFurthestCheckpointToReach(ship.getPosition(), new ArrayList<>(allSeaEntities));
                if (checkpoint2 != null) {
                    objective.setCurrent(checkpoint2);
                }
                else {
                    System.out.println("ALERT");
                }
            }
        }
    }

    @Override
    public List<String> getLogs() {
        return new ArrayList<>();
    }
}
