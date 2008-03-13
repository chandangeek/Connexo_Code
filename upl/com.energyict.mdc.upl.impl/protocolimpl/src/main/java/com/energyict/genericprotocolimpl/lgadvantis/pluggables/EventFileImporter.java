package com.energyict.genericprotocolimpl.lgadvantis.pluggables;

import com.energyict.cbo.BusinessException;
import com.energyict.eisimport.core.AbstractReaderImporter;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class EventFileImporter extends AbstractReaderImporter {

	public EventFileImporter() {
	}

	public String getVersion() {
		return "$Revision: 1.1 $";
	}

	protected void importReader() throws BusinessException, SQLException, IOException {
		
        try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(this.getFile(), new EventFileSaxHandler() );
		} catch (ParserConfigurationException e) {
			getLogger().warning(e.getMessage());
			e.printStackTrace();
			throw new BusinessException(e);
		} catch (SAXException e) {
			getLogger().warning(e.getMessage());
			e.printStackTrace();
			throw new BusinessException(e);
		}

	}

	protected void preImport() throws BusinessException , SQLException {
	}

	
}
