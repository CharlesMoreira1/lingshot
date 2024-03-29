-keep class io.grpc.** {*;}
-keepattributes Signature
-keep class * extends com.google.protobuf.GeneratedMessageLite {*;}
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn com.lingshot.common.R$string
-dontwarn com.squareup.okhttp.CipherSuite
-dontwarn com.squareup.okhttp.ConnectionSpec
-dontwarn com.squareup.okhttp.TlsVersion
-keep class com.google.** { *; }
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn java.lang.invoke.*
-dontwarn retrofit.appengine.UrlFetchClient
-keepclasseswithmembers class * {
   @retrofit.http.* <methods>;
}
-keepclassmembernames interface * {
   @retrofit.http.* <methods>;
}
-dontwarn retrofit2.Platform$Java8
