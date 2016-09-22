package com.android.functional.otatests;

public enum UpdateUrgency {
    MANDATORY("mandatory"),
    AUTOMATIC("automatic"),
    RECOMMENDED("recommended");

    String mUrgency;
    private UpdateUrgency(String urgency) {
        mUrgency = urgency;
    }

}

