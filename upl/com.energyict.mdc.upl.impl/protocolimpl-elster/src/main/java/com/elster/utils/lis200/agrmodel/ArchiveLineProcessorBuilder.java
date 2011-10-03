package com.elster.utils.lis200.agrmodel;

import com.elster.agrimport.agrreader.AgrColumnHeader;
import com.elster.agrimport.agrreader.AgrFileType;

import java.util.List;

/**
 * User: heuckeg
 * Date: 07.07.2010
 * Time: 13:37:18
 */
public class ArchiveLineProcessorBuilder {

    public static IArchiveLineProcessor getArchiveLineProcessor(AgrFileType fileType, List<AgrColumnHeader> columns, int colIndex) {
        switch (fileType) {
            case LIS200:
                return new Lis200LineProcessor(columns, colIndex);
            case DSFG:
                return new DsfgLineProcessor(columns, colIndex);
            default:
                return new DefaultLineProcessor(columns, colIndex);
        }
    }
}
