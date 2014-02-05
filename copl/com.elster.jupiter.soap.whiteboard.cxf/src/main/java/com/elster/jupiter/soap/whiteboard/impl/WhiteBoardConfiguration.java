package com.elster.jupiter.soap.whiteboard.impl;

import java.util.Map;

class WhiteBoardConfiguration {
	
	private WhiteBoardConfiguration() {
	}
	
	private WhiteBoardConfiguration(Map<String,Object> properties) {			
	}		
	
	static WhiteBoardConfiguration of(Map<String,Object> props) {
		return props == null ? new WhiteBoardConfiguration() : new WhiteBoardConfiguration(props);
	}


}
