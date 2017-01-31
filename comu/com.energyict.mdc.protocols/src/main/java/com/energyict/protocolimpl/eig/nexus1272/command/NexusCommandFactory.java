/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
	
	private int getNextTransId() {
		transID++;
		if (transID==65536)
			transID = 0;
		return transID;
	}
	
	public Command getAuthenticationCommand() {
		return new AuthenticationCommand(getNextTransId());
	}
	
	public Command getVerifyAuthenticationCommand() {
		return new VerifyAuthenticationCommand(getNextTransId());
	}
	
	public Command getSerialNumberCommand() {
		return new SerialNumberCommand(getNextTransId());
	}
	
	public Command getCommBootVersionCommand() {
		return new CommBootVersionCommand(getNextTransId());
	}
	
	public Command getCommRunVersionCommand() {
		return new CommRunVersionCommand(getNextTransId());
	}
	
	public Command getDSPBootVersionCommand() {
		return new DSPBootVersionCommand(getNextTransId());
	}
	
	public Command getDSPRunVersionCommand() {
		return new DSPRunVersionCommand(getNextTransId());
	}
	
	public Command getGetTimeCommand() {
		return new GetTimeCommand(getNextTransId());
	}
	
	public Command getSetTimeCommand() {
		return new SetTimeCommand(getNextTransId());
	}
	
	public Command getSystemLogHeaderCommand() {
		return new SystemLogHeaderCommand(getNextTransId());
	}

	public Command getSystemLogWindowCommand() {
		return new SystemLogWindowCommand(getNextTransId());
	}
	
	public Command getWriteSingleRegisterCommand() {
		return new WriteSingleRegisterCommand(getNextTransId());
	}
	
	public Command getDataPointersCommand() {
		return new DataPointersCommand(getNextTransId());
	}

	public Command getHistorical2LogWindowCommand() {
		return new Historical2LogWindowCommand(getNextTransId());
	}

	public Command getHistorical2LogHeaderCommand() {
		return new Historical2LogHeaderCommand(getNextTransId());
	}

	public Command getLimitTriggerLogHeaderCommand() {
		return new LimitTriggerLogHeaderCommand(getNextTransId());
	}

	public Command getLimitTriggerLogWindowCommand() {
		return new LimitTriggerLogWindowCommand(getNextTransId());
	}

	public Command getLimitSnapshotLogWindowCommand() {
		return new LimitSnapshotLogWindowCommand(getNextTransId());
	}

	public Command getLimitSnapshotLogHeaderCommand() {
		return new LimitSnapshotLogHeaderCommand(getNextTransId());
	}

	public Command getReadSingleRegisterCommand() {
		return new ReadSingleRegisterCommand(getNextTransId());
	}
}
