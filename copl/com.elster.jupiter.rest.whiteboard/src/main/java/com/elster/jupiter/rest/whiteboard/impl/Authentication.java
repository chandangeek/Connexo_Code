package com.elster.jupiter.rest.whiteboard.impl;

import java.io.IOException;

import javax.servlet.http.*;

public interface Authentication {
	boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
