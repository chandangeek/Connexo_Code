package com.energyict.mdc;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates test methods or classes to run in a transaction wrapper, that will roll back all changes at the end of a test.
 *
 * Copyrights EnergyICT
 * Date: 12/02/13
 * Time: 15:47
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface Transactional {

}
