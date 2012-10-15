package com.energyict;

/**
 * @author: sva
 * @since: 15/10/12 (17:16)
 */

/* This is just a dummy class, so the ProGuard plugin can do its work. Apparently the ProGuard plugin needs at least 1 valid class in order to build its jar.
 *  When ProGuard runs on an empty module, the plugin fails with 'java.io.IOException: The input doesn't contain any classes.'
 *
 *  When this dummy is no longer needed, it can be removed.
 *  Note: it should also be removed from protocolimpl-v2.pro
**/
public class Dummy {
}
