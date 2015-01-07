Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeEditForm',
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesGrid'
    ],
    alias: 'widget.load-profile-type-edit',
    currentRoute: null,

    initComponent: function () {
        var me = this;

        me.content = {
            itemId: 'load-profile-type-edit',
            layout: 'card',
            items: [
                {
                    xtype: 'load-profile-type-edit-form',
                    itemId: 'load-profile-type-edit-form',
                    ui: 'large'
                },
                {
                    xtype: 'load-profile-type-add-measurement-types-grid',
                    itemId: 'load-profile-type-add-measurement-types-grid',
                    ui: 'large',
                    title: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesView.title', 'MDC', 'Add register types'),
                    cancelHref: me.currentRoute
                }
            ]
        };

        me.callParent(arguments);
    }
});