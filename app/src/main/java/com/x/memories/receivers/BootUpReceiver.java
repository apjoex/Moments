package com.x.memories.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.x.memories.services.NotificationService;

/**
 * Created by AKINDE-PETERS on 9/4/2016.
 */
public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /****** For Start Activity *****/
//        Intent i = new Intent(context, MyActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(i);

        /***** For start Service  ****/
        Intent myIntent = new Intent(context, NotificationService.class);
        context.startService(myIntent);
    }
}
