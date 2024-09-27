package com.shirkanesi.magmaplayer.model;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class UserData {

    @Nullable
    private Object userData = null;

    protected void setUserData(@Nullable Object userData) {
        this.userData = userData;
    }

}
