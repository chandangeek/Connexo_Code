Ext.define('Mdc.view.setup.deviceloadprofiles.SideFilterDateTime', {
    extend: 'Ext.form.FieldSet',
    alias: 'widget.side-filter-date-time-profiles',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.DateTimeField'
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
        xtype: 'datetime-field-profiles',
        style: {
            border: 'none',
            padding: 0,
            margin: 0
        }
    },
    items: [
        { xtype: 'panel', name: 'header', baseCls: 'x-form-item-label', style: 'margin: 15px 0' },
        { label: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From'), name: 'from' }
    ],
    initComponent: function () {
        this.callParent(arguments);
        this.down('panel[name=header]').update(this.wTitle);
    }
});
