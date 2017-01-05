package org.smssecure.smssecure.crypto;

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

public class SessionUtil {

  public static boolean hasSession(Context context, MasterSecret masterSecret, Recipient recipient) {
    return hasSession(context, masterSecret, recipient.getNumber());
  }

  public static boolean hasSession(Context context, MasterSecret masterSecret, @NonNull String number) {
    if (Build.VERSION.SDK_INT >= 22) {
      List<SubscriptionInfo> listSubscriptionInfo = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
      for (SubscriptionInfo subscriptionInfo : listSubscriptionInfo) {
        SessionStore   sessionStore   = new SilenceSessionStore(context, masterSecret, subscriptionInfo.getSubscriptionId());
        AxolotlAddress axolotlAddress = new AxolotlAddress(number, 1);

        if (!sessionStore.containsSession(axolotlAddress)) return false;
      }
      return true;
    } else {
      SessionStore   sessionStore   = new SilenceSessionStore(context, masterSecret, -1);
      AxolotlAddress axolotlAddress = new AxolotlAddress(number, 1);

      return sessionStore.containsSession(axolotlAddress);
    }

  }
}
