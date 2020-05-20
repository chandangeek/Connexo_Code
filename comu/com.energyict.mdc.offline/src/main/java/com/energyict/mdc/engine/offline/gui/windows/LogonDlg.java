package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.remote.RemoteProperties;
import com.energyict.mdc.engine.offline.UserEnvironment;
import com.energyict.mdc.engine.impl.core.offline.OfflineComServerProperties;
import com.energyict.mdc.engine.offline.core.OfflinePropertiesProvider;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.core.AntiAliasLabel;
import com.energyict.mdc.engine.offline.gui.core.PatchedJPasswordField;
import com.energyict.mdc.engine.offline.gui.dialogs.EisDialog;
import com.energyict.mdc.engine.offline.gui.security.EISLoginException;
import com.energyict.mdc.engine.offline.gui.util.EisConst;
import com.energyict.mdc.engine.offline.gui.util.EisIcons;
import com.energyict.mdc.engine.offline.gui.util.HashingUtil;
import com.energyict.mdc.engine.offline.model.OfflineUser;
import com.energyict.mdc.engine.users.OfflineUserInfo;

import javax.security.auth.login.LoginException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import static com.elster.jupiter.util.Checks.is;

public class LogonDlg extends EisDialog {

    private static final int BACKGROUND_IMAGE_WIDTH = 640;
    private static final int BACKGROUND_IMAGE_HEIGHT = 344;
    private static final String BACKGROUND_IMAGE = "/mdw/images/ComServerMobile_login.png";
    private static final int TEXTFIELDS_OFFSET_X = 395;
    private static final int TEXTFIELDS_OFFSET_Y = 90;
    private static final String SHA_PASSWORD_HASHING_MECHANISM = "SHA-256";
    private static final String DEFAULT_PASSWORD_HASHING_MECHANISM = "MD5";

    // Components
    private JTextField nameField;
    private JPasswordField passwordField;
    private JButton okButton;

    private boolean logonSuccess = false;
    private Preferences userPrefs;
    private String name = null; // overruling
    private String password = null; // overruling
    private boolean canceled = false;

    private Cursor prevCursor;

    private final OfflineFrame offlineFrame;

    public LogonDlg(OfflineFrame offlineFrame, boolean modal) {
        super(offlineFrame, "", modal);
        this.offlineFrame = offlineFrame;
        Object arg[] = new Object[2];
        arg[0] = getApplicationName();
        String version = OfflinePropertiesProvider.getInstance().getConnexoVersion();
        arg[1] = (version == null ? "" : version + " ");
        setTitle(Utils.format(getLogonTitle(), arg));
        initComponents();
        getContentPane().setPreferredSize(new Dimension(BACKGROUND_IMAGE_WIDTH, BACKGROUND_IMAGE_HEIGHT));

        pack();
        addBackgroundImage(getBackgroundImage());
        getRootPane().setDefaultButton(this.okButton);
        this.userPrefs = Preferences.userNodeForPackage(LogonDlg.class);
    }

    protected String getApplicationName() {
        return "ComServer Mobile";
    }

    protected String getLogonTitle() {
        return TranslatorProvider.instance.get().getTranslator().getTranslation("logon");
    }

    protected ImageIcon getBackgroundImage() {
        return new ImageIcon(EisIcons.class.getResource(BACKGROUND_IMAGE));
    }

    public boolean logon() {
        if ((this.name != null && this.password != null) || hideLogonDialog()) {
            okButtonActionPerformed();
        } else {
            setLocationRelativeTo(null); // center
            String user = this.userPrefs.get(EisConst.PREFKEY_LOGON_USER, "");

            if (!Utils.isNull(user)) {
                this.nameField.setText(user);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        passwordField.requestFocusInWindow();
                    }
                });
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        nameField.requestFocusInWindow();
                    }
                });
            }
            if (!isVisible()) {
                setAlwaysOnTop(true);
                setFocusableWindowState(true);
                setVisible(true);
            }
        }
        return this.logonSuccess;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    @Override
    public void performEscapeAction(KeyEvent evt) {
        this.canceled = true;
        this.logonSuccess = false;
        closeDialog();
    }

    private boolean hideLogonDialog() {
        return false;   //Not allowed in offline comserver
    }

    private void initComponents() {// GEN-BEGIN:initComponents
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog();
            }
        });

        AntiAliasLabel nameLabel = new AntiAliasLabel(TranslatorProvider.instance.get().getTranslator().getTranslation("name") + ":");
        nameLabel.setForeground(Color.white);
        Font labelFont = nameLabel.getFont().deriveFont(18f);
        nameLabel.setFont(labelFont);

        this.nameField = new JTextField(this.name);
        this.nameField.setBorder(new javax.swing.border.EmptyBorder(new Insets(1, 5, 1, 1)));
        this.nameField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }

            public void focusGained(FocusEvent e) {
                selectName();
            }
        });

        AntiAliasLabel passwordLabel = new AntiAliasLabel(TranslatorProvider.instance.get().getTranslator().getTranslation("password") + ":");
        passwordLabel.setForeground(Color.white);
        passwordLabel.setFont(labelFont);

        this.passwordField = new PatchedJPasswordField();
        this.passwordField.setBorder(new javax.swing.border.EmptyBorder(new Insets(1, 5, 1, 1)));
        this.passwordField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }

            public void focusGained(FocusEvent e) {
                selectPassword();
            }
        });

        this.okButton = new JButton();
        this.okButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("label.login"));
        this.okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setPreferredSize(new Dimension(205, 30));
        buttonPanel.setOpaque(false);
        buttonPanel.add(this.okButton);

        JPanel userPanel = new JPanel(new GridBagLayout());
        userPanel.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 5, 5, 0);
        userPanel.add(nameLabel, gc);

        gc.gridy = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 5, 10, 12);
        userPanel.add(this.nameField, gc);

        gc.gridy = 2;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(2, 5, 5, 0);
        userPanel.add(passwordLabel, gc);

        gc.gridy = 3;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 5, 0, 12);
        userPanel.add(this.passwordField, gc);

        gc.gridy = 4;
        gc.anchor = GridBagConstraints.EAST;
        gc.insets = new Insets(35, 0, 0, 0);
        userPanel.add(buttonPanel, gc);

        JPanel thePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thePanel.setBorder(new javax.swing.border.EmptyBorder(new Insets(TEXTFIELDS_OFFSET_Y, TEXTFIELDS_OFFSET_X, 0, 0)));
        thePanel.setOpaque(false);
        thePanel.add(userPanel);
        setContentPane(thePanel);

        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 23));
        nameField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 23));

        pack();
    }

    private void focusPassword() {
        this.passwordField.requestFocus();
    }

    private void selectPassword() {
        if (this.passwordField.getPassword().length > 0) {
            this.passwordField.setSelectionStart(0);
            this.passwordField.setSelectionEnd(this.passwordField.getPassword().length);
        }
    }

    private void selectName() {
        if (this.nameField.getText().length() > 0) {
            this.nameField.setSelectionStart(0);
            this.nameField.setSelectionEnd(this.nameField.getText().length());
        }
    }

    private void doLogin() {

        this.canceled = false;
        this.logonSuccess = false;
        boolean doClose = true;

        while (true) {
            try {
                boolean online;
                ComServerDAO remoteComServerDAO = null;
                if (new RemoteProperties(OfflineComServerProperties.getInstance().getProperties()).getRemoteQueryApiUrl() != null) {
                    try {
                        remoteComServerDAO = offlineFrame.getOfflineExecuter().getRemoteComServerDAO();
                        online = true;
                    } catch (Throwable e) {
                        //Cannot go online, let's work offline.
                        online = false;
                    }
                } else {
                    online = false;
                }

                List<OfflineUserInfo> userInfos = new ArrayList<OfflineUserInfo>();
                if (online) {
                    userInfos = remoteComServerDAO.getUsersCredentialInformation();
                    offlineFrame.getOfflineWorker().getFileManager().saveUserInfos(userInfos);
                } else {
                    userInfos = offlineFrame.getOfflineWorker().getFileManager().loadUserInfos();
                    if (userInfos == null) {
                        throw new LoginException(TranslatorProvider.instance.get().getTranslator().getTranslation("noUserCredentialsAvailable"));
                    }
                }

                for (OfflineUserInfo userInfo : userInfos) {
                    if (userInfo.getUserName().equalsIgnoreCase(getUserName())) {
                        if (userInfo.isCanUseComServerMobile()) {
                            this.logonSuccess = !is(getPassword()).empty() && new HashingUtil().createHash(getPassword(), userInfo.getSalt()).equals(userInfo.getHash());
                            if (this.logonSuccess && (this.name == null) && (this.password == null)) {
                                offlineFrame.getOfflineExecuter().setComServerUser(new OfflineUser(userInfo));
                                storeUser(); // if no overruling
                                return;
                            } else {
                                throw new EISLoginException(EISLoginException.Type.INVALID_PASSWORD, getUserName());
                            }
                        } else {
                            throw new EISLoginException(EISLoginException.Type.USER_NOT_AUTHORIZED, getUserName());
                        }
                    }
                }
                //If name was not found in the list of user info
                throw new EISLoginException(EISLoginException.Type.UNKNOWN_USERNAME, getUserName());
            } catch (EISLoginException ex) { // Invalid User name/Password
                switch (ex.getErrorType()) {
                    case INVALID_PASSWORD:
                        // Fall through...
                    case UNKNOWN_USERNAME:
                        JOptionPane.showMessageDialog(this, UserEnvironment.getDefault().getMsg("mdw/language.ErrorMsg", "invalidUserOrPassword"),
                                TranslatorProvider.instance.get().getTranslator().getTranslation("message"), JOptionPane.ERROR_MESSAGE);
                        doClose = false;
                        return;
                    case ACCOUNT_EXPIRED:
                        String pattern = UserEnvironment.getDefault().getMsg("mdw/language.ErrorMsg", "userXLockedOut");
                        String errMsg = Utils.format(pattern, new Object[]{getUserName()});
                        JOptionPane.showMessageDialog(this, errMsg, TranslatorProvider.instance.get().getTranslator().getTranslation("message"), JOptionPane.ERROR_MESSAGE);
                        doClose = false;
                        return;
                    case USER_NOT_AUTHORIZED:
                        pattern = UserEnvironment.getDefault().getMsg("mdw/language.ErrorMsg", "userHasNoComServerMobileRights");
                        errMsg = Utils.format(pattern, new Object[]{getUserName()});
                        JOptionPane.showMessageDialog(this, errMsg, TranslatorProvider.instance.get().getTranslator().getTranslation("message"), JOptionPane.ERROR_MESSAGE);
                        doClose = false;
                        return;
                }
            } catch (LoginException ex) {     //Exception while logging in
                JOptionPane.showMessageDialog(this, ex.getMessage(), TranslatorProvider.instance.get().getTranslator().getTranslation("message"), JOptionPane.ERROR_MESSAGE);
                if (hideLogonDialog()) {
                    this.canceled = true;
                    doClose = true;
                    return;
                } else {
                    doClose = false;
                    return;
                }
            } catch (ApplicationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), TranslatorProvider.instance.get().getTranslator().getTranslation("message"), JOptionPane.ERROR_MESSAGE);
                return;
            } finally {
                setCursor(this.prevCursor);
                if (doClose) {
                    closeDialog();
                } else {
                    focusPassword();
                }
            }
        } // end of while
    }

    protected void okButtonActionPerformed() {
        this.prevCursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        UiHelper.applyFontSize();
        doLogin();
    }

    private String getUserName() {
        if (this.name != null) {
            return this.name;
        }
        return this.nameField.getText();
    }

    private String getPassword() {
        if (this.password != null) {
            return this.password;
        }
        return new String(this.passwordField.getPassword());
    }

    private void storeUser() {
        this.userPrefs.put(EisConst.PREFKEY_LOGON_USER, this.nameField.getText());
    }

    /**
     * Closes the dialog
     */
    private void closeDialog() {
        setVisible(false);
        dispose();
    }
}