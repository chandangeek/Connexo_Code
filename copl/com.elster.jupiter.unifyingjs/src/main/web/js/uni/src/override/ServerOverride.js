/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.override.ServerOverride', {
    override: 'Ext.data.proxy.Server',
    processResponse: function(success, operation, request, response, callback, scope) {
        var me = this,
            reader,
            result;

        if (success === true) {
            reader = me.getReader();

            // Apply defaults to incoming data only for read operations.
            // For create and update, there will already be a client-side record
            // to match with which will contain any defaulted in values.
            reader.applyDefaults = operation.action === 'read';

            result = reader.read(me.extractResponseData(response));

            if (result.success !== false) {
                //see comment in buildRequest for why we include the response object here
                Ext.apply(operation, {
                    response: response,
                    resultSet: result
                });

                operation.commitRecords(result.records);
                operation.setCompleted();
                operation.setSuccessful();
            } else {
                Ext.apply(operation, {
                    response: response,
                    resultSet: result
                });
                operation.setException(result.message);
                me.fireEvent('exception', this, response, operation);
            }
        } else {
            Ext.apply(operation, {
                response: response,
                resultSet: result
            });
            me.setException(operation, response);
            me.fireEvent('exception', this, response, operation);
        }

        //this callback is the one that was passed to the 'read' or 'write' function above
        if (typeof callback == 'function') {
            callback.call(scope || me, operation);
        }

        me.afterRequest(request, success);
    }
});
