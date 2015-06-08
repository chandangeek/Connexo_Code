Ext.define('Mdc.view.setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailRulesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.load-profile-configuration-detail-rules-grid',
    overflowY: 'auto',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.validation.RuleActionMenu',
        'Mdc.store.ChannelConfigValidationRules'
    ],
    store: 'Mdc.store.ChannelConfigValidationRules',
    itemId: 'loadProfileConfigurationDetailRulesGrid',
    deviceTypeId: null,
    deviceConfigId: null,
    channelConfigId: null,
    privileges: Cfg.privileges.Validation.fineTuneOnDeviceConfiguration,
    initComponent: function () {
        var me = this;
        me.columns = [
            { header: Uni.I18n.translate('validation.validationRule', 'CFG', 'Validation rule'), dataIndex: 'name', flex: 1,
                renderer: function (value, b, record) {					
					return '<a href="#/administration/validation/rulesets/' + record.get('ruleSetId') 
                        + '/versions/' + record.get('ruleSetVersionId') 
                        + '/rules/' + record.getId() + '">' +  Ext.String.htmlEncode(value) + '</a>';
                }
            },
            { header: Uni.I18n.translate('validation.status', 'CFG', 'Status'), dataIndex: 'active', flex: 1,
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
                renderer: function (value, metaData, record) {
					var ruleSetVersion = record.get('ruleSetVersion');
					if (ruleSetVersion){
						var ruleSet = ruleSetVersion.ruleSet;
						if (ruleSet){
							metaData.tdAttr = 'data-qtip="' + ruleSet.description + '"';
							return '<a href="#/administration/validation/rulesets/' + ruleSet.id + '">' + Ext.String.htmlEncode(ruleSet.name) + '</a>';
						}
					}
                    
                    return '';
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                menu: {
                    xtype: 'validation-rule-actionmenu'
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'toolbar',
                dock: 'top',
                items: [
                    {
                        xtype: 'container',
                        flex: 1,
                        items: [
                            {
                                xtype: 'pagingtoolbartop',
                                store: me.store,
                                displayMsg: Uni.I18n.translate('rule.display.msg', 'MDC', '{0} - {1} of {2} validation rules'),
                                displayMoreMsg: Uni.I18n.translate('rule.display.more.msg', 'MDC', '{0} - {1} of more than {2} validation rules'),
                                emptyMsg: Uni.I18n.translate('rule.pagingtoolbartop.emptyMsg', 'MDC', 'There are no validation rules to display'),
                                dock: 'top',
                                border: false
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemsPerPageMsg: Uni.I18n.translate('rule.items.per.page.msg', 'MDC', 'Validation rules per page'),
                store: me.store,
                dock: 'bottom',
                deferLoading: true,
                pageSizeParam: 'limit2',
                pageStartParam: 'start2',
                params: {
                    deviceType: me.deviceTypeId,
                    deviceConfig: me.deviceConfigId,
                    channelConfig: me.channelConfigId
                }
            }
        ];
        me.callParent(arguments);
    }
});

