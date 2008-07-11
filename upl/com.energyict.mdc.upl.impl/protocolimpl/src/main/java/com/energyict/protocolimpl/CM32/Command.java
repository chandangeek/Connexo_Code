package com.energyict.protocolimpl.CM32;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Command {
    
    private List parameters = new ArrayList();
    private String command;
    
    /** Creates a new instance of Command */
    public Command(String command) {
        this.setCommand(command);
    }   
    
    public void validate() throws IOException {

    }
    
    public void addParameter(String parameter) {
    	parameters.add(parameter);
    }
    
    public String getParameter(int i) {
    	return (String) parameters.get(i);
    }

    public List getParameters() {
        return parameters;
    }

    public void setParameters(List parameters) {
        this.parameters = parameters;
    }

    public String getCommand() {
        return command;
    }

    private void setCommand(String command) {
        this.command = command;
    }

    

}
