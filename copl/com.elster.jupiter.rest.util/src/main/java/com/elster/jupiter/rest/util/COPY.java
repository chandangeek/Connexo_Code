package com.elster.jupiter.rest.util;

/**
 * Created by bvn on 7/7/15.
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.NameBinding;


@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("COPY")
@Documented
@NameBinding
public @interface COPY {
}
