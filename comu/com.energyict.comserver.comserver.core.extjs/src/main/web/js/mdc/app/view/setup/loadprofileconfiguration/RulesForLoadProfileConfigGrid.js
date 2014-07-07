Ext.define('Mdc.view.setup.loadprofileconfiguration.RulesForLoadProfileConfigGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'validation-rules-for-loadprofileconfig-grid',
    itemId: 'rulesForLoadProfileConfigGrid',
    overflowY: 'auto',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.LoadProfileValidationRules',
        'Mdc.view.setup.loadprofileconfiguration.RulesForLoadProfileConfigActionMenu'
    ],

    store: 'LoadProfileValidationRules',

    deviceTypeId: null,
    deviceConfigId: null,
    loadProfileConfigId: null,

    initComponent: function () {
        var me = this;

        this.columns = [
            {
                header: Uni.I18n.translate('validation.name', 'CFG', 'Name'),
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="#/administration/validation/rulesets/validationrules/' + record.data.ruleSetId + '/ruleoverview/' + record.getId()  + '">' + value + '</a>';
                },
                flex: 1
            },
            { header: Uni.I18n.translate('validation.status', 'CFG', 'Status'), dataIndex: 'active', flex: 0.3, sortable: false, fixed: true,
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('validation.active', 'CFG', 'Active')
                    } else {
                        return Uni.I18n.translate('validation.inactive', 'CFG', 'Inactive')
                    }
                }
            },
            {
                header: Uni.I18n.translate('validation.validationRuleSet', 'CFG', 'Validation rule set'),
                dataIndex: 'ruleSetName',
                flex: 1,
                renderer: function (value, b, record) {
                    return '<a href="#/administration/validation/rulesets/overview/' + record.data.ruleSetId + '">' + value + '</a>';
                }
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.loadprofileconfiguration.RulesForLoadProfileConfigActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMsgRule', 'CFG', '{0} - {1} of {2} validation rules'),
                displayMoreMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMoreMsgRule', 'CFG', '{0} - {1} of more than {2} validation rules'),
                emptyMsg: Uni.I18n.translate('validation.pagingtoolbartop.emptyMsgRule', 'CFG', 'There are no validation rules to display')
            }/*,
             {
             xtype: 'pagingtoolbarbottom',
             params: {deviceType: me.deviceTypeId, deviceConfig: me.deviceConfigId, loadProfileConfig: me.loadProfileConfigId},
             store: me.store,
             itemsPerPageMsg: Uni.I18n.translate('validation.pagingtoolbarbottom.itemsPerPageRule', 'CFG', 'Validation rules per page'),
             dock: 'bottom'
             }  */
        ];

        me.callParent();
    }
});

