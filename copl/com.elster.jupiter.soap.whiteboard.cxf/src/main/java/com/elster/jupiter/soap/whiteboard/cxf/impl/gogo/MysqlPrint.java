package com.elster.jupiter.soap.whiteboard.cxf.impl.gogo;

import java.util.Arrays;
import java.util.List;

/**
 * Created by bvn on 5/12/16.
 */
public class MysqlPrint {
    private static String spaces = "                                                                                     ";
    private static String stars = "---------------------------------------------------------------------------------------------";

    public void printTableWithHeader(List<List<?>> items) {
        int[] maxLength = getColumnWidths(items);
        printSeparator(stars, maxLength);
        System.out.print("|");
        for (int i = 0; i < maxLength.length; i++) {
            System.out.print(" " + items.get(0).get(i));
            System.out.print(spaces.substring(0, maxLength[i] - items.get(0).get(i).toString().length() + 1));
            System.out.print("|");
        }
        System.out.println();
        printTable(items.subList(1, items.size()), maxLength);
    }

    public void printTable(List<List<?>> items) {
        printTable(items, getColumnWidths(items));
    }

    private void printTable(List<List<?>> items, int[] maxLength) {
        printSeparator(stars, maxLength);
        for (List<?> item : items) {
            System.out.print("|");
            for (int i = 0; i < maxLength.length; i++) {
                String value = i < item.size() ? item.get(i) == null ? "" : item.get(i).toString() : "";
                System.out.print(" " + value);
                System.out.print(spaces.substring(0, maxLength[i] - value.length() + 1));
                System.out.print("|");
            }
            System.out.println();
        }
        printSeparator(stars, maxLength);
    }

    private int[] getColumnWidths(List<List<?>> items) {
        int[] maxLength = new int[items.get(0).size()];
        Arrays.fill(maxLength, 0);
        for (List<?> entry : items) {
            for (int i = 0; i < entry.size(); i++) {
                if (entry.get(i) != null && entry.get(i).toString().length() > maxLength[i]) {
                    maxLength[i] = entry.get(i).toString().length();
                }
            }
        }
        return maxLength;
    }

    private void printSeparator(String stars, int[] maxLength) {
        System.out.print("+");
        for (int columnWidth : maxLength) {
            System.out.print(stars.substring(0, columnWidth + 2));
            System.out.print("+");
        }
        System.out.println();
    }

}
