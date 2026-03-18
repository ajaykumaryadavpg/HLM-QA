package com.tpg.annotations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Scope("threadscope")
@Bean
public @interface ParallelThreadScope {
}
