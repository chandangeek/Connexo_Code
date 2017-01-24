package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.RequestFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.structure.field.ChannelGroupVisibility;
import com.energyict.protocolimplv2.abnt.common.structure.field.LoadProfileBlockArgument;
import com.energyict.protocolimplv2.abnt.common.structure.field.LoadProfileReadSizeArgument;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ReadParametersRequest extends Data<ReadParametersRequest> {

    private static final int PADDING_DATA_LENGTH = 57;

    private LoadProfileBlockArgument loadProfileBlockArgument;
    private ChannelGroupVisibility channelGroupVisibility;
    private LoadProfileReadSizeArgument loadProfileReadSizeArgument;
    private PaddingData paddingData;

    public ReadParametersRequest(TimeZone timeZone) {
        super(RequestFrame.REQUEST_DATA_LENGTH, timeZone);
        this.loadProfileBlockArgument = new LoadProfileBlockArgument();
        this.channelGroupVisibility = new ChannelGroupVisibility();
        this.loadProfileReadSizeArgument = new LoadProfileReadSizeArgument();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                loadProfileBlockArgument.getBytes(),
                channelGroupVisibility.getBytes(),
                loadProfileReadSizeArgument.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ReadParametersRequest parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.loadProfileBlockArgument.parse(rawData, ptr);
        ptr += this.loadProfileBlockArgument.getLength();

        this.channelGroupVisibility.parse(rawData, ptr);
        ptr += this.channelGroupVisibility.getLength();

        this.loadProfileReadSizeArgument.parse(rawData, ptr);
        return this;
    }

    public int getLoadProfileBlockCount() {
        return this.loadProfileBlockArgument.getLoadProfileBlockCount();
    }

    public void setLoadProfileBlockCount(int loadProfileBlockArgument) {
        this.loadProfileBlockArgument.setBlockCount(loadProfileBlockArgument);
    }

    public int getChannelGroupVisibilitySelection() {
        return this.channelGroupVisibility.getChannelGroupVisibilitySelection();
    }

    public void setChannelGroupSelection(int channelGroupVisibilitySelection) {
        this.channelGroupVisibility.setChannelGroupVisibilitySelection(channelGroupVisibilitySelection);
    }

    public int getLoadProfileReadSizeArgument() {
        return this.loadProfileReadSizeArgument.getReadSizeArgument();
    }

    public void setLoadProfileReadSizeArgument(LoadProfileReadSizeArgument readSizeArgument) {
        this.loadProfileReadSizeArgument = readSizeArgument;
    }
}