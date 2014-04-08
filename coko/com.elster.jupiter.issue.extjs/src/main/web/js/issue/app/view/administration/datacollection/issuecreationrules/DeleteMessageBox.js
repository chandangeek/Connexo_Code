Ext.define('Isu.view.administration.datacollection.issuecreationrules.DeleteMessageBox', {
    alias: 'widget.delete-message-box',
    show: function (menu) {
        var dialog = Ext.create('Ext.window.MessageBox', {
            buttons: [
                {
                    text: 'Delete',
                    handler: function () {
                        Ext.Ajax.request({
                            url: '/api/isu/creationrules/{id}?version=1',
                            method: 'DELETE',
                            success: function () {
                                dialog.close();
                                var header = {
                                    style: 'msgHeaderStyle',
                                    text: 'Issue creation rule deleted'
                                };
                                Isu.Current.fireEvent('isushowmsg', {
                                    type: 'notify',
                                    msgBody: [header],
                                    y: 10,
                                    showTime: 5000
                                });
                            }
                        })
                    }
                },
                {
                    text: 'Cancel',
                    cls: 'isu-btn-link',
                    handler: function () {
                        dialog.close();
                    }
                }
            ]
        });

        dialog.show({
            title: 'Delete issue creation rule',
            msg: '<p><b>Delete rule " "?</b></p>' + '<p>This issue creation rule disappears from the list.<br>Issues will not be created automatically by this rule.</p>',
            icon: Ext.MessageBox.WARNING,
            cls: 'isu-delete-message'
        });

        dialog.setHeight(200);
        dialog.setWidth(450);
    }
});
