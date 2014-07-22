Ext.define('Mdc.controller.setup.DeviceDataValidation', {
    extend: 'Ext.app.Controller',
    mRID: null,
    requires: [
        'Mdc.store.DeviceDataValidationRulesSet',
        'Cfg.store.ValidationRules'
    ],
    stores: [
        'DeviceDataValidationRulesSet',
        'Cfg.store.ValidationRules'
    ],
    views: [
        'Mdc.view.setup.devicedatavalidation.RulesSetMainView'
    ],
    refs: [
        {ref: 'rulesSetGrid', selector: '#deviceDataValidationRulesSetGrid'},
        {ref: 'rulesSetPreviewCt', selector: '#deviceDataValidationRulesSetPreviewCt'},
        {ref: 'rulesGrid', selector: '#deviceDataValidationRulesGrid'},
        {ref: 'rulePreview', selector: '#deviceDataValidationRulePreview'},
        {ref: 'changeRuleSetStateActionMenuItem', selector: '#changeRuleSetStateActionMenuItem'}
    ],
    init: function () {
        this.control({
            '#deviceDataValidationRulesSetGrid': {
                afterrender: this.onRulesSetGridAfterRender,
                itemclick: this.onRulesSetGridItemClick,
                selectionchange: this.onRulesSetGridSelectionChange
            },
            '#deviceDataValidationRulesGrid': {
                selectionchange: this.onRulesGridSelectionChange
            },
            '#changeRuleSetStateActionMenuItem': {
                click: this.changeRuleSetState
            }
        });
        this.callParent();
    },
    showDeviceDataValidationMainView: function (mRID) {
        this.mRID = mRID;
        var widget = Ext.widget('deviceDataValidationRulesSetMainView', {
            mRID: this.mRID,
//            status: this.status
            status: 'Inactive'
        });
        this.getApplication().fireEvent('changecontentevent', widget);
    },
    onRulesSetGridAfterRender: function () {
        var me = this;
        me.getRulesSetGrid().getStore().load({
            callback: function () {
                me.getRulesSetGrid().getSelectionModel().doSelect(0);
            }
        });
    },
    onRulesSetGridSelectionChange: function (sm, selection, e) {
        this.getRulesSetPreviewCt().removeAll(true);
        var rulesSetPreview = Ext.widget('deviceDataValidationRulesSetPreview', {
            rulesSetId: selection[0].get('id'),
            title: selection[0].get('name')
        });
        this.getRulesGrid().getStore().suspendEvents(false);
        this.renderPreviewComponent(rulesSetPreview);
    },
    renderPreviewComponent: function (rulesSetPreview) {
        this.getRulesGrid().getStore().resumeEvents();
        var me = this;
        me.getRulesSetPreviewCt().setLoading(true);
        me.getRulesGrid().getStore().load({
            id: rulesSetPreview.rulesSetId,
            callback: function () {
                me.getRulesSetPreviewCt().add(rulesSetPreview);
                me.getRulesGrid().getSelectionModel().doSelect(0);
                me.getRulesSetPreviewCt().setLoading(false);
            }
        });
    },
    onRulesGridSelectionChange: function (sm, selection, e) {
        this.getRulePreview().updateValidationRule(selection[0]);
    },
    onRulesSetGridItemClick: function (gridView, record, el, idx, e) {
        var target = e.getTarget(null, null, true);
        if (target.hasCls('x-action-col-icon')) {
            var menuItem = this.getChangeRuleSetStateActionMenuItem();
            menuItem.setText(this.isRulesSetActive(record) ?
                Uni.I18n.translate('general.deactivate', 'MDC', 'Deactivate') :
                Uni.I18n.translate('general.activate', 'MDC', 'Activate'))
        }
    },
    changeRuleSetState: function () {
        var record = this.getRulesSetGrid().getSelectionModel().getLastSelected();
        record.set('status', this.isRulesSetActive(record) ? 'Inactive' : 'Active');
    },
    isRulesSetActive: function (record) {
        return record.get('status') === 'Active';
    }
});