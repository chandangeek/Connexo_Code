package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util;


import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class Converter {

    public static String convertByteArrayToString(byte[] byteArr) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < byteArr.length; i++) {
            String hex = Integer.toHexString(0xFF & byteArr[i]);
            if (hex.length() == 1) {
                // could use a for loop, but we're only dealing with a single byte
                hexString.append('0');
            }
            hexString.append(hex + " ");
        }
        return hexString.toString();
    }

    public static byte[] convertStringToByteArray(String inputStr) {
        inputStr = inputStr
                .replace(" ","")
                .replace(".","")
                .replace(":","")
                .replace("-","")
                .replace(";","")
                .replace("/","")
                ;

        if (inputStr.length() % 2 == 1 ){
            inputStr = "0" + inputStr;
        }

        String[] parts = new String[inputStr.length() / 2];

        for (int i = 0; i < parts.length; i++) {
            int index = i * 2;
            parts[i] = inputStr.substring(index, index + 2);
        }

        return Converter.convertStringListToByteArray(Arrays.asList(parts));
    }

    public static byte[] convertStringArrToByteArray(String[] inputStr) {
        return Converter.convertStringListToByteArray(Arrays.asList(inputStr));
    }

    public static byte[] convertStringListToByteArray(List<String> inputStr) {
        byte[] fByteArr = new byte[inputStr.size()];
        for(int i = 0; i < inputStr.size(); i++) {
            // this transformation also deals with negative values
            // so not all 128 Bits are used
            // FF --> 00 FF
            // -FF --> FF 01
            // thus we have to check which part of the result we have to use
            byte[] byteArrTemp = new BigInteger(inputStr.get(i), 16).toByteArray();
            fByteArr[i] = byteArrTemp[0];
            if(byteArrTemp.length > 1) {
                fByteArr[i] = byteArrTemp[1];
            }
        }
        return fByteArr;
    }

    public static String convertListToString(List<String> inputStr) {
        String retString = "";
        for(int i = 0; i < inputStr.size(); i++) {
            retString = retString + inputStr.get(i);
        }
        return retString;
    }

    public static int hexToInt(String s) {
        try {
            return Integer.parseInt(s, 16);
        } catch (NumberFormatException ex) {
            System.err.println("ERROR: cannot convert to int: " + s );
            BigInteger bigInteger = new BigInteger(s, 16);
            return bigInteger.intValue();
        }
    }

    public static BigInteger hexToBigInteger(String hexString) {
        return new BigInteger(hexString, 16);
    }

    public static BigInteger hexToBigInteger(List<String> hexStringList) {
        return hexToBigInteger(convertListToString(hexStringList));
    }

    public static int hexToInt(List<String> hexIntList) {
        return hexToInt(convertListToString(hexIntList));
    }
}