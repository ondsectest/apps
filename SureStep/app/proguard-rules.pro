# Room generates implementations reflectively referenced by name.
-keep class com.surestep.app.data.local.** { *; }

# Kotlin coroutines / Compose defaults are handled by the consumer rules that
# ship with the libraries; nothing extra is needed here.

# Keep line numbers for readable crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
