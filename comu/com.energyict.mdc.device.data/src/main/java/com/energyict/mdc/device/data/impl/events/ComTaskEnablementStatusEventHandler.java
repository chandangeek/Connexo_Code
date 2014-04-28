package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.pubsub.EventHandler;
import com.energyict.mdc.device.config.ComTaskEnablement;
import java.util.EnumSet;
import java.util.Set;
import org.osgi.service.component.annotations.Component;

/**
 * Handles events that are being sent when a {@link ComTaskEnablement}
 * was suspended or resumed.<br>
 * The changes that are being monitored are listed and described
 * <a href="http://confluence.eict.vpdc/display/JUPMDC/ComTaskEnablement+events">here</a>.
 * @see ComTaskEnablement#suspend()
 * @see ComTaskEnablement#resume()
 * Todo (JP-1125): complete implementation as part of the port of ComTaskExecution to the new ORM framework
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (11:51)
 */
@Component(name="com.energyict.mdc.device.data.update.comtaskenablement.status.eventhandler", service = TopicHandler.class, immediate = true)
public class ComTaskEnablementStatusEventHandler extends EventHandler<LocalEvent> {

    protected ComTaskEnablementStatusEventHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        ActualEventHandler.forTopic(event.getType().getTopic()).onEvent(event, eventDetails);
    }

    private enum ActualEventHandler {
        SUSPEND {
            @Override
            void onEvent(LocalEvent event, Object... eventDetails) {
                // Code from 9.1
//                this.getComTaskExecutionFactory().
//                        suspendAll(
//                                this.getComTask(),
//                                this.getDeviceCommunicationConfiguration().getDeviceConfiguration());
            }
        },

        RESUME {
            @Override
            void onEvent(LocalEvent event, Object... eventDetails) {
                // Code from 9.1
//                this.getComTaskExecutionFactory().
//                        resumeAll(
//                                this.getComTask(),
//                                this.getDeviceCommunicationConfiguration().getDeviceConfiguration());
            }
        },

        DEV_NULL {
            @Override
            void onEvent(LocalEvent event, Object... eventDetails) {
                // Designed to ignore everything
            }
        };

        abstract void onEvent(LocalEvent event, Object... eventDetails);

        private String topic () {
            return "com/energyict/mdc/device/config/comtaskenablement/" + this.name();
        }

        private static ActualEventHandler forTopic (String topic) {
            Set<ActualEventHandler> candidates = EnumSet.range(SUSPEND, RESUME);
            for (ActualEventHandler actualEventHandler : candidates) {
                if (actualEventHandler.topic().equals(topic)) {
                    return actualEventHandler;
                }
            }
            return DEV_NULL;
        }

    }
}