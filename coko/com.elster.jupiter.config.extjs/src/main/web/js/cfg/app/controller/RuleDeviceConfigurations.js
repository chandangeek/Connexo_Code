Ext.define('Cfg.controller.RuleDeviceConfigurations', {
    extend: 'Ext.app.Controller',

    stores: [
        'Cfg.store.RuleDeviceConfigurations',
        'Cfg.store.ValidationRuleSets',
        'Cfg.store.RuleDeviceConfigurationsNotLinked'
    ],

    requires: [
    ],

    models: [
        'Cfg.model.RuleDeviceConfiguration'
    ],

    views: [
        'deviceconfiguration.RuleDeviceConfigurationBrowse',
        'deviceconfiguration.RuleDeviceConfigurationGrid',
        'deviceconfiguration.RuleDeviceConfigurationAdd'
    ],

    refs: [
        {ref: 'ruleDeviceConfigurationBrowsePanel', selector: 'ruleDeviceConfigurationBrowse'},
        {ref: 'ruleDeviceConfigurationAddPanel', selector: 'rule-device-configuration-add'}
    ],

    ruleSetId: null,

    init: function () {
        this.control({
            'rule-device-configuration-grid': {
                select: this.loadDetails
            },
            'rule-device-configuration-add grid': {
                afterrender: this.selectFirstItem,
                selectionchange: this.countSelectedDeviceConfigurations
            },
            'rule-device-configuration-add button[action=add]': {
                click: this.addDeviceConfigurations
            },
            'rule-device-configuration-add button[action=uncheck]': {
                click: this.onUncheckAll
            },
            'rule-device-configuration-add radiogroup': {
                change: this.onChangeRadio
            }
        });
    },

    showDeviceConfigView: function (ruleSetId) {
        var me = this,
            ruleDeviceConfigStore = me.getStore('Cfg.store.RuleDeviceConfigurations'),
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets');
        ruleDeviceConfigStore.getProxy().setExtraParam('ruleSetId', ruleSetId);
        me.ruleSetId = ruleSetId;
        ruleSetsStore.load({
            params: {
                id: ruleSetId
            },
            callback: function () {
                var ruleSet = ruleSetsStore.getById(parseInt(ruleSetId)),
                    widget = Ext.widget('ruleDeviceConfigurationBrowse', {ruleSetId: ruleSetId});
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#stepsMenu').setTitle(ruleSet.get('name'));
                me.getApplication().fireEvent('loadRuleSet', ruleSet);
            }
        });
    },

    showAddDeviceConfigView: function () {
        var me = this,
            ruleDeviceConfigNotLinkedStore = me.getStore('Cfg.store.RuleDeviceConfigurationsNotLinked'),
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets');
        ruleDeviceConfigNotLinkedStore.getProxy().setExtraParam('ruleSetId', me.ruleSetId);
        ruleDeviceConfigNotLinkedStore.load({
            callback: function () {
                var widget = Ext.widget('rule-device-configuration-add', {ruleSetId: me.ruleSetId});
                me.getApplication().fireEvent('changecontentevent', widget);
                var textLabel = me.getRuleDeviceConfigurationAddPanel().down('#countLabel'),
                    grid = me.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigGrid');
                textLabel.setText('No configurations selected');
                if (this.getCount() < 1) {
                    grid.hide();
                    me.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigToolbar').hide();
                    grid.next().show();
                }
                ruleSetsStore.load({
                    params: {
                        id: me.ruleSetId
                    },
                    callback: function () {
                        var ruleSet = ruleSetsStore.getById(parseInt(me.ruleSetId));
                        widget.down('#stepsMenu').setTitle(ruleSet.get('name'));
                        me.getApplication().fireEvent('loadRuleSet', ruleSet);
                    }
                });
            }
        });
    },

    loadDetails: function (rowModel, record) {
        var me = this,
            itemForm = me.getRuleDeviceConfigurationBrowsePanel().down('rule-device-configuration-preview');
        itemForm.loadRecord(record);
        itemForm.setTitle(record.get('config_name'));
    },

    countSelectedDeviceConfigurations: function (grid) {
        var me = this,
            textLabel = me.getRuleDeviceConfigurationAddPanel().down('#countLabel'),
            chosenDeviceConfigCount = grid.view.getSelectionModel().getSelection().length;
        console.log(chosenDeviceConfigCount);
        textLabel.setText(chosenDeviceConfigCount >= 1 ? (chosenDeviceConfigCount + (chosenDeviceConfigCount > 1 ? ' configurations' : ' configuration') + ' selected') : Uni.I18n.translate('validation.deviceconfiguration.noSelected', 'CFG', 'No configurations selected'));
    },

    onUncheckAll: function () {
        var grid = this.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigGrid'),
            radioAll = this.getRuleDeviceConfigurationAddPanel().down('#radioAll'),
            radioSelected = this.getRuleDeviceConfigurationAddPanel().down('#radioSelected');
        grid.getView().getSelectionModel().deselectAll();
        if (radioAll.getValue()) {
            radioAll.setValue(false);
            radioSelected.setValue(true);
        }
    },

    onChangeRadio: function (radiogroup, newValue, oldValue) {
        var grid = this.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigGrid');
        switch (newValue.configsRadio) {
            case 'ALL':
                grid.getSelectionModel().selectAll(true);
                grid.fireEvent('selectionchange', grid);
        }
    },

    selectFirstItem: function (grid) {
        var gridView = grid.getView(),
            selectionModel = gridView.getSelectionModel();
        selectionModel.select(0);
        grid.fireEvent('selectionchange', grid);
        this.getRuleDeviceConfigurationAddPanel().down('#countLabel').setText(Uni.I18n.translate('validation.deviceconfiguration.oneSelected', 'CFG', '1 configuration selected'))
    },

    addDeviceConfigurations: function () {

    }
});
