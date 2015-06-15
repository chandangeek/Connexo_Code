Ext.define('Isu.view.issues.ActionMenu', {
    extend: 'Ext.menu.Menu',
    requires: [
        'Isu.privileges.Issue',
        'Isu.privileges.Device'
    ],
    alias: 'widget.issues-action-menu',
    store: 'Isu.store.IssueActions',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    minHeight: 60,
    router: null,
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    predefinedItems: [
        {
            text: Uni.I18n.translate('issues.actionMenu.addComment', 'ISU', 'Add comment'),
            privilege: Isu.privileges.Issue.comment,
            action: 'addComment'
        }
    ],
    listeners: {
        show: {
            fn: function () {
                var me = this;

                me.removeAll();
                if (me.record) {
                    me.store.getProxy().url = me.record.getProxy().url + '/' + me.record.getId() + '/actions';
                    me.store.load(function () {
                        me.onLoad();
                        me.setLoading(false);
                    });
                    setTimeout(function () {
                        me.setLoading(true);
                    }, 1)
                } else {
                    //<debug>
                    console.error('Record for \'' + me.xtype + '\' is not defined');
                    //</debug>
                }
            }
        }
    },
    initComponent: function () {
        var me = this;

        //<debug>
        if (!me.router) {
            console.error('Router for \'' + me.xtype + '\' is not defined');
        }
        //</debug>

        me.bindStore(me.store || 'ext-empty-store', true);

        this.callParent(arguments);
    },

    onLoad: function () {
        var me = this,
            issueId = me.record.getId(),
            deviceMRID,
            comTaskId,
            comTaskSessionId,
            connectionTaskId,
            comSessionId;

        if (!me.router) {
            return
        }

        Ext.suspendLayouts();
        me.removeAll();

        // add dynamic actions
        me.store.each(function (record) {
            var privileges;
            switch (record.get('name')) {
                case 'Assign issue':
                    privileges = Isu.privileges.Issue.assign;
                    break;
                case 'Close issue':
                    privileges = Isu.privileges.Issue.close;
                    break;
                case 'Retry now':
                    privileges = Isu.privileges.Device.canOperateDeviceCommunication();
                    break;
                case 'Send someone to inspect':
                    privileges = Isu.privileges.Issue.notify;
                    break;
                case 'Notify user':
                    privileges = Isu.privileges.Issue.notify;
                    break;
            }

            var menuItem = {
                text: record.get('name'),
                privileges: privileges
            };

            if (Ext.isEmpty(record.properties().count())) {
                menuItem.actionRecord = record;
            } else {
                menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/action').buildUrl(
                    {
                        issueId: issueId,
                        actionId: record.getId()
                    },
                    {
                        fromOverview: me.router.currentRoute.match('view') != null
                    }
                );
            }
            me.add(menuItem);
        });

        // add predefined actions
        if (me.predefinedItems && me.predefinedItems.length) {
            Ext.Array.each(me.predefinedItems, function (menuItem) {
                switch (menuItem.action) {
                    case 'addComment':
                        menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view').buildUrl({issueId: issueId}, {addComment: true});
                        break;
                }
            });
            me.add(me.predefinedItems);
        }

        // add specific actions
        if (Isu.privileges.viewDeviceCommunication) {
            deviceMRID = me.record.get('deviceMRID');
            if (deviceMRID) {
                comTaskId = me.record.get('comTaskId');
                comTaskSessionId = me.record.get('comTaskSessionId');
                connectionTaskId = me.record.get('connectionTaskId');
                comSessionId = me.record.get('comSessionId');
                if (comTaskId && comTaskSessionId) {
                    me.add({
                        text: Uni.I18n.translate('issues.actionMenu.viewCommunicationLog', 'ISU', 'View communication log'),
                        href: me.router.getRoute('devices/device/communicationtasks/history/viewlog').buildUrl(
                            {
                                mRID: deviceMRID,
                                comTaskId: comTaskId,
                                historyId: comTaskSessionId
                            },
                            {
                                filter: {
                                    logLevels: ['Error', 'Warning', 'Information']
                                }
                            }
                        ),
                        hrefTarget: '_blank'
                    });
                }
                if (connectionTaskId && comSessionId) {
                    me.add({
                        text: Uni.I18n.translate('issues.actionMenu.viewConnectionLog', 'ISU', 'View connection log'),
                        href: me.router.getRoute('devices/device/connectionmethods/history/viewlog').buildUrl(
                            {
                                mRID: deviceMRID,
                                connectionMethodId: connectionTaskId,
                                historyId: comSessionId
                            },
                            {
                                filter: {
                                    logLevels: ['Error', 'Warning', 'Information'],
                                    logTypes: ['connections', 'communications']
                                }
                            }
                        ),
                        hrefTarget: '_blank'
                    });
                }
            }
        }

        Ext.resumeLayouts(true);
    }
});