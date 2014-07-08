Ext.define('Cfg.view.deviceconfiguration.RuleDeviceConfigurationBrowse', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ruleDeviceConfigurationBrowse',
    requires: [
        'Cfg.view.deviceconfiguration.RuleDeviceConfigurationGrid',
        'Cfg.view.deviceconfiguration.RuleDeviceConfigurationPreview',
        'Cfg.view.deviceconfiguration.RuleDeviceConfigurationActionMenu',
        'Uni.view.container.PreviewContainer',
        'Cfg.view.validation.RuleSetSubMenu'
    ],
    ruleSetId: null,

    initComponent: function () {
        var me = this;
        this.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('validation.deviceConfigurations', 'CFG', 'Device configurations'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'rule-device-configuration-grid',
                            ruleSetId: me.ruleSetId
                        },
                        emptyComponent: {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'left'
                            },
                            minHeight: 20,
                            items: [
                                {
                                    xtype: 'image',
                                    margin: '0 10 0 0',
                                    src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                    height: 20,
                                    width: 20
                                },
                                {
                                    xtype: 'container',
                                    items: [
                                        {
                                            xtype: 'component',
                                            html: '<h4>' + Uni.I18n.translate('validation.empty.deviceconfiguration.title', 'CFG', 'No device configurations found') + '</h4><br>' +
                                                Uni.I18n.translate('validation.empty.deviceconfiguration.detail', 'CFG', 'There are no device configurations. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('validation.empty.deviceconfiguration.list.item1', 'CFG', 'No device configurations have been added yet.') + '</li></lv><br>' +
                                                Uni.I18n.translate('validation.empty.steps', 'CFG', 'Possible steps:')
                                        },
                                        {
                                            xtype: 'button',
                                            text: Uni.I18n.translate('validation.deviceconfiguration.add', 'CFG', 'Add device configuration'),
                                            itemId: 'addDeviceConfiguration',
                                            margin: '10 0 0 0',
                                            ui: 'action',
                                            listeners: {
                                                click: {
                                                    fn: function () {
                                                        me.setLoading();
                                                        window.location.href = '#/administration/validation/rulesets/' + me.ruleSetId + '/deviceconfigurations/add';
                                                    }
                                                }
                                            }
                                        }
                                    ]
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'rule-device-configuration-preview'
                        }
                    }
                ]
            }
        ];
        this.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRuleSet', 'CFG', 'Validation rule set'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                ui: 'medium',
                items: [
                    {
                        xtype: 'ruleSetSubMenu',
                        itemId: 'stepsMenu',
                        ruleSetId: me.ruleSetId,
                        toggle: 2
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});


