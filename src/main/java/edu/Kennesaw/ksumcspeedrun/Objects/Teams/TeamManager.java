package edu.Kennesaw.ksumcspeedrun.Objects.Teams;

import java.util.ArrayList;
import java.util.List;

public class TeamManager {

    private List<Team> teams;

    public TeamManager() {
        teams = new ArrayList<>();
    }

    public void addTeam(Team team) {
        teams.add(team);
    }

    public void removeTeam(Team team) {
        teams.remove(team);
    }

    public List<Team> getTeams() {
        return teams;
    }

    public Team getTeam(String team) {
        for (Team t : teams) {
            if (t.getName().equals(team)) {
                return t;
            }
        }
        return null;
    }

}
