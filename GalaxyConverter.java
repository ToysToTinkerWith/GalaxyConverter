import java.io.*;
import java.lang.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class GalaxyConverter {

    // Main method parses through input file creating command objects to work with.
    // Takes each command and either records notes in a HashMap or prints the answer to a query.
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Please provide an input file.");
            return;
        }

        ArrayList<Command> commands = new ArrayList<Command>();
        
        try {

            FileInputStream inputFile = new FileInputStream(args[0]);
            Scanner scanFile = new Scanner(inputFile);

            while (scanFile.hasNextLine()) {

                String commandLine = scanFile.nextLine();
                Command command = parseCommand(commandLine);
                commands.add(command);
                
            }

            scanFile.close();

            /*
            For printing out all commands:
            for (Command command : commands) {
                System.out.println(command.commandType);
                for (String key : command.keys) {
                    System.out.print(key);
                }
                System.out.println();
                for (String value : command.values) {
                    System.out.print(value);
                }
                System.out.println();
                System.out.println(command.resource);
                System.out.println();
            }
            */

            // Values for roman numerals.
            HashMap<String, Integer> romanValues = new HashMap<String, Integer>();
            romanValues.put("I", 1);
            romanValues.put("V", 5);
            romanValues.put("X", 10);
            romanValues.put("L", 50);
            romanValues.put("C", 100);
            romanValues.put("D", 500);
            romanValues.put("M", 1000);

            // Values for decimal to roman conversion.
            String[] ones = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
            String[] tens = {"X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
            String[] hundreds = {"C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
            String[] thousands = {"M", "MM", "MMM"};


            HashMap<String, Float> keyValues = new HashMap<String, Float>();

            for (Command command : commands) {

                if (command.commandType.equals("query")) {

                    // Assumption: Queries with 2 keys are asking how much.
                    if (command.keys.size() == 2) {
                        
                        float combineValue = evaluateKeys(command.values, keyValues, romanValues, ones, tens, hundreds, thousands);

                        if (combineValue == -1.0) {
                            System.out.println("Incorrect Roman Formatting: Discarding Query...");
                        }
                        else {
                            String valueString = "";

                            for (String value : command.values) {
                                valueString += value + " ";
                                
                            }
                            System.out.println(valueString + "is " + (int) combineValue);
                        }
                        
                    }

                    //Assumption: Queries with 2 keys are asking how many Credits.
                    else if (command.keys.size() == 3) {
                        
                        float combineValue = evaluateKeys(command.values, keyValues, romanValues, ones, tens, hundreds, thousands);

                        if (combineValue == -1.0) {
                            System.out.println("Incorrect Roman Formatting: Discarding Query...");
                        }
                        else {

                            try {
                                float resourceValue = keyValues.get(command.resource);

                                String valueString = "";

                                for (String value : command.values) {
                                    valueString += value + " ";
                                }
                                System.out.println(valueString + command.resource + " is " + (int) (combineValue * resourceValue) + " Credits");
                            }
                            catch (Exception e) {
                                System.out.println("Could not evaluate.");
                            }
                
                            
                        }
                        

                    }
                }
                
                else if (command.commandType.equals("note")) {

                    //Assumption: These notes set the roman values for the galactic currency.
                    if (command.keys.size() == 1) { 
                        float romanValue = romanValues.get(command.values.get(0));
                        keyValues.put(command.keys.get(0), romanValue);
                    }

                    //Assumption: These notes take in galactic currency and another resource as a multiploer.
                    else {

                        float combineKeyValue = evaluateKeys(command.keys, keyValues, romanValues, ones, tens, hundreds, thousands);

                        if (combineKeyValue == -1.0) {
                            System.out.println("Incorrect Roman Formatting: Discarding Note...");
                        }
                        else {
                            float resourceValue = Integer.parseInt(command.values.get(0)) / combineKeyValue;
                            keyValues.put(command.resource, resourceValue);
                        }
                        
                    }


                }

                else {
                    System.out.println("I have no idea what you are talking about.");
                }
            }

        }

        catch(IOException e) {
            e.printStackTrace();  
        }  
    }

    // parseCommand takes a line of the input file and returns a command object thats easier to work with.
    public static Command parseCommand(String command) {

        String commandType;
        ArrayList<String> keys = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        String resource = "none";

        try {
            //Assumption: Queries end with a '?'
            if (command.charAt(command.length() - 1) == '?') {
                commandType = "query";

                String[] commandStrings = command.split(" ", 0);

                int i = 0;

                while (!commandStrings[i].equals("is")) {
                    keys.add(commandStrings[i]);
                    i++;
                }
                i++;

                //Assumption: Queries must start with a specific question.
                //Assumption: These queries don't have a resource in them.
                if (keys.get(0).equals("how") && keys.get(1).equals("much")) {

                    while (i < commandStrings.length - 1) {
                        values.add(commandStrings[i]);
                        i++;
                    }
                }

                //Assumption: These queries have a resource at the end before the ?
                else if (keys.get(0).equals("how") && keys.get(1).equals("many") && keys.get(2).equals("Credits")) {

                    while (i < commandStrings.length - 2) {
                        values.add(commandStrings[i]);
                        i++;
                    }
                    resource = commandStrings[i];
                }


            }

            else {
                commandType= "note";

                String[] commandStrings = command.split(" ", 0);

                int i = 0;

                while (!commandStrings[i].equals("is")) {
                    keys.add(commandStrings[i]);
                    i++;
                }
                
                //Assumption: These notes have a resource associated.
                if (keys.size() > 1) {
                    resource = commandStrings[i-1];
                    keys.remove(i-1);

                }

                values.add(commandStrings[i+1]);
               
            }


            Command parsedCommand = new Command(commandType, keys, values, resource);

            return parsedCommand;
        }

        catch (Exception e) {
            
            return new Command("error", keys, values, resource);
        }

        
    }

    // evaluateKeys evaluates the galactic currency based on its roman numeral value.
    // Also checks if the roman numeral format is valid.
    public static float evaluateKeys(ArrayList<String> keys, HashMap<String, Float> keyValues, HashMap<String, Integer> romanValues, String[] ones, String[] tens, String[] hundreds, String[] thousands) {

        float romanTally = 0;
        StringBuilder romanNumerals = new StringBuilder();

        for (int i = 0; i < keys.size(); i++) {

            String key = keys.get(i);
            float keyValue = keyValues.get(key);

            for (Entry<String, Integer> entry: romanValues.entrySet()) {
                if (entry.getValue() == keyValue) {
                    romanNumerals.append(entry.getKey());
                }
            }

            // Last element doesn't have a next key.
            if (i < keys.size() - 1) {
                String nextKey = keys.get(i+1);
                float nextKeyValue = keyValues.get(nextKey);

                if (keyValue >= nextKeyValue) {
                    romanTally += keyValue;
                }

                else {
                    romanTally += nextKeyValue - keyValue;
                    for (Entry<String, Integer> entry: romanValues.entrySet()) {
                        if (entry.getValue() == nextKeyValue) {
                            romanNumerals.append(entry.getKey());
                        }
                    }
                    i++;
                }
            }
            
            else {
                romanTally += keyValue;
            }

        }
        
        // Seeing if the inputed roman numeral format is correct
        String valueStr = String.valueOf(romanTally);
        int pointIndex = valueStr.indexOf(".");

        StringBuilder correctRomans = new StringBuilder();

        for (int j = 0; j < pointIndex; j++) {

            int value = Integer.parseInt(valueStr.substring(j, j+1));

            if (value != 0) {
                if (pointIndex - j == 1) {
                    correctRomans.append(ones[value-1]);
                }
                else if (pointIndex - j == 2) {
                    correctRomans.append(tens[value-1]);
                }
                else if (pointIndex - j == 3) {
                    correctRomans.append(hundreds[value-1]);
                }
                else if (pointIndex - j == 4) {
                    correctRomans.append(thousands[value-1]);
                }
            }
            
        }

        if (romanNumerals.compareTo(correctRomans) == 0) {
            return romanTally;
        }

        else {
            return (float) -1.0;
        }

    }



}


