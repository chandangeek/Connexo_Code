Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeAddMeasurementTypesView',
    itemId: 'loadProfileTypeAddMeasurementTypesView',

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesView.title', 'MDC', 'Add measurement types'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'loadProfileTypeAddMeasurementTypesGrid'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

