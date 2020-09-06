package com.enjoy.annotion;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnjoyController {
    String value() default "";
}
