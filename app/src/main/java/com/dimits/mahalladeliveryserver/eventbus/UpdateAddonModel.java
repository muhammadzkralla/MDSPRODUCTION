package com.dimits.mahalladeliveryserver.eventbus;

import com.dimits.mahalladeliveryserver.model.AddonModel;

import java.util.List;

public class UpdateAddonModel {
    private List<AddonModel> addonModelList;

    public UpdateAddonModel(List<AddonModel> addonModelList) {
        this.addonModelList = addonModelList;
    }

    public UpdateAddonModel() {
    }

    public List<AddonModel> getAddonModelList() {
        return addonModelList;
    }

    public void setAddonModelList(List<AddonModel> addonModelList) {
        this.addonModelList = addonModelList;
    }
}
