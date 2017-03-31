package com.energyict.mdc.upl;

/**
 * Provides functionality to identify the protocol based on an unique description.
 *
 * @author sva
 * @since 30/10/13 - 15:11
 */
public interface DeviceDescriptionSupport {

    /**
     * Getter for the (unique) protocol description, formatted like<br/>
     * <i> &lt;Manufacturer&gt;[\&lt;Old Manufacturer&gt;] &lt;Device Type or Family&gt; &lt;Protocol Base&gt; </i>
     */
    String getProtocolDescription();

}