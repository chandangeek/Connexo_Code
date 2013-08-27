package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.SubscriberSpec;
import com.google.common.base.Optional;
import oracle.jdbc.aq.AQMessage;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.sql.SQLException;

/**
 * MSG console commands
 */
@Component(name = "com.elster.jupiter.messaging.commands", service = ConsoleCommandsImpl.class, property = { "name=" + Bus.COMPONENTNAME + "2" , "osgi.command.scope=jupiter" , "osgi.command.function=aqcreatetable", "osgi.command.function=aqdroptable", "osgi.command.function=drain" } )
public class ConsoleCommandsImpl {

	@Activate
	public void activate(BundleContext context) {
	}

	@Deactivate
	public void deactivate() {
	}
	
    public void aqcreatetable(String in) {
        System.out.println("About to create Queue table " + in);
        try {
            new QueueTableSpecImpl(in, "RAW", false).activate();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public void aqdroptable(String in) {
        try {
            new QueueTableSpecImpl(in, "RAW", false).deactivate();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public void drain(String subscriberName, String destinationName) {
        Optional<SubscriberSpec> spec = Bus.getOrmClient().getConsumerSpecFactory().get(destinationName, subscriberName);
        try {
            AQMessage message = ((SubscriberSpecImpl) spec.get()).receiveNow();
            while (message != null) {
                System.out.println(new String(message.getPayload()));
                message = ((SubscriberSpecImpl) spec.get()).receiveNow();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
