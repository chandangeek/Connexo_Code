package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.pubsub.EventHandler;
import com.energyict.mdc.device.config.ComTaskEnablement;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Handles events that are being sent when connection related changes
 * have been applied to a {@link ComTaskEnablement}.<br>
 * The changes that are being monitored are listed and described
 * <a href="http://confluence.eict.vpdc/display/JUPMDC/ComTaskEnablement+events">here</a>.
 * Todo (JP-1125): complete implementation as part of the port of ComTaskExecution to the new ORM framework
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-24 (11:51)
 */
@Component(name="com.energyict.mdc.device.data.update.comtaskenablement.connection.eventhandler", service = TopicHandler.class, immediate = true)
public class ComTaskEnablementConnectionEventHandler extends EventHandler<LocalEvent> {

    protected ComTaskEnablementConnectionEventHandler() {
        super(LocalEvent.class);
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        ActualEventHandler.forTopic(event.getType().getTopic()).onEvent(event, eventDetails);
    }

    private enum ActualEventHandler {
        SWITCH_ON_DEFAULT {
            @Override
            void onEvent(LocalEvent event, Object... eventDetails) {
                /* Find all ComTaskExecutions that use the ComTaskEnablement
                 * and make sure they now all use the default. */
            }
        },

        SWITCH_OFF_DEFAULT {
            @Override
            void onEvent(LocalEvent event, Object... eventDetails) {
                /* Find all ComTaskExecutions that use the ComTaskEnablement
                 * and make sure they now all use the default. */
            }
        },

        SWITCH_FROM_DEFAULT_TO_TASK {
            @Override
            void onEvent(LocalEvent event, Object... eventDetails) {
                // Code from 9.1
//                this.getComTaskExecutionFactory().
//                        switchFromDefaultConnectionTaskToPreferredConnectionTask(
//                                this.getComTask(),
//                                this.getDeviceCommunicationConfiguration().getDeviceConfiguration(),
//                                shadow.getPartialConnectionTaskId());
            }
        },

        SWITCH_FROM_TASK_TO_DEFAULT {
            @Override
            void onEvent(LocalEvent event, Object... eventDetails) {
                // Code from 9.1
//                this.getComTaskExecutionFactory().
//                        switchFromPreferredConnectionTaskToDefault(
//                                this.getComTask(),
//                                this.getDeviceCommunicationConfiguration().getDeviceConfiguration(),
//                                previousPartialConnectionTaskId);
            }
        },

        SWITCH_BETWEEN_TASKS {
            @Override
            void onEvent(LocalEvent event, Object... eventDetails) {
                // Code from 9.1
//                this.getComTaskExecutionFactory().
//                        preferredConnectionTaskChanged(
//                                this.getComTask(),
//                                this.getDeviceCommunicationConfiguration().getDeviceConfiguration(),
//                                previousPartialConnectionTaskId,
//                                shadow.getPartialConnectionTaskId());

            }
        },

        USE_TASK {
            @Override
            void onEvent(LocalEvent event, Object... eventDetails) {
                /* Find all ComTaskExecutions that use the ComTaskEnablement
                 * and make sure they now all use the ConnectionTask
                 * that relates to the PartialConnectionTask. */
            }
        },

        REMOVE_TASK {
            @Override
            void onEvent(LocalEvent event, Object... eventDetails) {
                /* Find all ComTaskExecutions that use the ComTaskEnablement
                 * and make sure they no longer use the ConnectionTask
                 * that relates to the PartialConnectionTask. */
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
            Set<ActualEventHandler> candidates = EnumSet.range(SWITCH_ON_DEFAULT, REMOVE_TASK);
            for (ActualEventHandler actualEventHandler : candidates) {
                if (actualEventHandler.topic().equals(topic)) {
                    return actualEventHandler;
                }
            }
            return DEV_NULL;
        }

    }
}