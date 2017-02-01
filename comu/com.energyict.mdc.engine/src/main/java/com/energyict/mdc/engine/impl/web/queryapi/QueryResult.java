/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.queryapi;

import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the result of the execution of a remote query api call
 * in the context of an xml marshaller.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-10 (10:16)
 */
@XmlRootElement(name = "QueryResult")
public final class QueryResult {
    @XmlAttribute(name = RemoteComServerQueryJSonPropertyNames.QUERY_ID)
    public String queryId;
    @XmlAttribute(name = RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_RESULT)
    public Object value;

    public static QueryResult forResult (String queryId, Object value) {
        QueryResult queryResult = new QueryResult();
        queryResult.queryId = queryId;
        queryResult.value = value;
        return queryResult;
    }

}