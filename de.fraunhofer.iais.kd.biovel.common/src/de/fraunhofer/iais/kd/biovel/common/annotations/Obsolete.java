package de.fraunhofer.iais.kd.biovel.common.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java annotation as suggested in #631. Indicates that the marked element has
 * become obsolete, because it is not longer used. @Obsolete supports an
 * optional argument to comment your suspicion.
 * 
 * @author cweiland
 */
@Target( { TYPE, FIELD, LOCAL_VARIABLE, METHOD, PARAMETER, CONSTRUCTOR })
@Retention(RetentionPolicy.SOURCE)
public @interface Obsolete {

    /**
     * place to comment your annotation. For yet unknown reasons, field may only
     * be unset when named value.
     */
    String value() default "";

}
