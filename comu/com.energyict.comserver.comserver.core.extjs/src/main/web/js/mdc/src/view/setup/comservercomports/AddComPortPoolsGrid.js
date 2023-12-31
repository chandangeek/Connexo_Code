/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comservercomports.AddComPortPoolsGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'add-com-port-pools-grid',
    autoScroll: false,
    store: 'Mdc.store.OutboundComPortPools',
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('comServerComPorts.pagingtoolbartop.portPoolsSelected', count, 'MDC',
            'No communication port pools selected', '1 communication port pool selected', '{0} communication port pool selected'
        );
    },

    allLabel: Uni.I18n.translate('comServerComPorts.allComPortPools', 'MDC', 'All communication port pools'),
    allDescription: Uni.I18n.translate('general.selectAllItems', 'MDC', 'Select all items (related to filters on previous screen)'),

    selectedLabel: Uni.I18n.translate('comServerComPorts.selectedComPortPools', 'MDC', 'Selected communication port pools'),
    selectedDescription: Uni.I18n.translate('general.selectItemsInTable', 'MDC', 'Select items in table'),

    cancelHref: '#/search',

    columns: [
        {
            header: Uni.I18n.translate('general.comPortPool', 'MDC', 'Communication port pool'),
            dataIndex: 'name',
            flex: 3
        },
        {
            header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
            dataIndex: 'active',
            flex: 1,
            renderer: function (value) {
                if (value === true) {
                    return Uni.I18n.translate('general.active', 'MDC', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                }
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        me.getTopToolbarContainer().add(
            {
                xtype: 'component',
                html: '&nbsp;',
                flex: 1
            },
            {
                xtype: 'button',
                border: 0,
                ui: 'link',
                text: Uni.I18n.translate('comServerComPorts.addPools.,manageComPorts', 'MDC', 'Manage communication port pools'),
                handler: function (button, event) {
                    window.open('#/administration/comportpools');
                }
            });
    },

    updateCancelHref: function (comServerId, comportId) {
        var href = '#/administration/comservers/' + Ext.String.htmlEncode(comServerId) + '/comports/';
        if (comportId !== undefined) {
            href += 'outbound/' + comportId + '/edit';
        } else {
            href += 'add/outbound';
        }

        if (this.rendered) {
            this.getCancelButton().setHref(href);
        } else {
            this.cancelHref = href;
        }
    }
});