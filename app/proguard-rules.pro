# Starscript Music Proguard Rules

# Media3
-keep class androidx.media3.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keepclassmembers class * {
    @javax.inject.Inject *;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.vk.starscriptmusic.data.** { *; }
-keep class com.vk.starscriptmusic.domain.model.** { *; }
