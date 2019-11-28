package com.energyict.mdc.engine.offline.gui.actions;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.windows.AboutBoxPnl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ShowAboutAction extends AbstractAction {

    private JFrame parentFrame;

    public ShowAboutAction(OfflineFrame mainFrame) {
        this.parentFrame = mainFrame;
        putValue(Action.NAME, TranslatorProvider.instance.get().getTranslator().getTranslation("about"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        putValue(ActionKeys.MAIN_FRAME, mainFrame);
    }

    public void actionPerformed(ActionEvent event) {
        UiHelper.showModalDialog(parentFrame, new AboutBoxPnl(), UiHelper.translate("mmr.aboutComServerMobile"));
    }
}
