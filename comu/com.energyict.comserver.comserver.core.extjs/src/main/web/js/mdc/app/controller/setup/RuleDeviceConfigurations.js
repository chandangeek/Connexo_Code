Ext.define('Mdc.controller.setup.RuleDeviceConfigurations', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.RuleDeviceConfigurations',
        'Cfg.store.ValidationRuleSets',
        'Mdc.store.RuleDeviceConfigurationsNotLinked'
    ],

    models: [
        'Mdc.model.RuleDeviceConfiguration'
    ],

    views: [
        'setup.ruledeviceconfiguration.RuleDeviceConfigurationBrowse',
        'setup.ruledeviceconfiguration.RuleDeviceConfigurationGrid',
        'setup.ruledeviceconfiguration.RuleDeviceConfigurationAdd',
        'setup.ruledeviceconfiguration.RuleDeviceConfigurationPreview',
        'setup.ruledeviceconfiguration.RuleDeviceConfigurationAddGrid',
        'setup.ruledeviceconfiguration.RuleAddDeviceConfigurationActionMenu'
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
                selectionchange: this.countSelectedDeviceConfigurations
            },
            'rule-device-configuration-add button[action=add]': {
                click: this.addDeviceConfigurations
            },
            'rule-device-configuration-add button[action=uncheck]': {
                click: this.onUncheckAll
            },
            'rule-device-configuration-action-menu': {
                click: this.chooseAction
            },
            'rule-device-configuration-add radiogroup': {
                change: this. onChangeRadio
            }
        });
    },

    onChangeRadio: function () {
        var grid = Ext.ComponentQuery.query('rule-device-configuration-add grid')[0];
        grid.getSelectionModel().getSelection().length === 0 ? grid.down('#uncheckAll').setDisabled(true) : grid.down('#uncheckAll').setDisabled(false);
        this.getRuleDeviceConfigurationAddPanel().down('#radioAll').getValue() && grid.down('#uncheckAll').setDisabled(true);
    },

    showDeviceConfigView: function (ruleSetId) {
        var me = this,
            ruleDeviceConfigStore = me.getStore('Mdc.store.RuleDeviceConfigurations'),
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
            ruleDeviceConfigNotLinkedStore = me.getStore('Mdc.store.RuleDeviceConfigurationsNotLinked'),
            ruleSetsStore = me.getStore('Cfg.store.ValidationRuleSets'),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('rule-device-configuration-add', {ruleSetId: router.routeparams.ruleSetId});
        me.ruleSetId = router.routeparams.ruleSetId;
        if (widget.down('#addDeviceConfigGrid')) {
            widget.down('#addDeviceConfigGrid').getStore().removeAll();
        }
        me.getApplication().fireEvent('changecontentevent', widget);
        ruleDeviceConfigNotLinkedStore.getProxy().setExtraParam('ruleSetId', router.routeparams.ruleSetId);
        ruleDeviceConfigNotLinkedStore.load(function () {
            ruleSetsStore.load({
                params: {
                    id: router.routeparams.ruleSetId
                },
                callback: function () {
                    var ruleSet = ruleSetsStore.getById(parseInt(router.routeparams.ruleSetId));
                    me.getApplication().fireEvent('loadRuleSet', ruleSet);
                }
            });
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
            radioSelected = me.getRuleDeviceConfigurationAddPanel().down('#radioSelected');

        var gridview = Ext.ComponentQuery.query('rule-device-configuration-add grid')[0],
            selection = gridview.getSelectionModel().getSelection(),
            selectionText = Uni.I18n.translatePlural(
                'validation.deviceconfiguration.selection',
                selection.length,
                'CFG',
                '{0} device configurations selected'
            );

        textLabel.setText(selectionText);
        me.changeRadioFromAllToSelected();
        selection.length === 0 ? gridview.down('#uncheckAll').setDisabled(true) : gridview.down('#uncheckAll').setDisabled(false);
    },

    changeRadioFromAllToSelected: function () {
        var radioAll = this.getRuleDeviceConfigurationAddPanel().down('#radioAll'),
            radioSelected = this.getRuleDeviceConfigurationAddPanel().down('#radioSelected');

        radioAll.setValue(false);
        radioSelected.setValue(true);
    },

    onUncheckAll: function () {
        var grid = this.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigGrid');
        grid.getView().getSelectionModel().deselectAll(true);
    },

    addDeviceConfigurations: function () {
        var me = this,
            allPressed = me.getRuleDeviceConfigurationAddPanel().down('#radioAll').getValue(),
            grid = me.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigGrid'),
            selection = grid.view.getSelectionModel().getSelection(),
            url = '/api/dtc/validationruleset/' + me.ruleSetId + '/deviceconfigurations',
            ids = [];

        if (!allPressed) {
            Ext.Array.each(selection, function (item) {
                ids.push(item.data.config.id);
            });
        }

        me.getRuleDeviceConfigurationAddPanel().setLoading();
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            timeout: 120000,
            params: {
                all: allPressed
            },
            jsonData: Ext.encode(ids),
            success: function () {
                location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/deviceconfigurations';
                var message = Uni.I18n.translatePlural(
                    'validation.deviceconfiguration.addSuccess',
                    selection.length,
                    'CFG',
                    'Succesfully added device configurations'
                );
                me.getApplication().fireEvent('acknowledge', message);
            },
            callback: function () {
                me.getRuleDeviceConfigurationAddPanel().setLoading(false);
            }
        });
    },

    chooseAction: function (menu, item) {
        var record;
        record = menu.record || this.getRuleDeviceConfigurationBrowsePanel().down('rule-device-configuration-grid').getSelectionModel().getLastSelected();
        location.href = '#/administration/devicetypes/' + record.data.deviceType.id + '/deviceconfigurations/' + record.data.config.id;
    }
});

