package com.tpg.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaData {
    String[] stories() default {};

    String[] bugs() default {};

    String author();

    String testCaseId();

    String category();
}
