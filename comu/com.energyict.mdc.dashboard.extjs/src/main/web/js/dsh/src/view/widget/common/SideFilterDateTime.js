/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.common.SideFilterDateTime', {
    extend: 'Ext.form.FieldSet',
    alias: 'widget.side-filter-date-time',
    requires: [
        'Dsh.view.widget.common.DateTimeField'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        border: 'none',
        padding: 0,
        margin: 0
    },
    defaults: {
        xtype: 'datetime-field',
        style: {
            border: 'none',
            padding: 0,
            margin: 0
        }
    },
    items: [
        { xtype: 'panel', name: 'header', baseCls: 'x-form-item-label', style: 'margin: 15px 0' },
        { label: Uni.I18n.translate('connection.widget.sideFilter.from', 'DSH', 'From'), name: 'from' },
        { label: Uni.I18n.translate('connection.widget.sideFilter.to', 'DSH', 'To'), name: 'to' }
    ],
    initComponent: function () {
        this.callParent(arguments);
        this.down('panel[name=header]').update(this.wTitle);
    }
});