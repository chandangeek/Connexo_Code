package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;

/**
 * Acts as a place holder for a {@link ComPortRelatedComChannel} that
 * does not actually exists yet. As such a ComChannelPlaceHolder
 * may be regarded as a {@link java.util.concurrent.Future}
 * without the capabilities to cancel the asynchronous computation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-20 (08:49)
 */
public final class ComChannelPlaceHolder {

    private ComPortRelatedComChannel comPortRelatedComChannel;

    public static ComChannelPlaceHolder empty () {
        return new ComChannelPlaceHolder();
    }

    public static ComChannelPlaceHolder forKnownComChannel (ComPortRelatedComChannel comPortRelatedComChannel) {
        ComChannelPlaceHolder placeHolder = new ComChannelPlaceHolder();
        placeHolder.setComPortRelatedComChannel(comPortRelatedComChannel);
        return placeHolder;
    }

    private ComChannelPlaceHolder () {super();}

    public ComPortRelatedComChannel getComPortRelatedComChannel() {
        return comPortRelatedComChannel;
    }

    public void setComPortRelatedComChannel(ComPortRelatedComChannel comPortRelatedComChannel) {
        this.comPortRelatedComChannel = comPortRelatedComChannel;
    }

}