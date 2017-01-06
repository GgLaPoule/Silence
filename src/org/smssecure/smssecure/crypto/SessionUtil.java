package org.smssecure.smssecure.crypto;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import org.smssecure.smssecure.crypto.storage.SilenceSessionStore;
import org.smssecure.smssecure.recipients.Recipient;
import org.whispersystems.libaxolotl.AxolotlAddress;
import org.whispersystems.libaxolotl.state.SessionStore;

import java.util.List;
import java.util.LinkedList;

public class SessionUtil {

  public static boolean hasSession(Context context, MasterSecret masterSecret, @NonNull String number, int subscriptionId) {
    SessionStore   sessionStore   = new SilenceSessionStore(context, masterSecret, subscriptionId);
    AxolotlAddress axolotlAddress = new AxolotlAddress(number, 1);

    return sessionStore.containsSession(axolotlAddress);
  }

  public static boolean hasSession(Context context, MasterSecret masterSecret, @NonNull String number) {
    if (Build.VERSION.SDK_INT >= 22) {
      List<SubscriptionInfo> listSubscriptionInfo = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
      for (SubscriptionInfo subscriptionInfo : listSubscriptionInfo) {
        if (!hasSession(context, masterSecret, number, subscriptionInfo.getSubscriptionId())) return false;
      }
      return true;
    } else {
      return hasSession(context, masterSecret, number, -1);
    }
  }

  public static boolean hasAtLeastOneSession(Context context, MasterSecret masterSecret, @NonNull String number) {
    if (Build.VERSION.SDK_INT >= 22) {
      List<SubscriptionInfo> listSubscriptionInfo = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
      for (SubscriptionInfo subscriptionInfo : listSubscriptionInfo) {
        if (hasSession(context, masterSecret, number, subscriptionInfo.getSubscriptionId())) return true;
      }
      return false;
    } else {
      return hasSession(context, masterSecret, number, -1);
    }
  }

  @TargetApi(22)
  public static List<Integer> getSubscriptionIdWithoutSession(Context context, MasterSecret masterSecret, @NonNull String number) {
    LinkedList<Integer> list = new LinkedList();

    List<SubscriptionInfo> listSubscriptionInfo = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
    for (SubscriptionInfo subscriptionInfo : listSubscriptionInfo) {
      int subscriptionId = subscriptionInfo.getSubscriptionId();
      if (!hasSession(context, masterSecret, number, subscriptionId)) list.add(subscriptionId);
    }
    return list;
  }
}
