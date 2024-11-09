package edu.Kennesaw.ksumcspeedrun.FileIO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ObjectiveReader{

    //Variable to store plugin instance to send to objectives
    Main plugin;

	public void loadObjectivesFromFile(String filePath, Main plugin) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            this.plugin = plugin
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processLine(String line) {
        String[] parts = line.split(" ");
        if (parts.length == 0) {
            return;
        }

        String command = parts[0].trim();
        Objective newObjective = new Objective() {};

        try {
            switch (command) {
                case "Kill":
                    if (parts.length == 1) {//without weight
                        //convert entity string ot EntityType
                        EntityType target = EntityType.valueOf(parts[1].toUpperCase());
                        //Create a new objective
                        newObjective = new KillObjective(target, plugin)
                    }
                    if (parts.length == 2) { //with Weight
                        //convert entity string ot EntityType
                        EntityType target = EntityType.valueOf(parts[1].toUpperCase());
                        //Convert weight to int
                        int weight = parts[2]
                        //Create a new objective
                        newObjective = new KillObjective(target, weight, plugin)
                    }
                    break;
                case "Obtain":
                    }
                    break;
                case "Mine":
                    break;
                case "Enter":
                    }
                    break;
                default:
                    System.out.println("Unknown objective: " + command);
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error parsing line: " + line + " - Invalid number format.");
        }
    }


}