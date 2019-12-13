/*
 * EisInternalFrame.java
 *
 * Created on April 15, 2003, 3:36 PM
 */

package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.util.EisConst;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.prefs.Preferences;

/**
 * @author gde
 */
public class EisInternalFrame extends JInternalFrame implements WaitCursorManager, DirectCloseable {

    private OfflineFrame mainWindow;
    private final static MouseAdapter mouseAdapter = new MouseAdapter() {
    };
    private Preferences userPrefs;
    private Dimension frameDimension = null;
    private boolean storeFrameDimension = true; // Must store and reapply its dimension?
    private boolean storeFramePosition = false; // Must store and reapply its position?
    private Point framePosition = new Point(0, 0);
    private String prefKeyName = null;
    // the offset to apply for X as well as Y = (offsetIndex-1)*offset
    private int offsetIndex = 1;
    private int offset = 0;

    private String helpKey = null;
    private boolean showMaximized = false;  // if no preferences key found for dimension, show the frame as maximized

    protected JMenuBar menuBar;
    /* ------------------------- Constructors -------------------- */

    /**
     * Creates new form eisInternalFrame
     */
    public EisInternalFrame(OfflineFrame mainWindow) {
        misLeadTheWindowsDesktopManager(mainWindow);
        setMainWindow(mainWindow);
        userPrefs = Preferences.userNodeForPackage(EisInternalFrame.class);
        initComponents();
    }

    public EisInternalFrame(OfflineFrame mainWindow, String strTitle) {
        this(mainWindow, strTitle, true, true, true, true);
    }

    public EisInternalFrame(OfflineFrame mainWindow, String strTitle,
                            boolean resizable, boolean closeable,
                            boolean maximizable, boolean iconifiable) {
        super(strTitle, resizable, closeable, maximizable, iconifiable);
        userPrefs = Preferences.userNodeForPackage(EisInternalFrame.class);
        misLeadTheWindowsDesktopManager(mainWindow);
        setMainWindow(mainWindow);
        if (mainWindow != null) {
            setFrameIcon(mainWindow.getMainIcon());
        }
        initComponents();
    }



    /* ----------------------------------------------------------- */

    public OfflineFrame getMainWindow() {
        if (mainWindow == null) {
            mainWindow = UiHelper.getMainWindow();
        }
        return mainWindow;
    }
    /* ----------------------------------------------------------- */

    public void setMainWindow(OfflineFrame aMainWindow) {
        mainWindow = aMainWindow;
    }
    /* ----------------------------------------------------------- */
    // Help

    @Override
    // Since the frame is not automatically maximized,
    // the 'Help key' should be set when the frame gains focus
    public void setSelected(boolean selected) throws PropertyVetoException {
        super.setSelected(selected);
        if (this.helpKey != null && selected) {
            //TODO: RCS - check the help menu
//            getMainWindow().setHelp(this, helpKey);
        }
    }

    protected void setHelp(String key) {
        this.helpKey = key;
    }

    /* ----------------------------------------------------------- */
    // Location & sizing

    public int getOffsetIndex() {
        return offsetIndex;
    }

    public void setOffsetIndex(int index) {
        offsetIndex = index;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }


    /**
     * called just before the actual show() *
     */
    public void preShow() {
    }

    // If you want the window to store/(re)apply its dimension

    public void setStoreFrameDimension(boolean value) {
        storeFrameDimension = value;
    }
    // If you want the window to store/(re)apply its location

    public void setStoreFramePosition(boolean value) {
        storeFramePosition = value;
    }

    public void applyStoredFrameInfo() {
        if (storeFramePosition) {
            framePosition = getStoredPosition();
            if (framePosition.y<0) {
                framePosition.y = 0;
            }
            setLocation(framePosition);
        } else {
            setLocation(getDefaultPosition());
        }
        if (storeFrameDimension) {
            Dimension stored = getStoredDimension();
            if (stored != null) {
                this.setSize(stored);
            }
        } else {
            this.setSize(getDefaultDimension());
        }
        // GDE (2010-jul-29) Check for windows lying 90% outside the desktop pane
        correctForInvisibility();
    }

    private void correctForInvisibility() {
        Rectangle rectDesktopPane = new Rectangle();
        if (getDesktopPane() == null) {
            return;
        }
        rectDesktopPane.setBounds(0, 0, getDesktopPane().getWidth(), getDesktopPane().getHeight());
        Rectangle rectFrame = new Rectangle();
        rectFrame.setLocation(getLocation());
        rectFrame.setSize(getSize());
        Rectangle rectIntersect = rectDesktopPane.intersection(rectFrame);

        if (rectIntersect.width == 0) { // frame would be completely outside the screen
            setLocation(getDefaultPosition());
            setSize(getDefaultDimension());
            return;
        }

        BigDecimal surfaceIntersect = BigDecimal.valueOf(((double) (rectIntersect.width * rectIntersect.height)));
        BigDecimal surfaceFrame = BigDecimal.valueOf(((double) (rectFrame.width * rectFrame.height)));
        BigDecimal percentageViewable = surfaceIntersect.divide(surfaceFrame, RoundingMode.HALF_EVEN);
        if (percentageViewable.compareTo(BigDecimal.valueOf(0.1)) <= 0) { // frame is 10% or less visible
            setLocation(getDefaultPosition());
            setSize(getDefaultDimension());
        }
    }

    protected void setShowMaximized(boolean flag) {
        this.showMaximized = flag;
    }

    protected Point getStoredPosition() {
        int offsetToApply = (offsetIndex - 1) * offset;
        int x = userPrefs.getInt(getPrefKeyName() + EisConst.PREFKEY_LOCATIONX, -1) + offsetToApply;
        int y = userPrefs.getInt(getPrefKeyName() + EisConst.PREFKEY_LOCATIONY, -1) + offsetToApply;
        if (x == -1 || y == -1) {
            return getDefaultPosition();
        }
        return new Point(x, y);
    }

    protected Point getDefaultPosition() {
        if (getDesktopPane() == null) {
            return new Point(0, 0);
        }
        int offsetToApply = (offsetIndex - 1) * offset;
        int x = this.getDesktopPane().getLocation().x + offsetToApply;   // the desktop's left upper corner
        int y = this.getDesktopPane().getLocation().y + offsetToApply;
        return new Point(x, y);
    }

    protected Dimension getStoredDimension() {
        int width = userPrefs.getInt(getPrefKeyName() + EisConst.PREFKEY_WIDTH, -1);
        int height = userPrefs.getInt(getPrefKeyName() + EisConst.PREFKEY_HEIGHT, -1);
        if (width == -1 || height == -1) {
            if (showMaximized) {
                try {
                    this.setMaximum(true);
                    // we set the 'normalBounds'  for 'Restoring' the frame
                    int x = (getDesktopPane().getWidth() - getPreferredSize().width) / 2;
                    if (x < 0) {
                        x = 0;
                    }
                    int y = (getDesktopPane().getHeight() - getPreferredSize().height) / 2;
                    if (y < 0) {
                        y = 0;
                    }

                    this.setNormalBounds(new Rectangle(x, y, getPreferredSize().width, getPreferredSize().height));
                    return null;
                } catch (PropertyVetoException exc) {
                    return getDefaultDimension();
                }
            } else {
                return getDefaultDimension();
            }
        }
        if (getDesktopPane() != null) {
            if (width > getDesktopPane().getWidth()) {
                width = getDesktopPane().getWidth();
            }
            if (height > getDesktopPane().getHeight()) {
                height = getDesktopPane().getHeight();
            }
        }
        return new Dimension(width, height);
    }

    public void setDefaultDimension(Dimension d) {
        frameDimension = d;
    }

    protected Dimension getDefaultDimension() {
        if (getDesktopPane() == null) {
            return new Dimension(0, 0);
        }
        int defaultWidthPercentage = 80; // in % of the full screen width
        int defaultHeightPercentage = 80; // in % of (the full screen height minus the following)

        if (frameDimension != null && isAcceptableDimension(frameDimension)) {
            return frameDimension;
        }
        return new Dimension(getDesktopPane().getWidth() * defaultWidthPercentage / 100,
                getDesktopPane().getHeight() * defaultHeightPercentage / 100);
    }

    private boolean isAcceptableDimension(Dimension dim) {
        this.getMinimumSize();  // minimumSize was set in initComponents()
        return (dim.width >= this.getMinimumSize().width && dim.height >= this.getMinimumSize().height);
    }

    /* ----------------------------------------------------------- */

    public void show() {
        preShow();          // preShow can have closed the frame
        if (!this.isClosed()) {
            applyStoredFrameInfo();
            super.show();
        }
    }

    // -----------------------------------------------------------

    public void startWaitCursor() {
        // If the cursor was not over the internal frame there was no hourglass visible
        // So, set it for the mainframe too:
        if (getMainWindow() != null) {
            if (!getMainWindow().getGlassPane().isVisible()) {
                getMainWindow().getGlassPane().addMouseListener(mouseAdapter);
                getMainWindow().getGlassPane().setVisible(true);
                getMainWindow().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        }
        if (!getGlassPane().isVisible()) {
            getGlassPane().addMouseListener(mouseAdapter);
            getGlassPane().setVisible(true);
            getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
    }

    public void stopWaitCursor() {
        if (getMainWindow() != null) { // cf. startWaitCursor()
            if (getMainWindow().getGlassPane().isVisible()) {
                getMainWindow().getGlassPane().setVisible(false);
                getMainWindow().getGlassPane().removeMouseListener(mouseAdapter);
                getMainWindow().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
        if (getGlassPane().isVisible()) {
            getGlassPane().setVisible(false);
            getGlassPane().removeMouseListener(mouseAdapter);
            getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    // -------------------------------------------------------------

    public void doDefaultCloseAction() {
        doCancelableDefaultCloseAction();
    }

    // Returns true if canceled/close has to stop
    public boolean doCancelableDefaultCloseAction() {
        Component[] children = getContentPane().getComponents();
        if (children.length == 1 && (children[0] instanceof JPanel)) {
            if (!discardChanges()) {
                return true;
            }
        }
        super.doDefaultCloseAction();
        return false;
    }

    // -------------------------------------------------------------
    public void doDirectClose() {
        super.doDefaultCloseAction();
    }

    public boolean discardChanges() {
        // In case this dialog is just used as a container for one (and only one)
        // JPanel [e.g. EisMain.showModalDialog()],
        // we try to call the isDataDirty() function on the panel.
        Component[] children = getContentPane().getComponents();
        if (children.length == 1 && (children[0] instanceof JPanel)) {
            JPanel panel = (JPanel) children[0];
            if (panel == null) {
                return true;
            }

            Boolean dirty = Boolean.FALSE;
            try {
                Method method = panel.getClass().getMethod("isDataDirty", new Class[]{});
                if (method != null) {
                    try {
                        dirty = (Boolean) method.invoke(panel, (Object[]) null);
                    } catch (Exception e) {
                        return true;
                    }
                }
            } catch (NoSuchMethodException e) {
                return true;
            }
            if (dirty) {
                int choice =
                        JOptionPane.showConfirmDialog(getRootPane(),
                                TranslatorProvider.instance.get().getTranslator().getTranslation("sureToDiscardChanges"),
                                TranslatorProvider.instance.get().getTranslator().getTranslation("confirmation"),
                                JOptionPane.YES_NO_OPTION);
                return (choice == JOptionPane.YES_OPTION);
            } else {
                return true;
            } // nothing is dirty
        }
        return true;
    }

    private void updateFrameInfo() {
        if (isIcon() || isMaximum()) {
            return; // don't remember size/position of minimized/maximized state
        }
        if (storeFrameDimension) {
            frameDimension = getSize();
        }
        if (storeFramePosition && offsetIndex == 1) {
            framePosition = getLocation();
        }
    }

    protected void storeFrameInfo() {
        if (isIcon() || isMaximum()) {
            if (showMaximized) {
                // remove the preferences for frames that are shown maximized
                userPrefs.remove(getPrefKeyName() + EisConst.PREFKEY_LOCATIONX);
                userPrefs.remove(getPrefKeyName() + EisConst.PREFKEY_LOCATIONY);
                userPrefs.remove(getPrefKeyName() + EisConst.PREFKEY_WIDTH);
                userPrefs.remove(getPrefKeyName() + EisConst.PREFKEY_HEIGHT);
                return;
            }
        }
        if (storeFrameDimension && frameDimension != null) {
            userPrefs.putInt(getPrefKeyName() + EisConst.PREFKEY_WIDTH, frameDimension.width);
            userPrefs.putInt(getPrefKeyName() + EisConst.PREFKEY_HEIGHT, frameDimension.height);
        }
        if (storeFramePosition && offsetIndex == 1) {
            // only store the "base" (=not offset-ed) frame's position (=offsetIndex==1)
            userPrefs.putInt(getPrefKeyName() + EisConst.PREFKEY_LOCATIONX, framePosition.x);
            userPrefs.putInt(getPrefKeyName() + EisConst.PREFKEY_LOCATIONY, framePosition.y);
        }
    }

    public void setPrefKeyName(String name) {
        prefKeyName = name;
    }

    public String getPrefKeyName() {
        if (prefKeyName != null) {
            return prefKeyName;
        }
        return getClass().getSimpleName();
    }

    public void storeUserPref(String key, int value) {
        userPrefs.putInt(getPrefKeyName() + key, value);
    }

    public int getStoredUserPref(String key, int defValue) {
        return userPrefs.getInt(getPrefKeyName() + key, defValue);
    }

    private void initComponents() {
        this.setMinimumSize(new Dimension(100, 40));

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                updateFrameInfo();
            }

            public void componentResized(java.awt.event.ComponentEvent evt) {
                updateFrameInfo();
            }
        });
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
                doOnFrameClosed();
            }

            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
                storeFrameInfo();
                doOnFrameClosing();
            }
        });
    }

    // to be overwritten in sub classes

    public void doOnFrameClosed() {}

    public void doOnFrameClosing() {}

    public void addMenu(JMenu menu) {
        if (menuBar == null) {
            menuBar = new JMenuBar();
            setJMenuBar(menuBar);
        }
        menuBar.add(menu);
    }

    public void removeMenu(JMenu menu) {
        if (menuBar != null) {
            menuBar.remove(menu);
        }
    }

    public void removeAllMenus() {
        if (menuBar != null) {
            menuBar.removeAll();
        }
    }

    private void misLeadTheWindowsDesktopManager(OfflineFrame mainWindow) {
        // avoid frame to be maximized by the WindowsDesktopManager when cloasing other maximized frame
        // and this one is the next to be selected
        if (mainWindow != null) {
            JDesktopPane desktopPane = (JDesktopPane) mainWindow.getContentPane();
            if (desktopPane != null) {
                if ("com.sun.java.swing.plaf.windows.WindowsDesktopManager".equals(desktopPane.getDesktopManager().getClass().getName())) {
                    putClientProperty("JInternalFrame.frameType", "optionDialog");
                }
            }
        }
    }

}
