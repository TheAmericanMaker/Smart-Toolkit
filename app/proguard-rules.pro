# AdMob
-keep class com.google.android.gms.ads.** { *; }

# Google Play Billing
-keep class com.android.billingclient.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
