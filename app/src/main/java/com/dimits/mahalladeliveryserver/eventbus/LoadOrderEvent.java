package com.dimits.mahalladeliveryserver.eventbus;

public class LoadOrderEvent {
    private int status;

    public LoadOrderEvent() {
    }

    public LoadOrderEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
