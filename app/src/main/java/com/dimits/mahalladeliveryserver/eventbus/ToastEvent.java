package com.dimits.mahalladeliveryserver.eventbus;

import com.dimits.mahalladeliveryserver.common.Common;

public class ToastEvent {
    private Common.ACTION action;
    private boolean isFromFoodList;

    public ToastEvent(Common.ACTION action, boolean isFromFoodList) {
        this.action = action;
        this.isFromFoodList = isFromFoodList;
    }

    public Common.ACTION getAction() {
        return action;
    }

    public void setAction(Common.ACTION action) {
        this.action = action;
    }

    public boolean isFromFoodList() {
        return isFromFoodList;
    }

    public void setFromFoodList(boolean fromFoodList) {
        isFromFoodList = fromFoodList;
    }
}
