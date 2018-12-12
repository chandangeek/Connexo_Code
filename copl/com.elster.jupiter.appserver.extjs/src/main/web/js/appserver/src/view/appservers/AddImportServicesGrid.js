/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.AddImportServicesGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-import-services-grid',

    plugins: [
        {
            ptype: 'bufferedrenderer',
        }
    ],

    counterTextFn: function (count){
        return Uni.I18n.translatePlural('general.nrOfImportServices.selected', count, 'APR',
            'No import services selected', '{0} import service selected', '{0} import services selected'
        );
    },
    bottomToolbarHidden: true,
    checkAllButtonPresent: true,

    columns: [
        {
            header: Uni.I18n.translate('general.importService', 'APR', 'Import service'),
            dataIndex: 'importService',
            flex: 1
        }
    ]
});
