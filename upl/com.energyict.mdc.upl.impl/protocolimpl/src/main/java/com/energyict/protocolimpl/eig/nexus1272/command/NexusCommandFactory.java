package com.energyict.protocolimpl.eig.nexus1272.command;

public class NexusCommandFactory {

	private int transID;
	private static NexusCommandFactory ncf;
	
	private NexusCommandFactory() {
		transID = 0;
	}
	
	public static NexusCommandFactory getFactory() {
		if (ncf == null)
			ncf = new NexusCommandFactory();
		return ncf;
	}
	
	public Command getAuthenticationCommand() {
		return new AuthenticationCommand(transID++);
	}
	
	public Command getVerifyAuthenticationCommand() {
		return new VerifyAuthenticationCommand(transID++);
	}
	
	public Command getSerialNumberCommand() {
		return new SerialNumberCommand(transID++);
	}
	
	public Command getCommBootVersionCommand() {
		return new CommBootVersionCommand(transID++);
	}
	
	public Command getCommRunVersionCommand() {
		return new CommRunVersionCommand(transID++);
	}
	
	public Command getDSPBootVersionCommand() {
		return new DSPBootVersionCommand(transID++);
	}
	
	public Command getDSPRunVersionCommand() {
		return new DSPRunVersionCommand(transID++);
	}
	
	public Command getGetTimeCommand() {
		return new GetTimeCommand(transID++);
	}
	
	public Command getSetTimeCommand() {
		return new SetTimeCommand(transID++);
	}
	
	public Command getSystemLogHeaderCommand() {
		return new SystemLogHeaderCommand(transID++);
	}

	public Command getSystemLogWindowCommand() {
		return new SystemLogWindowCommand(transID++);
	}
	
	public Command getWriteSingleRegisterCommand() {
		return new WriteSingleRegisterCommand(transID++);
	}
	
	public Command getDataPointersCommand() {
		return new DataPointersCommand(transID++);
	}

	public Command getHistorical2LogWindowCommand() {
		return new Historical2LogWindowCommand(transID++);
	}

	public Command getHistorical2LogHeaderCommand() {
		return new Historical2LogHeaderCommand(transID++);
	}

	public Command getLimitTriggerLogHeaderCommand() {
		return new LimitTriggerLogHeaderCommand(transID++);
	}

	public Command getLimitTriggerLogWindowCommand() {
		return new LimitTriggerLogWindowCommand(transID++);
	}

	public Command getLimitSnapshotLogWindowCommand() {
		return new LimitSnapshotLogWindowCommand(transID++);
	}

	public Command getLimitSnapshotLogHeaderCommand() {
		return new LimitSnapshotLogHeaderCommand(transID++);
	}

	public Command getReadSingleRegisterCommand() {
		return new ReadSingleRegisterCommand(transID++);
	}
}
