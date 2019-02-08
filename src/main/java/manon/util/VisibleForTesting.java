package manon.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/** Indicates that the visibility of a method has been relaxed to make the code testable. */
@SuppressWarnings("unused")
@Documented
@Target({TYPE, FIELD, METHOD})
@Retention(SOURCE)
public @interface VisibleForTesting {
    
    String where() default "";
}
