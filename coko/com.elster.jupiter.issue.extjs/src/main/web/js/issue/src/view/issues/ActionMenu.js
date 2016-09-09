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
    router: null,
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    predefinedItems: [
        {
            text: Uni.I18n.translate('issues.actionMenu.addComment', 'ISU', 'Add comment'),
            privileges: Isu.privileges.Issue.comment,
            action: 'addComment'
        }
    ],
    listeners: {
        show: {
            fn: function () {
                var me = this;

                me.removeAll();
                if (me.record) {
                    me.setLoading(true);
                    me.store.getProxy().url = '/api/isu/issues/' + me.record.getId() + '/actions';
                    me.store.load(function () {
                        me.onLoad();
                        me.setLoading(false);
                    });
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

    // this method overwritten to avoid firing click event twice in menu
    onClick: function (e) {
        var me = this,
            item;

        if (me.disabled) {
            e.stopEvent();
            return;
        }

        item = (e.type === 'click') ? me.getItemFromEvent(e) : me.activeItem;
        if (item && item.isMenuItem) {
            if (!item.menu || !me.ignoreParentClicks) {
                //item.onClick(e);
            } else {
                e.stopEvent();
            }
        }
        // Click event may be fired without an item, so we need a second check
        if (!item || item.disabled) {
            item = undefined;
        }
        me.fireEvent('click', me, item, e);
        me.hide();
    },

    onLoad: function () {
        var me = this,
            issueId = me.record.getId(),
            issueType = me.record.get('issueType').uid,
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
                    privileges = Isu.privileges.Issue.canDoAction() && Isu.privileges.Issue.assign;
                    break;
                case 'Close issue':
                    privileges = Isu.privileges.Issue.canDoAction() && Isu.privileges.Issue.close;
                    break;
                case 'Retry now':
                    privileges = Isu.privileges.Device.canOperateDeviceCommunication() && Isu.privileges.Issue.canDoAction();
                    break;
                case 'Send someone to inspect':
                    privileges = Isu.privileges.Issue.notify;
                    break;
                case 'Notify user':
                    privileges = Isu.privileges.Issue.notify;
                    break;
                case 'Retry estimation':
                    privileges = Isu.privileges.Issue.runTask;
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
                        fromOverview: me.router.currentRoute.match('view') != null,
                        issueType: issueType
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
                    case 'startProcess':
                        menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/startProcess').buildUrl({issueId: issueId} , {details: menuItem.details, issueType: issueType});
                        break;
                }
            });
            me.add(me.predefinedItems);
        }

        if (Isu.privileges.Issue.canViewProcessMenu() && issueType == 'datacollection')
        {
            me.add({
                text: Uni.I18n.translate('issues.actionMenu.startProcess', 'ISU', 'Start process'),
                action: 'startProcess',
                href: me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/startProcess').buildUrl({issueId: issueId} , {details: false, issueType: issueType}),
                details: false
            });
        }

        // add specific actions
        if (Isu.privileges.Device.viewDeviceCommunication) {
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
                                logLevels: ['Error', 'Warning', 'Information']
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
                                logLevels: ['Error', 'Warning', 'Information'],
                                communications: ['Connections', 'Communications']
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