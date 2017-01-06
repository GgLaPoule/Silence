package org.smssecure.smssecure.util.dualsim;

import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import org.smssecure.smssecure.crypto.IdentityKeyUtil;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.crypto.MasterSecretUtil;
import org.smssecure.smssecure.crypto.storage.SilenceSessionStore;

import java.io.File;
import java.util.List;

public class DualSimUpgradeUtil {
  private static final String TAG = DualSimUpgradeUtil.class.getSimpleName();

  public static void moveIdentityKeysAndSessionsToSubscriptionId(Context context, int originalSubscriptionId, int subscriptionId) {
    Log.w(TAG, "moveIdentityKeysMasterSecretAndSessionsToSubscriptionId(" + originalSubscriptionId + ", " + subscriptionId + ")");

    moveIdentityKeysToSubscriptionId(context, originalSubscriptionId, subscriptionId);
    moveSessionsToSubscriptionId(context, originalSubscriptionId, subscriptionId);
  }

  private static void moveIdentityKeysToSubscriptionId(Context context, int originalSubscriptionId, int subscriptionId) {
    String originalIdentityPublicPref  = IdentityKeyUtil.getIdentityPublicKeyDjbPref(originalSubscriptionId);
    String identityPublicPref          = IdentityKeyUtil.getIdentityPublicKeyDjbPref(subscriptionId);
    String originalIdentityPrivatePref = IdentityKeyUtil.getIdentityPrivateKeyDjbPref(originalSubscriptionId);
    String identityPrivatePref         = IdentityKeyUtil.getIdentityPrivateKeyDjbPref(subscriptionId);

    Log.w(TAG, "Moving " + originalIdentityPublicPref  + " to " + identityPublicPref);
    Log.w(TAG, "Moving " + originalIdentityPrivatePref + " to " + identityPrivatePref);

    String identityPublicKey  = IdentityKeyUtil.retrieve(context, originalIdentityPublicPref);
    String identityPrivateKey = IdentityKeyUtil.retrieve(context, originalIdentityPrivatePref);

    IdentityKeyUtil.save(context, identityPublicPref, identityPublicKey);
    IdentityKeyUtil.save(context, identityPrivatePref, identityPrivateKey);

    IdentityKeyUtil.remove(context, originalIdentityPublicPref);
    IdentityKeyUtil.remove(context, originalIdentityPrivatePref);
  }

  private static void moveSessionsToSubscriptionId(Context context, int originalSubscriptionId, int subscriptionId) {
    File sessionDirectory = SilenceSessionStore.getSessionDirectory(context);

    File[] sessionList = sessionDirectory.listFiles();

    String destinationSuffix = subscriptionId != -1 ? "." + subscriptionId : "";

    for (File session : sessionList){
      if (session.isFile()){
        String absolutePath = session.getAbsolutePath();
        String newSessionName = null;

        if (originalSubscriptionId != -1 && absolutePath.endsWith("." + originalSubscriptionId)) {
          newSessionName = absolutePath.replaceAll("/\\." + originalSubscriptionId + "/g", destinationSuffix);
        } else if (originalSubscriptionId == -1) {
          newSessionName = absolutePath + destinationSuffix;
        }

        if (newSessionName != null) {
          Log.w(TAG, "Moving session " + absolutePath + " to " + newSessionName);
          File newFile = new File(newSessionName);
          if (session.renameTo(newFile)) {
            Log.w(TAG, "Done!");
          } else {
            Log.w(TAG, "Failed!");
          }
        }

      }
    }
  }

  public static void generateKeysIfDoNotExist(Context context, MasterSecret masterSecret) {
    List<SubscriptionInfo> listSubscriptionInfo = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
    for (SubscriptionInfo subscriptionInfo : listSubscriptionInfo) {
      int subscriptionId = subscriptionInfo.getSubscriptionId();

      if (!IdentityKeyUtil.hasIdentityKey(context, subscriptionId))
        IdentityKeyUtil.generateIdentityKeys(context, masterSecret, subscriptionId);
    }
  }
}
