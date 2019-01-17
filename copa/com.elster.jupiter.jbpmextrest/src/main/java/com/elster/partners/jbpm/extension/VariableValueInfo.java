package com.elster.partners.jbpm.extension;

public class VariableValueInfo {


        public String variableValue;
        public String variableId;

        public VariableValueInfo(Object[] obj){
            variableValue = obj[0] == null ? "" :(String) obj[0];
            variableId  = obj[1] == null ? "" :(String) obj[1];
        }

        public VariableValueInfo(){

        }
}
