package com.energyict.protocolimpl.CM32;

import java.io.IOException;

public class Command {
    
    private String data;
    private String command;
    
    /** Creates a new instance of Command */
    public Command(String command) {
        this.setCommand(command);
    }   
    
    public void validate() throws IOException {

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    private void setCommand(String command) {
        this.command = command;
    }

    

}
