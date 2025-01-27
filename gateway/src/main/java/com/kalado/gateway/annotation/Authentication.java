package com.kalado.gateway.annotation;

import java.lang.annotation.*;
import javax.persistence.Inheritance;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inheritance
@Documented
public @interface Authentication {
  String token() default "";

  String userId() default "";
}
