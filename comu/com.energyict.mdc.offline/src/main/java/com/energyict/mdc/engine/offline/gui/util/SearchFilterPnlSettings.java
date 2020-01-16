package com.energyict.mdc.engine.offline.gui.util;

public class SearchFilterPnlSettings {

    private boolean disabledTypeCombo;
    private boolean noCustomProps;
    private boolean browseMode;
    private boolean showMaxResults;
    private boolean newLook;

    // only used when newLook is true:
    private boolean advancedSettingsPaneCollapsed;
    private boolean advancedSettingsPaneCollapsible;
    private boolean timeSettingsPaneCollapsed;
    private boolean timeSettingsPaneCollapsible;

    public SearchFilterPnlSettings() {
        // Default settings:
        disabledTypeCombo = false;
        noCustomProps = false;
        newLook = false;
        browseMode = false;
        showMaxResults = true;
        advancedSettingsPaneCollapsed = true;
        advancedSettingsPaneCollapsible = true;
        timeSettingsPaneCollapsed = true;
        timeSettingsPaneCollapsible = true;
    }


    public boolean isDisabledTypeCombo() {
        return disabledTypeCombo;
    }


    public void setDisabledTypeCombo(boolean disabledTypeCombo) {
        this.disabledTypeCombo = disabledTypeCombo;
    }


    public boolean isNoCustomProps() {
        return noCustomProps;
    }


    public void setNoCustomProps(boolean noCustomProps) {
        this.noCustomProps = noCustomProps;
    }


    public boolean isNewLook() {
        return newLook;
    }


    public void setNewLook(boolean newLook) {
        this.newLook = newLook;
    }


    public boolean isBrowseMode() {
        return browseMode;
    }


    public void setBrowseMode(boolean browseMode) {
        this.browseMode = browseMode;
    }


    public boolean isShowMaxResults() {
        return showMaxResults;
    }


    public void setShowMaxResults(boolean showMaxResults) {
        this.showMaxResults = showMaxResults;
    }


    public boolean isAdvancedSettingsPaneCollapsed() {
        return advancedSettingsPaneCollapsed;
    }


    public void setAdvancedSettingsPaneCollapsed(boolean advancedSettingsPaneCollapsed) {
        this.advancedSettingsPaneCollapsed = advancedSettingsPaneCollapsed;
    }


    public boolean isAdvancedSettingsPaneCollapsible() {
        return advancedSettingsPaneCollapsible;
    }


    public void setAdvancedSettingsPaneCollapsible(
            boolean advancedSettingsPaneCollapsible) {
        this.advancedSettingsPaneCollapsible = advancedSettingsPaneCollapsible;
    }


    public boolean isTimeSettingsPaneCollapsed() {
        return timeSettingsPaneCollapsed;
    }


    public void setTimeSettingsPaneCollapsed(boolean timeSettingsPaneCollapsed) {
        this.timeSettingsPaneCollapsed = timeSettingsPaneCollapsed;
    }


    public boolean isTimeSettingsPaneCollapsible() {
        return timeSettingsPaneCollapsible;
    }


    public void setTimeSettingsPaneCollapsible(boolean timeSettingsPaneCollapsible) {
        this.timeSettingsPaneCollapsible = timeSettingsPaneCollapsible;
    }

}
