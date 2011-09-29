package com.elster.utils.lis200.agrmodel;

import com.elster.agrimport.agrreader.*;

import java.util.Date;
import java.util.List;

/**
 * This class builds a ArchiveLine from a given AgrArchiveLine
 * <p/>
 * User: heuckeg
 * Date: 05.07.2010
 * Time: 14:59:06
 */
public class ArchiveLineBuilder {

    /* Thi map hold the column information */
    private List<AgrColumnHeader> header;

    /* indexes to special columns */
    private int idxGloSeqNo = -1;
    private int idxArcSeqNo = -1;
    private int idxTimeStamp = -1;
    private int idxSysStat = -1;

    /**
     * Constructor of builder...
     *
     * @param header   - header information
     */
    public ArchiveLineBuilder(List<AgrColumnHeader> header) {
        this.header = header;
        findIndexes();
    }

    /**
     * private method to detect all needed special columns
     */
    private void findIndexes() {
        /* check what info is in the line */
        AgrColumnHeader ach;
        for (int i = 0; i < header.size(); i++) {
            ach = header.get(i);
            switch (ach.getColumnType()) {
                case GLOBORDERNUMBER:
                    idxGloSeqNo = i;
                    break;
                case ORDERNUMBER:
                    idxArcSeqNo = i;
                    break;
                case TIMESTAMP:
                    idxTimeStamp = i;
                    break;
                case STATUS_REGISTER:
                    if (ach.getHeadName().equalsIgnoreCase("ST.SY") ||
                            ach.getHeadName().equalsIgnoreCase("STSY")) {
                        idxSysStat = i;
                    }
                    break;
            }
        }
    }

    public ArchiveLine makeArchiveLine(AgrArchiveLine readLine, int lineNo) {
        ArchiveLine result = null;

        if (readLine != null) {

            Date tst = null;
            Long gloNo = null;
            Long arcNo = null;
            Boolean isSummerTime = null;

            if (idxTimeStamp >= 0) {
                tst = ((AgrValueDate) readLine.get(idxTimeStamp)).getValue();
            }

            if (idxGloSeqNo >= 0) {
                gloNo = ((AgrValueLong) readLine.get(idxGloSeqNo)).getValue();
            }

            if (idxArcSeqNo >= 0) {
                arcNo = ((AgrValueLong) readLine.get(idxArcSeqNo)).getValue();
            }

            if (idxSysStat >= 0) {
                int s = ((AgrValueStatusregister) readLine.get(idxSysStat)).shortValue();
                isSummerTime = new Boolean((s & 0x8000) > 0);
            }

            result = new ArchiveLine(lineNo, tst, arcNo, gloNo, isSummerTime);

            result.setData(readLine);
        }

        return result;
    }
}