# Debug builds only for v1 (minify disabled). Kept for future release builds.
# kotlinx.serialization: keep @Serializable metadata.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.bykhavoy.ehat.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
