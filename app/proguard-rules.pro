# Add project specific ProGuard rules here.
# ResQCampus - keep Play Services and Compose
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
