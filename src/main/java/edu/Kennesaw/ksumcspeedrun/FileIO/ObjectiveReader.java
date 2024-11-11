package edu.Kennesaw.ksumcspeedrun.FileIO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import edu.Kennesaw.ksumcspeedrun.Exceptions.NonLivingEntityException;
import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.entity.EntityType;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.StructureType;

public class ObjectiveReader{

    //Variable to store plugin instance to send to objectives
    Main plugin;

    //Main Processing Method
        //Throws Number Format Exception if the name of an objective is not found
        //Throws invalid argument exceptions if a name of an item entity or location is not found
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

    //Method that checks each line for spacific objectives
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
                        //convert entity string to EntityType
                        try{
                            EntityType target = EntityType.valueOf(parts[1].toUpperCase());
                        } catch (IllegalArgumentException e) { // Handle the case where the entity name does not exist
                            System.out.println("Invalid entity name: " + parts[1]);
                            return null;
                        }
                        //Create a new objective
                        newObjective = new KillObjective(target, plugin)
                    }
                    if (parts.length == 2) { //with Weight
                        //convert entity string to EntityType
                        try{
                            EntityType target = EntityType.valueOf(parts[1].toUpperCase());
                        } catch (IllegalArgumentException e) { // Handle the case where the entity name does not exist
                            System.out.println("Invalid entity name: " + parts[1]);
                            return null;
                        }
                        //Convert weight to int
                        int weight = parts[2]
                        //Create a new objective
                        newObjective = new KillObjective(target, weight, plugin)
                    }
                    if (parts.length == 3) { //with Weight and amount
                        //convert entity string ot EntityType
                        try{
                            EntityType target = EntityType.valueOf(parts[1].toUpperCase());
                        } catch (IllegalArgumentException e) { // Handle the case where the entity name does not exist
                            System.out.println("Invalid entity name: " + parts[1]);
                            return null;
                        }
                        //Convert weight to int
                        int weight = parts[2]
                        //Convert anmount to int
                        int amount = parts[3]
                        //Create a new objective
                        newObjective = new KillObjective(target, weight, amount, plugin)
                    }
                    break;

                case "Obtain":
                    if (parts.length == 1) {//without weight
                        //convert item string to Material
                        try{
                            Material item = Material.valueOf(parts[1].toUpperCase());
                        } catch (IllegalArgumentException e) { // Handle the case where the material name does not exist
                            System.out.println("Invalid entity name: " + entityName);
                            return null;
                        }
                        //Create a new objective
                        newObjective = new ObtainObjective(item, plugin)
                    }
                    if (parts.length == 2) { //with Weight
                        //convert item string to Material
                        Material item = Material.valueOf(parts[1].toUpperCase());
                        //Convert weight to int
                        int weight = parts[2]
                        //Create a new objective
                        newObjective = new ObtainObjective(target, weight, plugin)
                    }
                    if (parts.length == 3) { //with Weight and amount
                        //convert item string to Material
                        Material item = Material.valueOf(parts[1].toUpperCase());
                        //Convert weight to int
                        int weight = parts[2]
                        //Convert anmount to int
                        int amount = parts[3]
                        //Create a new objective
                        newObjective = new ObtainObjective(target, weight, amount, plugin)
                    }
                    break;

                case "Mine":
                    if (parts.length == 1) {//without weight
                        //convert targetBlock string to Material
                        Material targetBlock = Material.valueOf(parts[1].toUpperCase());
                        //Create a new objective
                        newObjective = new MineObjective(item, plugin)
                    }
                    if (parts.length == 2) { //with Weight
                        //convert targetBlock string to Material
                        Material targetBlock = Material.valueOf(parts[1].toUpperCase());
                        //Convert weight to int
                        int weight = parts[2]
                        //Create a new objective
                        newObjective = new MineObjective(target, weight, plugin)
                    }
                    if (parts.length == 3) { //with Weight and amount
                        //convert targetBlock string to Material
                        Material targetBlock = Material.valueOf(parts[1].toUpperCase());
                        //Convert weight to int
                        int weight = parts[2]
                        //Convert anmount to int
                        int amount = parts[3]
                        //Create a new objective
                        newObjective = new MineObjective(target, weight, amount, plugin)
                    break;

                case "Enter":
                    if (parts.length == 1) {//without weight
                        //convert Location type string object
                        Object locationType = getLocationTypeFromString(parts[1].toUpperCase());
                        //Create a new objective
                        newObjective = new EnterObjective(locationType, plugin)
                    }
                    if (parts.length == 2) { //with Weight
                        //convert Location type string object
                        Object locationType = getLocationTypeFromString(parts[1].toUpperCase());
                        //Convert weight to int
                        int weight = parts[2]
                        //Create a new objective
                        newObjective = new EnterObjective(target, weight, plugin)
                    }
                    if (parts.length == 3) { //with Weight and amount
                        //convert Location type string object
                        Object locationType = getLocationTypeFromString(parts[1].toUpperCase());
                        //Convert weight to int
                        int weight = parts[2]
                        //Convert anmount to int
                        int amount = parts[3]
                        //Create a new objective
                        newObjective = new EnterObjective(target, weight, amount, plugin)
                    }
                    break;
                case "#": //This argument is to add comments to larger objective documents
                    break;
                default:
                    System.out.println("Unknown objective: " + command);
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error parsing line: " + line + " - Invalid number format.");
        }

    //Method to get location type from string
    public static Object getLocationTypeFromString(String locationType) {
        locationType = locationType.toLowerCase();

        // Check for Biome type
        Biome biome = getBiomeFromString(locationType);
        if (biome != null) {
            return biome;
        }

        // Check for Structure type
        StructureType structure = getStructureFromString(locationType);
        if (structure != null) {
            return structure;
        }

        // Check for Portal type
        PortalType portal = getPortalFromString(locationType);
        if (portal != null) {
            return portal;
        }

        System.out.println("Invalid location type: " + locationType);
        return null;
    }

    //method to change string to Biome type
    private static Biome getBiomeFromString(String biomeName) {
        try {
            return Biome.valueOf(biomeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Not a valid Biome
        }
    }

    //method to change string to Structure type
    private static StructureType getStructureFromString(String structureName) {
        try {
            return StructureType.valueOf(structureName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Not a valid StructureType
        }
    }
    //method to change string to Portal type
    private static PortalType getPortalFromString(String portalName) {
        switch (portalName) {
            case "nether":
                return PortalType.NETHER;
            case "end":
                return PortalType.END;
            default:
                return null; // Not a valid PortalType
        }
    }

    // Example enum for PortalType
    public enum PortalType {
        NETHER,
        END
    }
}