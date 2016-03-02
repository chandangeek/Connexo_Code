Ext.define('Idc.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.issue-details-log-grid',
    store: null,
    ui: 'medium',
    columns: [
        {
            text: Uni.I18n.translate('general.timestamp', 'IDC', 'Timestamp'),
            dataIndex: 'timestamp',
            flex: 1,
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTimeLong(value) : '';
            }
        },
        {
            text: Uni.I18n.translate('general.description', 'IDC', 'Description'),
            dataIndex: 'details',
            flex: 3,
            renderer: function (value) {
                return value ? Ext.String.htmlEncode(value) : '';
            }
        },
        {
            text: Uni.I18n.translate('general.logLevel', 'MDC', 'Log level'),
            dataIndex: 'logLevel',
            flex: 1,
            renderer: function (value) {
                return value ? Ext.String.htmlEncode(value) : '';
            }
        }
    ]
});
