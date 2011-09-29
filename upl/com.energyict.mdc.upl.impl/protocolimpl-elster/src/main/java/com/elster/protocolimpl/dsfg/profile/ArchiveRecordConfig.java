/**
 * 
 */
package com.elster.protocolimpl.dsfg.profile;

import com.elster.protocolimpl.dsfg.ChannelDefinition;

import java.io.IOException;
import java.util.ArrayList;

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

	/** Contains a list of the indexes of the values */
	private ArrayList<ChannelDefinition> valueIndexes = new ArrayList<ChannelDefinition>();

	/**
	 * Default constructor
	 * 
	 * @param archiveAddress - address letter of archive
     *
     * @param recordConfig - the raw record configuration string
     *
	 * @throws Exception
	 *             - if there is an error in the configuration string
	 */
	public ArchiveRecordConfig(String archiveAddress, String recordConfig)
			throws Exception {
		this.recordConfig = recordConfig;
		parse(archiveAddress);
	}

	/**
	 * Parse the configuration string with the representing indexes
	 * 
	 * @throws Exception
	 * 
	 * @throws java.io.IOException
	 */
	protected void parse(String archiveAddress) throws Exception {

		String[] colData = recordConfig.split(",");

		for (int i = 0; i < colData.length; i++) {
			ChannelDefinition cd = new ChannelDefinition(archiveAddress,
					colData[i]);
			valueIndexes.add(cd.getChannelNo(), cd);
		}
	}

	/**
	 * @return the number of channels
	 */
	public int getNumberOfChannels() throws IOException {
		return this.valueIndexes.size();
	}

	/**
	 * Getter for channel definition...
	 * 
	 * @param index
	 *            of definition
	 * 
	 * @return ChannelDefinition
	 */
	public ChannelDefinition getChannelDefinition(int index) {
		return this.valueIndexes.get(index);
	}

}
