package com.elster.protocolimpl.dsfg.profile;

import com.elster.protocolimpl.dsfg.ChannelDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration info for dsfg archive.<br>
 * This class builds a list of ChannelDefinition, having the necessary
 * properties with<br>
 * their address in a dsfg archive and properties for EIServer.
 *
 * @author gh
 * @since 5/26/2010
 *
 */
public class ArchiveRecordConfig {

	private String recordConfig;
	private List<ChannelDefinition> valueIndexes = new ArrayList<>();

	public ArchiveRecordConfig(String archiveAddress, String recordConfig) throws Exception {
		this.recordConfig = recordConfig;
		parse(archiveAddress);
	}

	protected void parse(String archiveAddress) throws Exception {
		String[] colData = recordConfig.split(",");
		for (int i = 0; i < colData.length; i++) {
			ChannelDefinition cd = new ChannelDefinition(archiveAddress, colData[i]);
			valueIndexes.add(cd.getChannelNo(), cd);
		}
	}

	public int getNumberOfChannels() throws IOException {
		return this.valueIndexes.size();
	}

	ChannelDefinition getChannelDefinition(int index) {
		return this.valueIndexes.get(index);
	}

}