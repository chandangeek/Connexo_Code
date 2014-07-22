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
            'rule-device-configuration-add radiogroup': {
                change: this.onChangeRadio
            },
            'rule-device-configuration-action-menu': {
                click: this.chooseAction
            }
//            'ruleDeviceConfigurationBrowse button[action=addDeviceConfiguration]': {
//                click: this.onAddClick
//            }
        });
        this.listen({
            store: {
                '#Mdc.store.RuleDeviceConfigurationsNotLinked': {
                    load: this.banDefaultSelection
                }
            }
        });
    },


    banDefaultSelection: function () {
        var gridview = Ext.ComponentQuery.query('rule-device-configuration-add grid')[0];
        if (gridview.getStore().getCount() > 0) {
            gridview.getSelectionModel().deselectAll();
        }
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
    },

    changeRadioFromAllToSelected: function () {
        var radioAll = this.getRuleDeviceConfigurationAddPanel().down('#radioAll'),
            radioSelected = this.getRuleDeviceConfigurationAddPanel().down('#radioSelected');
        if (radioAll.getValue()) {
            radioAll.setValue(false);
            radioSelected.setValue(true);
        }
    },

    onUncheckAll: function () {
        var grid = this.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigGrid');
        grid.getView().getSelectionModel().deselectAll();
        this.changeRadioFromAllToSelected();
    },

    onChangeRadio: function (radiogroup, newValue, oldValue) {
        var grid = this.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigGrid');
        switch (newValue.configsRadio) {
            case 'ALL':
                Ext.ComponentQuery.query('rule-device-configuration-add grid')[0].getView().setDisabled(true);
                this.getRuleDeviceConfigurationAddPanel().down('#countLabel').setText(Uni.I18n.translate('deviceconfiguration.selectedAll', 'MDC', 'All device configurations selected'));
                break;
            case 'SELECTED':
                if (Ext.ComponentQuery.query('rule-device-configuration-add grid')[0].getView().isDisabled()) {
                    Ext.ComponentQuery.query('rule-device-configuration-add grid')[0].getView().setDisabled(false);
                }
                var selection = Ext.ComponentQuery.query('rule-device-configuration-add grid')[0].getSelectionModel().getSelection(),
                    selectionText = Uni.I18n.translatePlural(
                        'validation.deviceconfiguration.selection',
                        selection.length,
                        'CFG',
                        '{0} device configurations selected'
                    );
                this.getRuleDeviceConfigurationAddPanel().down('#countLabel').setText(selectionText);
                break;
        }
    },

    addDeviceConfigurations: function () {
        var me = this,
            grid = me.getRuleDeviceConfigurationAddPanel().down('#addDeviceConfigGrid'),
            selection = grid.view.getSelectionModel().getSelection(),
            url = '/api/dtc/validationruleset/' + me.ruleSetId + '/deviceconfigurations',
            ids = [];
        Ext.Array.each(selection, function (item) {
            ids.push(item.data.config.id);
        });
        me.getRuleDeviceConfigurationAddPanel().setLoading();
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            timeout: 120000,
            params: {
                all: me.allPressed
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

