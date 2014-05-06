package com.energyict.mdc.engine.impl.core.inbound;

/**
 * Acts as a place holder for a {@link ComPortRelatedComChannel} that
 * does not actually exists yet. As such a ComChannelPlaceHolder
 * may be regarded as a {@link java.util.concurrent.Future}
 * without the capabilities to cancel the asynchroneous computation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-20 (08:49)
 */
public final class ComChannelPlaceHolder {

    private ComPortRelatedComChannel comChannel;

    public static ComChannelPlaceHolder empty () {
        return new ComChannelPlaceHolder();
    }

    public static ComChannelPlaceHolder forKnownComChannel (ComPortRelatedComChannel comChannel) {
        ComChannelPlaceHolder placeHolder = new ComChannelPlaceHolder();
        placeHolder.setComChannel(comChannel);
        return placeHolder;
    }

    private ComChannelPlaceHolder () {super();}

    public ComPortRelatedComChannel getComChannel () {
        return comChannel;
    }

    public void setComChannel (ComPortRelatedComChannel comChannel) {
        this.comChannel = comChannel;
    }

}