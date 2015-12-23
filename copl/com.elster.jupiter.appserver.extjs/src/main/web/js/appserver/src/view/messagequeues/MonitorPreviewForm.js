Ext.define('Apr.view.messagequeues.MonitorPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.monitor-preview-form',
    router: null,
    layout: {
        type: 'vbox'
    },
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    items: [
        {
            fieldLabel: Uni.I18n.translate('general.messages', 'APR', 'Messages'),
            name: 'numberOfMessages'
        },
        {
            fieldLabel: Uni.I18n.translate('general.Errors', 'APR', 'Errors'),
            name: 'numberOFErrors'
        },
        {
            fieldLabel: Uni.I18n.translate('messageQueue.subscribers', 'APR', 'Used by'),
            name: 'subscriberSpecInfos',
            /* tpl: ['<ul>', '<tpl for=".">', '<li>{displayName} ({active})) </li>', '</tpl>', '</ul>'], */
            renderer: function (value) {
               var resultArray = [];
               Ext.Array.each(value, function (subscriberSpecInfo) {
                   resultArray.push('<p>' + Ext.String.htmlEncode(subscriberSpecInfo['displayName']) /*+' ('+ subscriberSpecInfo['active']+')'+*/+'</p>');
               });
               return resultArray;
            }
        }
    ]
});
