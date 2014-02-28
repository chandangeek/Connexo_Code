Ext.define('Mtr.view.playground.I18n', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'playgroundI18n',

    content: [
        {
            xtype: 'component',
            html: '<h1>' + Uni.I18n.translate('mtr.playground.i18n.title', 'MTR', 'Internationalization example') + '</h1>'
        },
        {
            xtype: 'component',
            html: '<hr />'
        },
        {
            xtype: 'component',
            html: '<p style="margin-left: 10px;">' + Uni.I18n.translatePlural('mtr.playground.i18n.item', 0, 'MTR', 'There are {0} items') + '</p>'
        },
        {
            xtype: 'component',
            html: '<p style="margin-left: 10px;">' + Uni.I18n.translatePlural('mtr.playground.i18n.item', 1, 'MTR', 'There are {0} items') + '</p>'
        },
        {
            xtype: 'component',
            html: '<p style="margin-left: 10px;">' + Uni.I18n.translatePlural('mtr.playground.i18n.item', 1337, 'MTR', 'There are {0} items') + '</p>'
        },
        {
            xtype: 'component',
            html: '<hr />'
        },
        {
            xtype: 'component',
            html: '<p style="margin-left: 10px;">' + Uni.I18n.formatDate('mtr.playground.i18n.dateformat', new Date(), 'MTR', 'D MMMM YYYY LT') + '</p>'
        },
        {
            xtype: 'component',
            html: '<hr />'
        },
        {
            xtype: 'component',
            html: '<p style="margin-left: 10px;">' + Uni.I18n.formatNumber(13000.037, 'MTR', 3) + '</p>'
        },
        {
            xtype: 'component',
            html: '<hr />'
        },
        {
            xtype: 'component',
            html: '<p style="margin-left: 10px;">' + Uni.I18n.formatCurrency(13000.037, 'MTR') + '</p>'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});