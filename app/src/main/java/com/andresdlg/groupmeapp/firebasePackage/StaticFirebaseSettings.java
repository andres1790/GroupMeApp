package com.andresdlg.groupmeapp.firebasePackage;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by andresdlg on 21/01/18.
 */

public class StaticFirebaseSettings {
    public static String currentUserId;

    public static void setCurrentUserId(String currentUserId) {
        StaticFirebaseSettings.currentUserId = currentUserId;
    }
}
