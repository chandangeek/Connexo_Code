package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.security.thread.RunAs;
import com.elster.jupiter.transaction.VoidTransaction;
import com.google.common.base.Optional;
import oracle.jdbc.aq.AQMessage;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.io.PrintStream;
import java.security.Principal;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MSG console commands
 */
@Component(name = "com.elster.jupiter.messaging.commands", service = ConsoleCommandsImpl.class, property = { "name=" + Bus.COMPONENTNAME + "2" , "osgi.command.scope=jupiter" , "osgi.command.function=aqcreatetable", "osgi.command.function=aqdroptable", "osgi.command.function=drain", "osgi.command.function=subscribe" } )
public class ConsoleCommandsImpl {

    private static final Logger LOGGER = Logger.getLogger(ConsoleCommandsImpl.class.getName());

    private PrintStream output = System.out;


	@Activate
	public void activate(BundleContext context) {
	}

	@Deactivate
	public void deactivate() {
	}
	
    public void aqcreatetable(String in) {
        output.println("About to create Queue table " + in);
        try {
            new QueueTableSpecImpl(in, "RAW", false).activate();
        } catch (RuntimeException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void aqdroptable(String in) {
        try {
            new QueueTableSpecImpl(in, "RAW", false).deactivate();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    public void drain(String subscriberName, String destinationName) {
        Optional<SubscriberSpec> spec = Bus.getOrmClient().getConsumerSpecFactory().get(destinationName, subscriberName);
        try {
            AQMessage message = ((SubscriberSpecImpl) spec.get()).receiveNow();
            while (message != null) {
                output.println(new String(message.getPayload()));
                message = ((SubscriberSpecImpl) spec.get()).receiveNow();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void subscribe(final String subscriberName, final String destinationName) {
        Bus.getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                new RunAs(Bus.getThreadPrincipalService(), new Principal() {
                    @Override
                    public String getName() {
                        return "Command line";
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        Optional<DestinationSpec> destination = Bus.getOrmClient().getDestinationSpecFactory().get(destinationName);
                        if (!destination.isPresent()) {
                            System.err.println("No such destination " + destinationName);
                        }
                        destination.get().subscribe(subscriberName);
                    }
                }).run();
            }
        });
    }
}
