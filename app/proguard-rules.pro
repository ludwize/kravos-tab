-keepattributes JavascriptInterface
-keepclassmembers class net.kravos.tab.Bridge {
    @android.webkit.JavascriptInterface <methods>;
}
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
