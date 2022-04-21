package fr.unice.polytech.si3.qgl.merj;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.unice.polytech.si3.qgl.merj.actions.Action;
import fr.unice.polytech.si3.qgl.merj.actions.MovingAction;
import fr.unice.polytech.si3.qgl.merj.actions.OarAction;
import fr.unice.polytech.si3.qgl.merj.actions.TurnAction;
import fr.unice.polytech.si3.qgl.merj.boat.*;
import fr.unice.polytech.si3.qgl.merj.entities.boatEntities.*;
import fr.unice.polytech.si3.qgl.merj.entities.seaEntities.AllCheckpoints;
import fr.unice.polytech.si3.qgl.merj.entities.seaEntities.Checkpoint;
import fr.unice.polytech.si3.qgl.merj.entities.seaEntities.Reef;
import fr.unice.polytech.si3.qgl.merj.entities.seaEntities.Wind;
import fr.unice.polytech.si3.qgl.merj.exceptions.ImpossibleActionException;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Circle;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Position;
import fr.unice.polytech.si3.qgl.merj.pathfinding.Objective;

import fr.unice.polytech.si3.qgl.merj.utils.Constants;
import fr.unice.polytech.si3.qgl.merj.geometry.shapes2.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CockpitTest {

    Cockpit cockpit;
    JsonNode shipJson;
    String round;
    String initGame;
    Crew crew;
    Map<String, Set<BoatEntity>> entities;

    @BeforeEach
    void setUp() throws JsonProcessingException, ImpossibleActionException {
        cockpit = new Cockpit();
        ObjectMapper objectMapper = new ObjectMapper();
        initGame = """
                {"goal": {
                    "mode": "REGATTA",
                    "checkpoints": [
                      {
                        "position": {
                          "x": 1000,
                          "y": 0,
                          "orientation": 0
                        },
                        "shape": {
                          "type": "rectangle",
                      "width": 3,
                      "height": 6,
                      "orientation": 0
                        }
                      },
                      {
                        "position": {
                          "x": 0,
                          "y": 0,
                          "orientation": 0
                        },
                        "shape": {
                          "type": "rectangle",
                      "width": 3,
                      "height": 6,
                      "orientation": 0
                        }
                      }
                    ]
                  },
                  "ship": {
                    "type": "ship",
                    "life": 100,
                    "position": {
                      "x": 0,
                      "y": 0,
                      "orientation": 0
                    },
                    "name": "Les copaings d'abord!",
                    "deck": {
                      "width": 3,
                      "length": 6
                    },
                    "entities": [
                      {
                        "x": 1,
                        "y": 0,
                        "type": "oar"
                      },
                      {
                        "x": 1,
                        "y": 2,
                        "type": "oar"
                      },
                      {
                        "x": 3,
                        "y": 0,
                        "type": "oar"
                      },
                      {
                        "x": 3,
                        "y": 2,
                        "type": "oar"
                      },
                      {
                        "x": 4,
                        "y": 0,
                        "type": "oar"
                      },
                      {
                        "x": 4,
                        "y": 2,
                        "type": "oar"
                      },
                      {
                        "x": 2,
                        "y": 1,
                        "type": "sail",
                        "opened": false
                      },
                      {
                        "x": 5,
                        "y": 0,
                        "type": "rudder"
                      },
                      {
                        "x": 1,
                        "y": 3,
                        "type": "watch"
                      }
                    ],
                    "shape": {
                      "type": "rectangle",
                      "width": 3,
                      "height": 6,
                      "orientation": 0
                    }
                  },
                  "sailors": [
                    {
                      "x": 0,
                      "y": 0,
                      "id": 0,
                      "name": "Edward Teach"
                    },
                    {
                      "x": 0,
                      "y": 1,
                      "id": 1,
                      "name": "Edward Pouce"
                    },
                    {
                      "x": 0,
                      "y": 2,
                      "id": 2,
                      "name": "Tom Pouce"
                    },
                    {
                      "x": 1,
                      "y": 0,
                      "id": 3,
                      "name": "Jack Teach"
                    },
                    {
                      "x": 1,
                      "y": 1,
                      "id": 4,
                      "name": "Jack Teach"
                    },
                    {
                      "x": 1,
                      "y": 2,
                      "id": 5,
                      "name": "Tom Pouce"
                    },
                    {
                      "x": 1,
                      "y": 2,
                      "id": 6,
                      "name": "Tom Pouce"
                    }
                  ],
                  "shipCount": 1,
                  "wind": {
                    "orientation":0,
                    "strength":110
                  }
                }""";

        round = "{" +
                    "\"ship\": {" +
                        "\"type\": \"ship\"," +
                        "\"life\": 100," +
                        "\"position\": {" +
                            "\"x\": 24," +
                            "\"y\": 2," +
                            "\"orientation\": 7" +
                        "}," +
                        "\"name\": \"Les copaings d'abord!\"," +
                        "\"deck\": {" +
                            "\"width\": 3," +
                            "\"length\": 6" +
                        "}," +
                        "\"entities\": [" +
                            "{" +
                                "\"x\": 1," +
                                "\"y\": 0," +
                                "\"type\": \"oar\"" +
                            "}," +
                            "{" +
                                "\"x\": 1," +
                                "\"y\": 2," +
                                "\"type\": \"oar\"" +
                            "}," +
                            "{" +
                                "\"x\": 3," +
                                "\"y\": 0," +
                                "\"type\": \"oar\"" +
                            "}," +
                            "{" +
                                "\"x\": 3," +
                                "\"y\": 2," +
                                "\"type\": \"oar\"" +
                            "}," +
                            "{" +
                                "\"x\": 4," +
                                "\"y\": 0," +
                                "\"type\": \"oar\"" +
                            "}," +
                            "{" +
                                "\"x\": 4," +
                                "\"y\": 2," +
                                "\"type\": \"oar\"" +
                            "}," +
                            "{" +
                                "\"x\": 2," +
                                "\"y\": 1," +
                                "\"type\": \"sail\"," +
                                "\"opened\": false" +
                            "}," +
                            "{" +
                                "\"x\": 5," +
                                "\"y\": 0," +
                                "\"type\": \"rudder\"" +
                            "}," +
                            "{" +
                                "\"x\": 1," +
                                "\"y\": 3," +
                                "\"type\": \"watch\"" +
                            "}" +
                        "]," +
                        "\"shape\": {" +
                            "\"type\": \"rectangle\"," +
                            "\"width\": 3," +
                            "\"height\": 6," +
                            "\"orientation\": 0" +
                        "}" +
                    "}," +
                    "\"visibleEntities\": [" +
                        "{" +
                            "\"type\": \"stream\"," +
                            "\"position\": {" +
                                "\"x\": 500," +
                                "\"y\": 0," +
                                "\"orientation\": 0" +
                            "}," +
                            "\"shape\": {" +
                                "\"type\": \"rectangle\"," +
                                "\"width\": 50," +
                                "\"height\": 500," +
                                "\"orientation\": 0" +
                            "}," +
                            "\"strength\": 40" +
                        "}" +
                    "]," +
                    "\"wind\": {" +
                        "\"orientation\": 0," +
                        "\"strength\": 110" +
                    "}"+
                "}";
        shipJson = objectMapper.readTree(round);
        List<Oar> oars = List.of(
                new Oar(new Position(1,2,0)),
                new Oar(new Position(3,0,0)),
                new Oar(new Position(3,2,0)),
                new Oar(new Position(4,2,0)),
                new Oar(new Position(1,1,0)));
        entities = new HashMap<>(Map.of(
                Constants.BABORD, new HashSet<>(Set.of(
                        oars.get(0), oars.get(1), oars.get(2))),
                Constants.TRIBORD, new HashSet<>(Set.of(
                        oars.get(3), oars.get(4))),
                Constants.RUDDER, new HashSet<>(Set.of(
                        new Rudder(new Position(2,2,0)))),
                Constants.SAIL, new HashSet<>(Set.of(
                        new Sail(new Position(2,1,0),false))),
                Constants.WATCH, new HashSet<>(Set.of(
                        new Watch(new Position(1,3,0))))
                ));

        Sailor sailor = mock(Sailor.class);
        when(sailor.isBusy()).thenReturn(false);
        when(sailor.getId()).thenReturn(2, 4, 3, 1, 5);
        when(sailor.getPosition()).thenReturn(
                new Position(1, 2, 0),
                new Position(3, 0, 0),
                new Position(3, 2, 0),
                new Position(4, 2, 0),
                new Position(0, 0, 0),
                new Position(0, 0, 0),
                new Position(2, 2, 0));
        List<Action> actions = new ArrayList<>(List.of(
                new OarAction(sailor, oars.get(0)),
                new OarAction(sailor, oars.get(1)),
                new OarAction(sailor, oars.get(2)),
                new OarAction(sailor, oars.get(3)),
                new MovingAction(sailor, oars.get(4)),
                new TurnAction(sailor, (Rudder) entities.get(Constants.RUDDER).toArray()[0], Math.PI/4)));

        crew = mock(Crew.class);
        when(crew.getWantedActions(anyMap(), anyMap(), anyInt())).thenReturn(actions);

        cockpit.ship = new Ship("The wave merger",
                new Position(),
                new Deck(3, 6, entities),
                new Rectangle(3, 6, new Position()),
                crew);
    }

    @Test
    void initGame() {
        List<Checkpoint> checkpoints = new ArrayList<>(List.of(
                new Checkpoint(new Position(1000,0,0), new Rectangle(3,6,new Position())),
                new Checkpoint(new Position(0,0,0), new Rectangle(3,6,new Position()))));

        Map<String, Set<BoatEntity>> oars = new HashMap<>(Map.of(
                Constants.BABORD, new HashSet<>(Set.of(
                        new Oar(new Position(1,0,0)),
                        new Oar(new Position(3,0,0)),
                        new Oar(new Position(4,0,0)))),
                Constants.TRIBORD, new HashSet<>(Set.of(
                        new Oar(new Position(1,2,0)),
                        new Oar(new Position(3,2,0)),
                        new Oar(new Position(4,2,0)))),
                Constants.RUDDER, new HashSet<>(Set.of(
                        new Rudder(new Position(5,0,0)))),
                Constants.SAIL, new HashSet<>(Set.of(
                        new Sail(new Position(2, 1, 0), false))),
                Constants.WATCH, new HashSet<>(Set.of(
                        new Watch(new Position(1,3,0))))
                ));

        List<Sailor> sailors = new ArrayList<>(List.of(
                new Sailor(0,"Edward Teach", 0, 0),
                new Sailor(1,"Edward Pouce", 0, 1),
                new Sailor(2,"Tom Pouce", 0, 2),
                new Sailor(3,"Jack Teach", 1, 0),
                new Sailor(4,"Jack Teach", 1, 1),
                new Sailor(5,"Tom Pouce", 1, 2)));

        Crew exceptedCrew = new Crew(sailors);

        AllCheckpoints exceptedAllCheckpoints = new AllCheckpoints();
        exceptedAllCheckpoints.addAll(checkpoints);

        Ship exceptedShip = new Ship("Les copaings d'abord!",
                new Position(),
                new Deck(3, 6, oars),
                new Rectangle(3, 6, new Position()),
                exceptedCrew);

        cockpit.ship = null;
        cockpit.initGame(initGame);

        cockpit.allSeaEntities.setThereAreNewSeaEntities(false);
        cockpit.objective = new Objective(cockpit.ship);
        cockpit.objective.addCheckpoints(cockpit.allCheckpoints);

        assertEquals(exceptedShip, cockpit.ship);
        assertEquals(exceptedAllCheckpoints, cockpit.allCheckpoints);
        assertNull(cockpit.wind);

    }

    @Test
    void updateShip() throws Exception {
        assertEquals(new Position(), cockpit.ship.getPosition());
        cockpit.updateRound(shipJson);
        assertEquals(new Position(24, 2, 7), cockpit.ship.getPosition());
    }

    @Test
    void resetAttributes() {
        List<Sailor> sailors = new ArrayList<>(List.of(
                new Sailor(0,"Edward Teach", 0, 0),
                new Sailor(1,"Edward Pouce", 0, 1),
                new Sailor(2,"Tom Pouce", 0, 2),
                new Sailor(3,"Jack Teach", 1, 0),
                new Sailor(4,"Jack Teach", 1, 1),
                new Sailor(5,"Tom Pouce", 1, 2),
                new Sailor(6,"Tom Pouce", 1, 2)
                ));

        cockpit.ship = new Ship("The wave merger",
                new Position(),
                new Deck(3, 6, entities),
                new Rectangle(3, 6, new Position()),
                new Crew(sailors));

        Map<String, Set<BoatEntity>> oars = cockpit.ship.getDeck().getEntities();
        List<Sailor> sail = cockpit.ship.getCrew();
        ((Oar) oars.get("babord").toArray()[0]).using();
        ((Oar) oars.get("tribord").toArray()[1]).using();
        sail.get(0).work(); sail.get(1).work();
        cockpit.resetAttributes();
        cockpit.ship.getDeck().getEntities().forEach((name, list) -> {
            if (name.equals("babord") || name.equals("tribord"))
                list.forEach(oar -> assertFalse(oar.isUsed()));
        });
        cockpit.ship.getCrew().forEach(sailor -> assertFalse(sailor.isBusy()));
    }

    @Test
    void nextRound() {
        String expected = "[{\"type\":\"OAR\",\"sailorId\":2},{\"type\":\"OAR\",\"sailorId\":4},{\"type\":\"OAR\",\"sailorId\":3},{\"type\":\"OAR\",\"sailorId\":1},{\"type\":\"MOVING\",\"sailorId\":5,\"xdistance\":1.0,\"ydistance\":1.0},{\"type\":\"TURN\",\"sailorId\":5,\"rotation\":0.7853981633974483}]";
        cockpit.allCheckpoints = new AllCheckpoints();
        cockpit.allCheckpoints.add(new Checkpoint(new Position(0, 2, 0), new Rectangle(3,6,new Position())));

        ArrayList<Reef> reefs = new ArrayList<>(List.of(
                new Reef(new Position(850, 0, 0), new Rectangle(50, 50, new Position(850, 0, 0)))
        ));

        cockpit.allSeaEntities.addAllReef(reefs);
        cockpit.allSeaEntities.setThereAreNewSeaEntities(true);
        cockpit.objective = new Objective(cockpit.ship);
        cockpit.objective.addCheckpoints(cockpit.allCheckpoints);

        String results = cockpit.nextRound(round);
        assertEquals(expected, results);
    }

    @Test
    void nextRoundException() {
        String results = cockpit.nextRound("{"+round+"}");
        assertEquals("[]", results);
    }

    @Test
    void usePathfindingIfNeeded() {
        cockpit.objective = new Objective(cockpit.ship);
        cockpit.objective.addCheckpoints(cockpit.allCheckpoints);

        assertFalse(cockpit.allSeaEntities.areThereNewSeaEntities());
        Checkpoint current = null;
        Checkpoint nextCheckpoint = new Checkpoint(new Position(500,0,0), new Circle(10));
        cockpit.usePathfindingIfNeeded(current, nextCheckpoint);
        assertFalse(cockpit.allSeaEntities.areThereNewSeaEntities());

        cockpit.allSeaEntities.setThereAreNewSeaEntities(false);
        Checkpoint current2 = new Checkpoint(new Position(100,0,0), new Circle(10));
        Checkpoint nextCheckpoint2 = new Checkpoint(new Position(500,0,0), new Circle(10));
        cockpit.usePathfindingIfNeeded(current2, nextCheckpoint2);
        assertFalse(cockpit.allSeaEntities.areThereNewSeaEntities());
        assertTrue(current2.isReached());

        cockpit.allSeaEntities.setThereAreNewSeaEntities(false);
        Checkpoint current3 = new Checkpoint(new Position(100,0,0), new Circle(10));
        Checkpoint nextCheckpoint3 = new Checkpoint(new Position(100,0,0), new Circle(10));
        cockpit.allCheckpoints.setThereIsNewCheckpoint(false);
        cockpit.usePathfindingIfNeeded(current3, nextCheckpoint3);
    }

    @Test
    void pathfinding() {
        cockpit.allCheckpoints = new AllCheckpoints();
        cockpit.allCheckpoints.add(new Checkpoint(new Position(500, 0, 0), new Rectangle(3,6,new Position(500, 0, 0))));

        ArrayList<Reef> reefs = new ArrayList<>(List.of(
                new Reef(new Position(350, 0, 0), new Rectangle(50, 50, new Position(350, 0, 0)))
        ));

        cockpit.allSeaEntities.addAllReef(reefs);
        cockpit.allSeaEntities.setThereAreNewSeaEntities(true);
        cockpit.objective = new Objective(cockpit.ship);
        cockpit.objective.addCheckpoints(cockpit.allCheckpoints);
    }
    
    @Test
    void getObjectiveCheckpoint() {
        cockpit.allCheckpoints = new AllCheckpoints();
        cockpit.allCheckpoints.add(new Checkpoint(new Position(500, 0, 0), new Rectangle(3,6,new Position(500, 0, 0))));

        ArrayList<Reef> reefs = new ArrayList<>(List.of(
                new Reef(new Position(350, 0, 0), new Rectangle(50, 50, new Position(350, 0, 0)))
        ));

        cockpit.allSeaEntities.addAllReef(reefs);
        cockpit.allSeaEntities.setThereAreNewSeaEntities(true);
        cockpit.objective = new Objective(cockpit.ship);
        cockpit.objective.addCheckpoints(cockpit.allCheckpoints);

        cockpit.wind = new Wind();
        Checkpoint expected = new Checkpoint(new Position(400, 75,0), new Circle(50));
        cockpit.getObjectiveCheckpoint();
        assertEquals(expected, cockpit.objective.getCurrent());
    }
}