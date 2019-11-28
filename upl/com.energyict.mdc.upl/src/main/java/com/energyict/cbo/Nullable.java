package com.energyict.cbo;

/**
 * Created because of the need to know when (specifically) special classes used within  'dynamic properties'
 * are null: e.g. the Password class
 * Date: 20/06/13
 * Time: 8:25
 */
public interface Nullable {

    /**
     *
     * @return true is the object can be considered as a null (empty) object, false if not
     */
    boolean isNull();

}
