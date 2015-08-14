Ext.define('Mdc.view.setup.comserver.ComServersSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comServersSetup',

    requires: [
        'Mdc.view.setup.comserver.ComServersGrid',
        'Mdc.view.setup.comserver.ComServerPreview',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    content: {
        ui: 'large',
        title: Uni.I18n.translate('general.comServers', 'MDC', 'Communication servers'),
        items: [
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'comServersGrid'
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'ctr-no-comservers',
                    title: Uni.I18n.translate('comserver.empty.title', 'MDC', 'No communication servers found'),
                    reasons: [
                        Uni.I18n.translate('comserver.empty.list.item1', 'MDC', 'No communication servers created yet')
                    ],
                    stepItems: [
                        {
                            text: Uni.I18n.translate('comServer.addOnline', 'MDC', 'Add online communication server'),
                            itemId: 'btn-no-items-add-online-communication-server',
                            privileges: Mdc.privileges.Communication.admin,
                            action: 'addOnlineComServer',
                            href: '#/administration/comservers/add/online'
                        }
                    ]
                },
                previewComponent: {
                    xtype: 'comServerPreview'
                }
            }
        ]}

});

