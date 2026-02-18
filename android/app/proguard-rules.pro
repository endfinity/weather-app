# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.clearsky.weather.data.remote.dto.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.clearsky.weather.**$$serializer { *; }
-keepclassmembers class com.clearsky.weather.** { *** Companion; }
-keepclasseswithmembers class com.clearsky.weather.** { kotlinx.serialization.KSerializer serializer(...); }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
