package com.energyict.protocolimpl.eig.nexus1272.parse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.energyict.protocolimpl.eig.nexus1272.NexusProtocolConnection;

public class ScaledEnergySettingFactory {

	private Map <String, ScaledEnergySetting> sesCache = new HashMap <String, ScaledEnergySetting>();
	
	private OutputStream outputStream;
	private NexusProtocolConnection connection;

	public ScaledEnergySettingFactory(OutputStream os ,NexusProtocolConnection npc) throws IOException {
		outputStream = os;
		connection = npc;
	}
	
	public ScaledEnergySetting getScaledEnergySetting(LinePoint lp) throws IOException {
		ScaledEnergySetting ses = sesCache.get(lp.getLine() + "." + lp.getPoint());
		if (ses == null) {
			ses = new ScaledEnergySetting(lp, outputStream, connection);
			sesCache.put(lp.getLine() + "." + lp.getPoint(), ses);
		}
		return ses;
	}
	
}
