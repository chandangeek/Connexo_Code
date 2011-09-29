package com.elster.utils.lis200.events;

import com.energyict.protocol.MeterEvent;

import java.util.Date;

/**
 * (very) general EventInterpreter class for elster meters. <br>
 * Derived classes should first try to find a own event, and if not in own list,
 * call EventInterpreter from super class<br>
 *
 * <b>General Description:</b><br>
 * An event in a log book is interpreted and a corresponding MeterEvent object
 * is created<br>
 * <br>
 * <br>
 *
 * @author gh
 * @since 21-Apr-2010
 *
 */

public class EventInterpreter {

	enum EventClass {
		ERROR, WARNING, HINT
	}

	enum EventType {
		RAISED, GONE
	}

	private EventClass[] classOfEvent = { EventClass.ERROR, EventClass.ERROR,
			EventClass.WARNING, EventClass.WARNING, EventClass.WARNING,
			EventClass.WARNING, EventClass.WARNING, EventClass.WARNING,
			EventClass.HINT, EventClass.HINT, EventClass.HINT, EventClass.HINT,
			EventClass.HINT, EventClass.HINT, EventClass.HINT, EventClass.HINT };

	public EventInterpreter() {
	}

	public MeterEvent interpretEvent(Date timeStamp, int event) {
		MeterEvent me = null;
		String msg;
		switch (getEventCategory(event)) {
		case 0:
			me = getGeneralEvent(timeStamp, event);
			if (me == null) {
				msg = String.format("%s: Signal %d %s in instance %d.",
						getEventClassText(getEventClass(event)),
						getEventSignal(event),
						getEventTypeText(getEventType(event)),
						getEventInstance(event));
				me = new MeterEvent(timeStamp, MeterEvent.OTHER, event, msg);
			}
			break;
		case 1:
			me = getSystemEvent(timeStamp, event);
			if (me == null) {
				msg = String.format("%s: Signal %d %s in register %s.",
						getEventClassText(getEventClass(event)),
						getEventSignal(event),
						getEventTypeText(getEventType(event)),
						getStatusRegisterName(event));
				me = new MeterEvent(timeStamp, MeterEvent.OTHER, event, msg);
			}
			break;
		case 4:
			msg = String.format("%s: Signal 1-%d %s in instance %d.",
					getEventClassText(getEventClass(event)),
					getEventSignal(event),
					getEventTypeText(getEventType(event)),
					getEventInstance(event));
			me = new MeterEvent(timeStamp, MeterEvent.OTHER, event, msg);
			break;
		case 5:
			msg = String.format("%s: Signal 1-%d %s in register %s.",
					getEventClassText(getEventClass(event)),
					getEventSignal(event),
					getEventTypeText(getEventType(event)),
					getStatusRegisterName(event));
			me = new MeterEvent(timeStamp, MeterEvent.OTHER, event, msg);
			break;
		case 8:
			me = getGeneralArchiveEvent(timeStamp, event);
			if (me == null) {
				msg = String.format("Event 0x%s: signal=%d  instance=%d",
						Integer.toHexString(event), getEventSignal(event) - 1,
						getEventInstance(event));
				me = new MeterEvent(timeStamp, MeterEvent.OTHER, event, msg);
			}
			break;
		default:
			msg = String.format("Unknown event 0x%s", Integer
					.toHexString(event));
			me = new MeterEvent(timeStamp, MeterEvent.OTHER, event, msg);
			break;
		}
		return me;
	}

	private MeterEvent getGeneralArchiveEvent(Date timeStamp, int event) {
		String msg;
		switch (getEventSignal(event) - 1) {
		case 0:
			msg = String.format("Event counter %d decremented",
					getEventInstance(event));
			break;
		case 1:
			msg = String.format("Event counter %d incremented",
					getEventInstance(event));
			break;
		case 2:
			msg = String.format("Changed data in archive %d - after change",
					getEventInstance(event));
			break;
		case 3:
			msg = String.format("Changed data in archive %d - before change",
					getEventInstance(event));
			break;
		case 4:
			msg = String.format("Cleared archive %d", getEventInstance(event));
			break;
		case 5:
			msg = String.format("Freeze in archive %d", getEventInstance(event));
			break;
		case 6:
			msg = String.format("Changed value in list %d - after change", getEventInstance(event));
			break;
		case 7:
			msg = String.format("Changed value in list %d - before change", getEventInstance(event));
		case 8:
			msg = String.format("ComFTP: value %d is gone", getEventInstance(event));
			break;
		case 9:
			msg = String.format("ComFTP: value %d is raised", getEventInstance(event));
			break;
		default:
			msg = String.format("Unknown event 0x%s", Integer.toHexString(event));
		}

		return new MeterEvent(timeStamp, MeterEvent.OTHER, event, msg);
	}

	/**
	 * checks if event is an known event
	 *
	 * @param timeStamp
	 * @param event
	 * @return MeterEvent or null is event is unknown
	 */
	private MeterEvent getGeneralEvent(Date timeStamp, int event) {
		MeterEvent me = null;
		switch (event) {
		case 0x0D01:
		case 0x2D01:
		case 0x0D02:
		case 0x2D02:
		case 0x0D03:
		case 0x2D03:
		case 0x0D04:
		case 0x2D04:
			String msg = String.format("%s %s.", getLockName(event & 0xF),
					getLockState(event));
			me = new MeterEvent(timeStamp, MeterEvent.OTHER, event, msg);
		}
		return me;
	}

	/**
	 * gets name of lock identified by i
	 *
	 * @param i
	 * @return name of lock
	 */
	private Object getLockName(int i) {
		switch (i) {
		case 1:
			return "Approval lock";
		case 2:
			return "Manufacturer lock";
		case 3:
			return "Supplier lock";
		case 4:
			return "Customer lock";
		}
		return "unknown lock " + i;
	}

	/**
	 * checks if lock was opened or closed
	 *
	 * @param event
	 * @return String with state ("opened" or "closed")
	 */
	private Object getLockState(int event) {
		if (getEventType(event) == EventType.RAISED) {
            return "opened";
		} else {
            return "closed";
		}
	}

	/**
	 * checks if an event is a known system event
	 *
	 * @param timeStamp
	 * @param event
	 * @return MeterEvent or null is event is unknown
	 */
	private MeterEvent getSystemEvent(Date timeStamp, int event) {
		MeterEvent me = null;
		String msg = "";
		int mec = MeterEvent.OTHER;

		switch (event) {
		case 0x1002:
			msg = "Restart ended";
			break;
		case 0x3002:
			msg = "Restart";
			mec = MeterEvent.CLEAR_DATA; // 0x0C
			break;
		case 0x1202:
			msg = "Data restored";
			break;
		case 0x3202:
			msg = "Data restoring";
			mec = MeterEvent.WATCHDOGRESET; // 0x03
			break;
		case 0x1702:
			msg = "Settings fault ended";
			mec = MeterEvent.APPLICATION_ALERT_STOP; // 0x13
			break;
		case 0x3702:
			msg = "Settings fault started";
			mec = MeterEvent.APPLICATION_ALERT_START; // 0x12
			break;
		case 0x1802:
			msg = "Battery warning ended";
			break;
		case 0x3802:
			msg = "Battery warning started";
            mec = 0x1F;
			break;
		case 0x1902:
			msg = "Repair mode switched off";
			break;
		case 0x3902:
			msg = "Repair mode switched on";
			break;
		case 0x1A02:
			msg = "Clock NOT justified ended";
			break;
		case 0x3A02:
			msg = "Clock NOT justified started";
            mec = 0x1E; //MeterEvent.Clock_Invalid
			break;
		case 0x1C02:
			msg = "Data transmission ended";
			break;
		case 0x3C02:
			msg = "Data transmission started";
			break;
		case 0x1D02:
			msg = "Remote clock setting ended";
			break;
		case 0x3D02:
			msg = "Remote clock setting started";
			break;
		case 0x1E02:
			msg = "Device is working with main power";
			break;
		case 0x3E02:
			msg = "Device is working in battery mode";
			break;
		case 0x1F02:
			msg = "Device clock is running in winter time";
			break;
		case 0x3F02:
			msg = "Device clock is running in summer time";
			break;
		}

		if (msg.length() > 0) {
			me = new MeterEvent(timeStamp, mec, event, msg);
		}

		return me;
	}

	/**
	 * get category of event: 0, 1, 4, 5, 8 (upper nibble, but without
	 * eventType[gone, raised]!)
	 *
	 * @param event
	 * @return category
	 */
	public int getEventCategory(int event) {
		return (event & 0xD000) >> 12;
	}

	/**
	 * separate signal from given event
	 *
	 * @param event
	 * @return signal
	 */
	public int getEventSignal(int event) {
		return ((event & 0x0F00) >> 8) + 1;
	}

	/**
	 * separate instance from given event
	 *
	 * @param event
	 * @return instance
	 */
	public int getEventInstance(int event) {
		return event & 0x00FF;
	}

	/**
	 * detect class of event (error, warning, hint)
	 *
	 * @param event
	 * @return EventClass
	 */
	public EventClass getEventClass(int event) {
		return classOfEvent[getEventSignal(event) - 1];
	}

	/**
	 * detect type of event (raised, gone)
	 *
	 * @param event
	 * @return EventType
	 */
	public EventType getEventType(int event) {
		if ((event & 0x2000) == 0) {
            return EventType.GONE;
		} else {
            return EventType.RAISED;
		}
	}

	public String getStatusRegisterName(int instance) {
		switch (instance & 0x00FF) {
		case 1:
			return "total status";
		case 2:
			return "system status";
		case 3:
			return "system status 2";
		default:
			return "program. status " + (instance - 3);
		}
	}

	public String getEventClassText(EventClass evc) {
		switch (evc) {
		case ERROR:
			return "Error";
		case WARNING:
			return "Warning";
		default:
			return "Hint";
		}
	}

	public String getEventTypeText(EventType evt) {
		if (evt == EventType.GONE) {
			return "gone";
		} else {
			return "raised";
		}
	}
}