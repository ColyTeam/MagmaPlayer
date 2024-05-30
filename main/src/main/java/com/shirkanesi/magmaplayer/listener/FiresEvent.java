package com.shirkanesi.magmaplayer.listener;

import com.shirkanesi.magmaplayer.listener.events.AudioTrackEvent;

/**
 * Annotation to document events possibly fired when method is called
 */
public @interface FiresEvent {

    /**
     * The type of event fired
     * @return
     */
    Class<? extends AudioTrackEvent> value();

    /**
     * If true, the event will fire on every method-call (if no error occurs)
     * @return
     */
    boolean onEveryPass() default false;

}
