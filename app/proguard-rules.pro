# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/agrewal/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# WebView
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Parceler
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class org.parceler.Parceler$$Parcels

# Akatsuki
-dontwarn com.sora.util.akatsuki.**
-keep class com.sora.util.akatsuki.** { *; }
-keep class **$$BundleRetainer { *; }
-keepclasseswithmembernames class * {
    @com.sora.util.akatsuki.* <fields>;
}

# Icepick
-dontwarn icepick.**
-keep class **$$Icepick { *; }
-keepclasseswithmembernames class * {
    @icepick.* <fields>;
}

# Stetho
-keep class com.facebook.stetho.**{ *; }
-dontwarn com.facebook.stetho.**
-dontwarn org.apache.**

# Realm
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-dontwarn javax.**
-dontwarn io.realm.**

# RxJava
-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
   long producerNode;
   long consumerNode;
}

# Support
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

# Retrolamda
-dontwarn java.lang.invoke.*

# LeakCanary
-keep class org.eclipse.mat.** { *; }
-keep class com.squareup.leakcanary.** { *; }
-dontwarn android.app.**

# Google Play Services
-dontwarn com.google.android.gms.**

-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**