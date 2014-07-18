package com.energyict.mdc.dashboard;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Application;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (10:32)
 */
@Component(name = "com.energyict.dsb.rest", service = Application.class, immediate = true, property = {"alias=/dsb"})
public class DashboardApplication extends Application {

    public static final String COMPONENT_NAME = "DSB";

    private volatile ManaBeFa
}