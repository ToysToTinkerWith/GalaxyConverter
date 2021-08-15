import java.util.ArrayList;

class Command {
    String commandType;
    ArrayList<String> keys;
    ArrayList<String> values;
    String resource;


    public Command(String commandType, ArrayList<String> keys, ArrayList<String> values, String resource) {
        this.commandType = commandType;
        this.keys = keys;
        this.values = values;
        this.resource = resource;
    }

}