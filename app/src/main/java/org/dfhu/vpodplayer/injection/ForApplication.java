package org.dfhu.vpodplayer.injection;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Annotation to differentiate from activity context */
@Qualifier @Retention(RUNTIME)
public @interface ForApplication {
}
