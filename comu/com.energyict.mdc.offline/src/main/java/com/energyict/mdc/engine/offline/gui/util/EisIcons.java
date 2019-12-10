/*
 * EisIcons.java
 *
 * Created on 7 november 2003, 9:17
 */

package com.energyict.mdc.engine.offline.gui.util;

import javax.swing.*;

/**
 * @author Geert
 */
public class EisIcons {

    private EisIcons() {
    }

    public static final Icon ENERGYICT_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/logo.png"));
    public static final Icon EISERVER_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/eiMaster16x16.png"));
    // To become a clearer icon in the task bar:
    public static final Icon EISERVER_ICON_32 = new ImageIcon(EisIcons.class.getResource("/mdw/images/eiMaster32x32.png"));

    public static final Icon EVALUATION_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/evaluation.gif"));

    public static final Icon CRITERIUM_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/criterium.gif"));
    public static final Icon INVALID_CRITERIUM_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/invalidcriterium.png"));
    //    public static Icon ANDFILTER_ICON =
//        new ImageIcon(MdwIcons.class.getResource("/mdw/images/andfilter.gif"));
//    public static Icon ORFILTER_ICON =
//        new ImageIcon(MdwIcons.class.getResource("/mdw/images/orfilter.gif"));
    public static final Icon CODEJOIN_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/Code_JOIN.gif"));
    public static final Icon CODEOUTERJOIN_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/Code_OUTERJOIN.gif"));
    public static final Icon ZOOMIN_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/zoomin.png"));
    public static final Icon ZOOMOUT_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/zoomout.png"));
    public static final Icon ALIGNTOP_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/align_top.png"));
    public static final Icon ALIGNLEFT_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/align_left.png"));
    public static final Icon ALIGNBOTTOM_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/align_bottom.png"));
    public static final Icon ALIGNRIGHT_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/align_right.png"));
    public static final Icon DELETE_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/delete.gif"));
    public static final Icon MOVETOFRONT_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/move_front.png"));
    public static final Icon MOVETOBACK_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/move_back.png"));
    public static final Icon LEFT_ICON = new ImageIcon(EisIcons.class.getResource("/images/left.png"));
    public static final Icon RIGHT_ICON = new ImageIcon(EisIcons.class.getResource("/images/right.png"));
    public static final Icon LEFTLEFT_ICON = new ImageIcon(EisIcons.class.getResource("/images/leftleft.png"));
    public static final Icon RIGHTRIGHT_ICON = new ImageIcon(EisIcons.class.getResource("/images/rightright.png"));
    public static final Icon LEFT_DISABLED_ICON = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) LEFT_ICON).getImage()));
    public static final Icon RIGHT_DISABLED_ICON = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) RIGHT_ICON).getImage()));
    public static final Icon LEFTLEFT_DISABLED_ICON = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) LEFTLEFT_ICON).getImage()));
    public static final Icon RIGHTRIGHT_DISABLED_ICON = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) RIGHTRIGHT_ICON).getImage()));
    public static final Icon UP_DISABLED_ICON = new ImageIcon(EisIcons.class.getResource("/images/up_disabled.gif"));
    public static final Icon DOWN_DISABLED_ICON = new ImageIcon(EisIcons.class.getResource("/images/down_disabled.gif"));
    public static final Icon UP_ICON = new ImageIcon(EisIcons.class.getResource("/images/up.gif"));
    public static final Icon DOWN_ICON = new ImageIcon(EisIcons.class.getResource("/images/down.gif"));
    public static final Icon EXPORT_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/export.gif"));
    public static final Icon IMPORT_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/import.gif"));
    public static final Icon TIMEDRETRIEVE_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/timed_retrieve.png"));
    public static final Icon DOWNSMALL_ICON = new ImageIcon(EisIcons.class.getResource("/images/downsmall.png"));
    public static final Icon DOWNSMALL2_ICON = new ImageIcon(EisIcons.class.getResource("/images/downsmall2.png"));
    public static final Icon PREVIOUS_SUSPECT_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/PreviousSuspect.png"));
    public static final Icon NEXT_SUSPECT_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/NextSuspect.png"));
    public static final Icon PREVIOUS_BLOCK_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/PreviousBlock.png"));
    public static final Icon NEXT_BLOCK_ICON = new ImageIcon(EisIcons.class.getResource("/mdw/images/NextBlock.png"));
    public static final Icon SPLIT_VERTICAL_ICON = new ImageIcon(EisIcons.class.getResource("/images/application-split-vertical.png"));
    public static final Icon SPLIT_HORIZONTAL_ICON = new ImageIcon(EisIcons.class.getResource("/images/application-split.png"));
    public static final Icon UNSPLIT_ICON = new ImageIcon(EisIcons.class.getResource("/images/application.png"));
    public static final Icon MODIFIED_SMALL_ICON = new ImageIcon(EisIcons.class.getResource("/images/pencil-small.png"));
    public static final Icon MODIFIED_ICON = new ImageIcon(EisIcons.class.getResource("/images/pencil.png"));
    public static final Icon CONFIRM_ICON = new ImageIcon(EisIcons.class.getResource("/images/tick.png"));
    public static final Icon GEAR_ICON = new ImageIcon(EisIcons.class.getResource("/images/gear.png"));
}	
