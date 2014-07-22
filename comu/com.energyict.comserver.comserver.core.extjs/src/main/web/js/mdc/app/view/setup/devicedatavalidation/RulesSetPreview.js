Ext.define('Mdc.view.setup.devicedatavalidation.RulesSetPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceDataValidationRulesSetPreview',
    itemId: 'deviceDataValidationRulesSetPreview',
    title: '',
    rulesSetId: null,
    ui: 'medium',
    padding: 0,
    requires: [
        'Mdc.view.setup.devicedatavalidation.RulesGrid',
        'Mdc.view.setup.devicedatavalidation.RulePreview'
    ],
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'deviceDataValidationRulesGrid',
                    rulesSetId: me.rulesSetId
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
                                    html: '<h4>' + Uni.I18n.translate('validation.empty.title', 'MDC', 'No validation rules found') + '</h4><br>' +
                                        Uni.I18n.translate('validation.empty.detail', 'MDC', 'There are no validation rules. This could be because:') + '<ul>' +
                                        '<li>' + Uni.I18n.translate('validation.empty.list.item1', 'MDC', 'No validation rules have been included in this validation rules set.') + '</li>' +
                                        '<li>' + Uni.I18n.translate('validation.empty.list.item2', 'MDC', 'Validation rules exists, but you do not have permission to view them.') + '</li></ul>'
                                }
                            ]
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'deviceDataValidationRulePreview'
                }
            }
        ];
        me.callParent(arguments);
    }
});