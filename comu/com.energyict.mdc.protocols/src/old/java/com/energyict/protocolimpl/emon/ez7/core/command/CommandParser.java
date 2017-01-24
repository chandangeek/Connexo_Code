/*
 * CommandParser.java
 *
 * Created on 13 mei 2005, 14:36
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author  Koen
 */
public class CommandParser {

    //byte[] data=null;
    String strData=null;

    /** Creates a new instance of CommandParser */
    public CommandParser(byte[] data) {
        //this.data=data;
        strData = new String(data);
    }

    public List getValues(String tag) {
        return getValues(tag,0);
    }

    public List getValues(String tag, int index) {
        String temp=strData;
        StringTokenizer strTok = new StringTokenizer(temp,"\r\n");
        int count=index;
        while(strTok.hasMoreTokens()) {
            temp = strTok.nextToken();
            if ((temp.indexOf(tag)==0) && (count-- == 0)) {
                temp = temp.substring(temp.indexOf(tag)+tag.length());
                strTok = new StringTokenizer(temp,"\t");
                List values = new ArrayList();
                while(strTok.hasMoreTokens()) {
                    values.add(strTok.nextToken());
                }
                return values;
            }
        }
        return null;
    }

    /**
     * Retrieve the values for the first occurrence of a specific tag.
     * The tag is specified by the subString with which it should ends.
     * E.g.: tag "LINE-1" can be specified by partialTag "-1" .
     *
     * @param partialTag    The subString with which the tag ends
     * @return
     */
    public List getValuesForTagEndingWith(String partialTag) {
        return getValuesForTagEndingWith(partialTag, 0);
    }

    /**
     * Retrieve the values for an occurrence of a specific tag.
     * The tag is specified by a subString with which it should ends.
     * E.g.: tag "LINE-1" can be specified by partialTag "-1" .
     *
     * @param partialTag    The subString with which the tag ends
     * @param index         The index, describing which occurrence of the tag is of interest.
     * @return
     */
    public List getValuesForTagEndingWith(String partialTag, int index) {
        String temp=strData;
        StringTokenizer strTok = new StringTokenizer(temp,"\r\n");
        int count=index;
        while(strTok.hasMoreTokens()) {
            temp = strTok.nextToken();
            if ((temp.contains(partialTag)) && (count-- == 0)) {
                temp = temp.substring(temp.indexOf(partialTag) + partialTag.length());
                strTok = new StringTokenizer(temp,"\t");
                List values = new ArrayList();
                while(strTok.hasMoreTokens()) {
                    values.add(strTok.nextToken());
                }
                return values;
            }
        }
        return null;
    }

}
