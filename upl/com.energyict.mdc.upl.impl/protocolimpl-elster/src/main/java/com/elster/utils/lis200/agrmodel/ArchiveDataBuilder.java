package com.elster.utils.lis200.agrmodel;

import com.elster.agrimport.agrreader.AgrFileException;
import com.elster.agrimport.agrreader.AgrReader;

import java.io.*;
import java.text.DateFormat;

/**
 * This class is a builder class for archive data from an agr file
 * <p/>
 * User: heuckeg
 * Date: 05.07.2010
 * Time: 11:31:50
 */
@SuppressWarnings({"unused"})
public class ArchiveDataBuilder {

    /**
     * ArchiveDataBuilder what accepts path to agr file
     *
     * @param file - complete path to agr file
     * @param dateTimeFormat - defines date format in agr file
     * @return ArchiveData - content of file
     * @throws java.io.FileNotFoundException - if given file doesn't exist
     * @throws AgrFileException      - if errors occur during parsing
     */
    public static ArchiveData readAgrFile(String file, DateFormat dateTimeFormat) throws FileNotFoundException, AgrFileException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        return readAgrFile(reader, dateTimeFormat);
    }

    /**
     * ArchiveDataBuilder with given stream
     *
     * @param stream - with InputStream to agr file
     * @param dateTimeFormat - defines date format in agr file
     * @return ArchiveData - content of file
     * @throws AgrFileException - if errors occur during parsing
     */
    public static ArchiveData readAgrFile(InputStream stream, DateFormat dateTimeFormat) throws AgrFileException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return readAgrFile(reader, dateTimeFormat);
    }

    public static ArchiveData readAgrFile(InputStream stream, DateFormat dateTimeFormat, String charsetName) throws AgrFileException {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream, charsetName));
        } catch (UnsupportedEncodingException e) {
            throw new AgrFileException("Unsupported encoding exception (" + e.getMessage() + ")");
        }
        return readAgrFile(reader, dateTimeFormat);
    }

    /**
     * ArchiveDataBuilder with given reader
     *
     * @param reader         - with BufferedReader to agr file
     * @param dateTimeFormat - defines date format in agr file
     * @return ArchiveData - content of file
     * @throws AgrFileException - if errors occur during parsing
     */
    public static ArchiveData readAgrFile(BufferedReader reader, DateFormat dateTimeFormat) throws AgrFileException {

        ArchiveData result;

        /* create a agr reader to read file */
        AgrReader agrReader = new AgrReader(reader, true, dateTimeFormat);
        /* read header lines */
        agrReader.readHeader();
        /* create a ArchiveData object to hold all data */
        result = new ArchiveData();

        /* fill ArchiveData object with read data */
        result.setArchiveName(agrReader.getAgrFileHeadLine().getArchiveName());
        result.setArchiveDevice(agrReader.getAgrFileHeadLine().getSerialNumber());
        result.setArchiveDeviceType(agrReader.getAgrFileHeadLine().getDeviceType());

        result.setFileType(agrReader.getFileType());

        result.setInfo(agrReader.getHeader());

        result.setColumns(agrReader.getColumnHeaders());

        ArchiveLineBuilder alBuilder = new ArchiveLineBuilder(agrReader.getColumnHeaders());

        ArchiveLine line;
        int lineNo = 0;
        do {
            lineNo++;
            line = alBuilder.makeArchiveLine(agrReader.buildNextAgrLine(), lineNo);
            if (line != null) {
                result.add(line);
            }
        } while (line != null);

        return result;
    }


}
