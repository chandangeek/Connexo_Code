/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.webservices.AddWebServiceGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-web-service-grid',
    checkAllButtonPresent: true,
    
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('webService.counterText', count, 'UNI',
            'No web services selected', '{0} web service selected', '{0} web services selected'
        );
    },

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('webService.title.endpoint', 'UNI', 'Web service endpoint'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'UNI', 'Type'),
                dataIndex: 'direction',
                flex: 1
            }
        ];

        me.callParent(arguments);
    }
});
