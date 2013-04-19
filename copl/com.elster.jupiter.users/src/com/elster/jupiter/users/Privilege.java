package com.elster.jupiter.users;

public final class Privilege {
	final String componentName;
	final int id;
	
	public Privilege(String componentName , int id) {
		this.componentName = componentName;
		this.id = id;
	}
	
	
	public String getComponentName() {
		return componentName;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return "Privilege " + id + " in component " + componentName;
	}
	
	@Override
	public boolean equals(Object other) {
		try {
			Privilege o = (Privilege) other;
			return this.componentName.equals(o.componentName) && this.id == o.id;
		} catch (ClassCastException ex) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return componentName.hashCode() ^ Integer.valueOf(id).hashCode();
	}
}
