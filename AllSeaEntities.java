package fr.unice.polytech.si3.qgl.merj.entities.seaEntities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AllSeaEntities extends HashSet<SeaEntity> {

    private boolean thereAreNewSeaEntities = false;
    public static int numberOfVisibleEntities;

    public AllSeaEntities() {
        super();
    }

    public boolean areThereNewSeaEntities() {
        return thereAreNewSeaEntities;
    }

    public void setThereAreNewSeaEntities(boolean value) {
        this.thereAreNewSeaEntities = value;
    }

    public Set<Reef> getReefs() {
        return parallelStream().filter(Reef.class::isInstance)
                .map(Reef.class::cast)
                .collect(Collectors.toSet());
    }

    public Set<Stream> getStreams() {
        return parallelStream().filter(Stream.class::isInstance)
                .map(Stream.class::cast)
                .collect(Collectors.toSet());
    }

    public void addAllReef(List<Reef> reefs) {
        addAll(reefs);
    }

}
