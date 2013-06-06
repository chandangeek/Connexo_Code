package com.elster.jupiter.users;

public interface Group {
	long getId();
	String getName();	
	boolean hasPrivilege(String privilegeName);
	void grant(String privilegeName);	
}
