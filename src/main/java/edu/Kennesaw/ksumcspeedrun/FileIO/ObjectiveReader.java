package edu.Kennesaw.ksumcspeedrun.FileIO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import edu.Kennesaw.ksumcspeedrun.Exceptions.NonLivingEntityException;
import edu.Kennesaw.ksumcspeedrun.Main;
import edu.Kennesaw.ksumcspeedrun.Objects.Objective.*;
import edu.Kennesaw.ksumcspeedrun.Structures.SRStructure;
import edu.Kennesaw.ksumcspeedrun.Exceptions.InvalidTargetLocationException;

import org.bukkit.entity.EntityType;
import org.bukkit.Material;
import org.bukkit.block.Biome;

public class ObjectiveReader{

    //Variable to store plugin instance to send to objectives
    Main plugin;

    //default file path to add the file name too
    String filePath = "/plugins/KSU-MC-Speedrun/";

    //Main Processing Method
        //Throws Number Format Exception if the name of an objective is not found
        //Throws invalid argument exceptions if a name of an item entity or location is not found
	public void loadObjectivesFromFile(String fileName, Main plugin) { //adds filename to default filePath to complete the path
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath + fileName))) {
            this.plugin = plugin;
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
        //in the intrest of simplisity, we seperate by " "
        String[] parts = line.split(" ");

        // if there is nothing in the line or if there is a comment line starting wiht "//" ignore that line
        if (parts.length == 0) {
            return;
        }

        //seperate the first string. Thiss wil be the comand string
        String command = parts[0].trim();

        // coments can be added to txt file by using "//" as the command
        if (command == "//"){
            return;
        }

        //keeping a hardcoded default of 1 for ammount and weight to avoid bugs
        Objective newObjective = null;
        int weight = 1;
        int amount = 1;

         //set up Weight
        if (parts.length == 2 || parts.length == 3) {
            //Convert weight to int
            weight = Integer.parseInt(parts[2]);
        }

        //set up amount
        if (parts.length == 3) {
            //Convert anmount to int
            amount = Integer.parseInt(parts[3]);
        }


        try {
            switch (command) {
                case "Kill":
                    //convert entity string to EntityType
                    EntityType target;
                    try {
                        target = EntityType.valueOf(parts[1].toUpperCase());
                    } catch (IllegalArgumentException e) { // Handle the case where the entity name does not exist
                        System.out.println("Invalid entity name: " + parts[1]);
                        break;
                    }

                    try {
                        //without weight
                        if (parts.length == 1) {
                            //Create a new objective
                                newObjective = new KillObjective(target, plugin);
                        }

                        //with Weight
                        if (parts.length == 2) {
                            //Create a new objective
                            try {
                            newObjective = new KillObjective(target, weight, plugin);
                            } catch (NonLivingEntityException e) {
                                System.out.println("Entity must be living: " + parts[1]);
                                break;
                            }
                        }

                        //with Weight and amount
                        if (parts.length == 3) {
                            try {
                            newObjective = new KillObjective(target, weight, amount, plugin);
                            } catch (NonLivingEntityException e) {
                                System.out.println("Entity must be living: " + parts[1]);
                                break;
                            }
                        }
                    } catch (NonLivingEntityException e) {
                        System.out.println("Entity must be living: " + parts[1]);
                        break;
                    }

                    break;

                case "Obtain":
                    //convert item string to Material
                    Material item;
                    try {
                        item = Material.valueOf(parts[1].toUpperCase());
                    } catch (IllegalArgumentException e) { // Handle the case where the material name does not exist
                        System.out.println("Invalid item name: " + parts[1]);
                        break;
                    }

                    //without weight
                    if (parts.length == 1) {
                        //Create a new objective
                        newObjective = new ObtainObjective(item, plugin);
                    }

                    //with Weight
                    if (parts.length == 2) {
                        //Create a new objective
                        newObjective = new ObtainObjective(item, weight, plugin);
                    }

                    //with Weight and amount
                    if (parts.length == 3) {
                        //Create a new objective
                        newObjective = new ObtainObjective(item, weight, amount, plugin);
                    }
                    break;

                case "Mine":
                //convert targetBlock string to Material
                    Material targetBlock = null;
                    try {
                        targetBlock = Material.valueOf(parts[1].toUpperCase());
                    } catch (IllegalArgumentException e) { // Handle the case where the material name does not exist
                        System.out.println("Invalid block name: " + parts[1]);
                        break;
                    }

                    //without weight
                    if (parts.length == 1) {
                        //Create a new objective
                        newObjective = new MineObjective(targetBlock, plugin);
                    }

                    //with Weight
                    if (parts.length == 2) {
                        //Create a new objective
                        newObjective = new MineObjective(targetBlock, weight, plugin);
                    }

                    //with Weight and amount
                    if (parts.length == 3) {
                        //Create a new objective
                        newObjective = new MineObjective(targetBlock, weight, amount, plugin);
                    }

                    break;

                case "Enter":
                    //convert Location type string object
                    Object locationType = getLocationTypeFromString(parts[1].toUpperCase());

                    try{
                        //without weight
                        if (parts.length == 1) {
                            //Create a new objective
                            newObjective = new EnterObjective(locationType, plugin);
                        }

                        //with Weight
                        if (parts.length == 2) {
                            //Create a new objective
                            newObjective = new EnterObjective(locationType, weight, plugin);
                        }

                        //with Weight and amount
                        if (parts.length == 3) {
                            //Create a new objective
                            newObjective = new EnterObjective(locationType, weight, amount, plugin);
                        }
                    } catch (InvalidTargetLocationException e) {
                        System.out.println("Invalid target location: " + parts[1]);
                    }

                    break;

                default:
                    System.out.println("Unknown objective: " + command);
                    break;
            }
            
            //Finnaly, add the objective to the speedrun
            plugin.getSpeedrun().addObjective(newObjective);

        } catch (NumberFormatException e) {
            System.out.println("Error parsing line: " + line + " - Invalid number format.");
        }
    }

    //Method to get location type from string
    public Object getLocationTypeFromString(String locationType) {
        locationType = locationType.toLowerCase();

        // Check for Biome type
        Biome biome = getBiomeFromString(locationType);
        if (biome != null) {
            return biome;
        }

        // Check for Structure type
        SRStructure structure = new SRStructure(plugin, locationType);
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