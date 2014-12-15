Ext.define('Mdc.view.setup.devicegroup.PreviewForm', {
    extend: 'Ext.form.Panel',
    xtype: 'devicegroups-preview-form',

    border: false,
    itemId: 'deviceGroupPreviewForm',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        labelWidth: 150
    },

    items: [
                {
                            xtype: 'displayfield',
                            name: 'name',
                            fieldLabel: Uni.I18n.translate('devicegroup.name', 'MDC', 'Name'),
                            itemId: 'deviceGroupName'

                },
                {
                            xtype: 'displayfield',
                            name: 'dynamic',
                            fieldLabel: Uni.I18n.translate('devicegroup.type', 'MDC', 'Type'),
                            renderer: function (value) {
                                if (value) {
                                    return Uni.I18n.translate('devicegroup.dynamic', 'MDC', 'Dynamic')
                                } else {
                                    return Uni.I18n.translate('devicegroup.static', 'MDC', 'Static')
                                }
                            }
                },
                {
                            xtype: 'fieldcontainer',
                            columnWidth: 0.5,
                            fieldLabel: Uni.I18n.translate('deviceGroup.searchCriteria', 'MDC', 'Search criteria'),
                            labelAlign: 'right',
                            layout: {
                                type: 'vbox'
                            },
                            itemId: 'searchCriteriaContainer',
                            items: [

                            ]
                }

      ]

});
