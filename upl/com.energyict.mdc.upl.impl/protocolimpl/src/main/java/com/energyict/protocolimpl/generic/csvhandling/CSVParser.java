package com.energyict.protocolimpl.generic.csvhandling;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;

import java.util.ArrayList;
import java.util.List;

public class CSVParser {

    private final DeviceMessageFileExtractor messageFileExtractor;
    private String rawText;
    private List<TestObject> lines = new ArrayList<>();

    public CSVParser(DeviceMessageFileExtractor messageFileExtractor) {
        this.messageFileExtractor = messageFileExtractor;
    }

    public void parse(byte[] rawBytes) {
        this.rawText = new String(rawBytes);
        int beginOffset = 0;
        int endOffset = this.rawText.indexOf(new String(new byte[]{0x0D, 0x0A}));
        while (endOffset != -1) {
            lines.add(new TestObject(this.rawText.substring(beginOffset, endOffset)));
            beginOffset = endOffset + 2;
            endOffset = this.rawText.indexOf(new String(new byte[]{0x0D, 0x0A}), beginOffset + 1);
        }
    }

    public TestObject getTestObject(int index) {
        return this.lines.get(index);
    }

    public int size() {
        return this.lines.size();
    }

    public int getValidSize() {
        int count = 0;
        for (int i = 0; i < size(); i++) {
            if (isValidLine(this.lines.get(i))) {
                count++;
            }
        }
        return count;
    }

    public boolean isValidLine(TestObject to) {
        if (to.size() == 0) {
            return false;
        }
        switch (to.getType()) {
            case 0: { // GET
                return true;
            }
            case 1: { // SET
                return true;
            }
            case 2: { // ACTION
                return true;
            }
            case 3: { // MESSAGE
                return true;
            }
            case 4: { // WAIT
                return true;
            }
            case 5: { // EMPTY line
                return false;
            }
            default: {
                return false;
            }
        }
    }

    public void addLine(String string) {
        lines.add(new TestObject(string));
    }
}