package com.energyict.echelon;

import java.awt.*;

class GbUtil {
    
    /* gbc GridBagConstraint */
    static GridBagConstraints gbc(double weightx, int x, int y, int gridwidth){
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = weightx;
        c.gridx = x;
        c.gridy = y; 
        c.gridwidth = gridwidth;
        c.insets = new Insets(2,2,2,2);
        c.anchor = GridBagConstraints.WEST;
        return c;
    }
    
    /* gbc GridBagConstraint */
    static GridBagConstraints gbc(double weightx, int x, int y){
        return gbc(weightx, x, y, 1);
    }
    
    /* gbc GridBagConstraint */
    static GridBagConstraints gbc( int x, int y) {
        return gbc(0, x, y);
    }
    
//    /* gbc GridBagConstraint */
//    static GridBagConstraints gbc(double weightx, int x, int y, int gridwidth){
//        GridBagConstraints c = new GridBagConstraints();
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.weightx = weightx;
//        c.gridx = x;
//        c.gridy = y; 
//        c.gridwidth = gridwidth;
//        return c;
//    }
}
