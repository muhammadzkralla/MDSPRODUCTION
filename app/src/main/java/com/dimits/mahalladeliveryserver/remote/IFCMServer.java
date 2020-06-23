package com.dimits.mahalladeliveryserver.remote;

import com.dimits.mahalladeliveryserver.model.FCMserver.FCMResponse;
import com.dimits.mahalladeliveryserver.model.FCMserver.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMServer {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAkiNampA:APA91bFpdVwqAmNtSy0_FVFKsxT2Lo1qnDIqQWpVJPQzWzE9u6Rx4VXAj2Ldw2ufX8Jg2LMtpI806KQVKHo4nE3ECqMspxnVxtQPHlK_Cf9gGP76JoHpYpY9mvtSO2qlgIadQPf1_7jW"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}