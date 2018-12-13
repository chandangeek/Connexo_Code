package com.energyict.cim;

/**
 * Created by IntelliJ IDEA.
 * User: jbr
 * Date: 16-aug-2011
 * Time: 16:25:05
 */
public enum EndDeviceEventOrAction  implements CimMnemonicProvider {
    NOT_APPLICABLE(0, "NotApplicable"),
    ABORTED(1, "Aborted"),
    ACCESSED(2, "Accessed"),
    ACKNOWLEDGED(3, "Acknowledged"),
    ACTIVATED(4, "Activated"),
    ARMFORCLOSURE(5, "ArmForClosure"),
    ARMFOROPEN(6, "ArmForOpen"),
    ATTEMPTED(7, "Attempted"),
    CANCEL(8, "Cancel"),
    CANCELLED(10, "Cancelled"),
    ARMEDFORCLOSURE(11, "ArmedForClosure"),
    ARMEDFOROPEN(12, "ArmedForOpen"),
    CHANGE(13, "Change"),
    CHANGEPENDING(14, "ChangePending"),
    CHARGED(15, "Charged"),
    CLOSED(16, "Closed"),
    CONFIRMED(17, "Confirmed"),
    CONNECT(18, "Connect"),
    DEACTIVATED(19, "Deactivated"),
    DELAYED(20, "Delayed"),
    CALCULATED(21, "Calculated"),
    DISABLE(22, "Disable"),
    DISCONNECT(23, "Disconnect"),
    CHANGED(24, "Changed"),
    DOWNLOADED(25, "Downloaded"),
    ENABLE(26, "Enable"),
    CHANGEOUT(27, "ChangeOut"),
    CLEARED(28, "Cleared"),
    ESTABLISHED(29, "Established"),
    EXECUTE(30, "Execute"),
    COLDSTARTED(31, "ColdStarted"),
    EXPIRED(31, "Expired"),
    FULL(32, "Full"),
    INITIALIZED(33, "Initialized"),
    INPROGRESS(34, "InProgress"),
    INVALID(35, "Invalid"),
    LOADED(36, "Loaded"),
    NORMAL(37, "Normal"),
    NOTAUTHORIZED(38, "NotAuthorized"),
    OPENED(39, "Opened"),
    OUTOFRANGE(40, "OutofRange"),
    PREEMPTED(41, "Preempted"),
    CONNECTED(42, "Connected"),
    CORRUPTED(43, "Corrupted"),
    PROCESSED(44, "Processed"),
    CROSSPHASEDETECTED(45, "CrossPhaseDetected"),
    READ(46, "Read"),
    LOSSDETECTED(47, "LossDetected"),
    READY(48, "Ready"),
    REESTABLISHED(49, "Re-established"),
    REGISTERED(50, "Registered"),
    RELEASED(51, "Released"),
    REPLACED(52, "Replaced"),
    RESTARTED(53, "Restarted"),
    START(54, "Start"),
    STOP(55, "Stop"),
    SUBSTITUTED(56, "Substituted"),
    DECREASED(57, "Decreased"),
    SUCCEEDED(58, "Succeeded"),
    TERMINATED(59, "Terminated"),
    UPLOADED(60, "Uploaded"),
    UNINITIALIZED(61, "Uninitialized"),
    UNLOCKED(62, "Unlocked"),
    UNSECURE(63, "Unsecure"),
    RESETFAILED(65, "ResetFailed"),
    DISABLED(66, "Disabled"),
    CONNECTFAILED(67, "ConnectFailed"),
    DISCONNECTED(68, "Disconnected"),
    HIGHDISTORTION(69, "HighDistortion"),
    CROSSPHASECLEARED(70, "CrossPhaseCleared"),
    HIGHDISTORTIONCLEARED(71, "HighDistortionCleared"),
    INACTIVECLEARED(72, "InactiveCleared"),
    MAXLIMITREACHEDCLEARED(73, "MaxLimitReachedCleared"),
    OUTOFRANGECLEARED(74, "OutofRangeCleared"),
    IMBALANCECLEARED(75, "ImbalanceCleared"),
    ENABLED(76, "Enabled"),
    DISPLAY(77, "Display"),
    DISPLAYED(78, "Displayed"),
    ERROR(79, "Error"),
    OPTEDIN(80, "Opted-In"),
    OPTEDOUT(81, "Opted-Out"),
    CREATE(82, "Create"),
    CREATED(83, "Created"),
    CREATEFAILED(84, "CreateFailed"),
    DISCONNECTFAILED(84, "DisconnectFailed"),
    FAILED(85, "Failed"),
    CANCELFAILED(86, "CancelFailed"),
    DISPLAYFAILED(87, "DisplayFailed"),
    FROZEN(88, "Frozen"),
    DISTORTED(91, "Distorted"),
    MAXLIMITREACHED(93, "MaxLimitReached"),
    IMBALANCED(98, "Imbalanced"),
    INACTIVE(100, "Inactive"),
    INCREASED(102, "Increased"),
    INSTALLED(105, "Installed"),
    EXCEEDED(139, "Exceeded"),
    MINLIMITREACHED(150, "MinLimitReached"),
    MISMATCHED(159, "Mismatched"),
    NOTFOUND(160, "NotFound"),
    DISALLOWED(161, "Disallowed"),
    OVERFLOW(177, "Overflow"),
    REMOVED(212, "Removed"),
    REPROGRAMMED(213, "Reprogrammed"),
    RESET(214, "Reset"),
    RESETOCCURRED(215, "ResetOccurred"),
    RESTORED(216, "Restored"),
    STARTFAILED(217, "StartFailed"),
    STOPFAILED(218, "StopFailed"),
    REVERSED(219, "Reversed"),
    DISABLEFAILED(220, "DisableFailed"),
    ENABLEFAILED(221, "EnableFailed"),
    ARMFOROPENFAILED(222, "ArmForOpenFailed"),
    SAGSTARTED(223, "SagStarted"),
    SAGSTOPPED(224, "SagStopped"),
    SCHEDULED(225, "Scheduled"),
    ARMFORCLOSUREFAILED(226, "ArmForClosureFailed"),
    SEALED(227, "Sealed"),
    STARTED(242, "Started"),
    STOPPED(243, "Stopped"),
    SWELLSTARTED(248, "SwellStarted"),
    SWELLSTOPPED(249, "SwellStopped"),
    TAMPERDETECTED(257, "TamperDetected"),
    TILTED(263, "Tilted"),
    UNSEALED(269, "Unsealed"),
    UNSTABLE(270, "Unstable"),
    WARMSTARTED(278, "WarmStarted");


    private int value;
    private String mnemonic;

    private EndDeviceEventOrAction(int value, String mnemonic) {
        this.value = value;
        this.mnemonic = mnemonic;
    }

    public int getValue() {
        return value;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String getTranslationKey() {
        return "ea" + mnemonic;
    }

    public static EndDeviceEventOrAction fromValue(int value) {
        for (EndDeviceEventOrAction ea : EndDeviceEventOrAction.values()) {
            if (ea.getValue() == value) {
                return ea;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getMnemonic();
    }

}
