package com.energyict.mdc.engine.offline.gui.core;

import javax.swing.*;

/**
 * JPasswordField uses an &quot;*&quot; as echo character;
 * Windows uses the &quot;\u2022&quot; character as echo character
 * This patched JPassWordField set the echo character to character &quot;\u2022&quot;
 * if the detected Operating System is Windows
 */
public class PatchedJPasswordField extends JPasswordField {

    public PatchedJPasswordField() {
        super();
        setInitialEchoChar();
    }

    public PatchedJPasswordField(int columns) {
        super(columns);
        setInitialEchoChar();
    }

    public void setPlainText(boolean plainText){
        if (plainText) {
            putClientProperty("JPasswordField.cutCopyAllowed", true);
            setEchoChar((char) 0);
        } else {
            setInitialEchoChar();
        }
    }

    private void setInitialEchoChar() {
        if (System.getProperty("os.name").contains("Windows")) {
            setEchoChar('\u2022');
        }
    }

}
