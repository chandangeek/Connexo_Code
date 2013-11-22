package com.energyict.mdc.common;

import java.sql.SQLException;

/**
 * The listener interface for receiving <Code>BusinessEvents</Code>.
 * <p>The class that is interested in processing a <Code>BusinessEvent</Code> implements this interface, and the object created with that class is registered with a component, using the component's addListener method.
 * When the <code>BusinessEvent</code>occurs, that object's handleEvent method is invoked.
 */
public interface BusinessEventListener extends TransactionResource {

    /**
     * Invoked when an <code>BusinessEvent</code> occurs
     * <p>Throwing an exception cancels the event. The transaction wherein the creation, update or deletion of
     * the <Code>BusinessObject<Code> takes place is rollbacked.</p>
     *
     * @param event <code>BusinessEvent</code> that occurred.
     * @throws BusinessException - <code>BusinessException</code> thrown during creation, update or deletion of the <code>BusinessObject</code>.
     * @throws SQLException      - <code>SQLException</code> thrown during creation, update or deletion of the <code>BusinessObject</code>.
     */
    public void handleEvent(BusinessEvent event) throws BusinessException, SQLException;
}
