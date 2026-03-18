# Keep LSPosed module entry point
-keep class eu.hxreborn.amznkiller.xposed.AmznkillerModule { *; }

# Prevent R8 from merging hook classes into app process code (compileOnly API)
-keep,allowobfuscation class eu.hxreborn.amznkiller.xposed.hook.** { *; }

# Xposed module class pattern
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations
-keep,allowobfuscation,allowoptimization public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
    public void onPackageLoaded(...);
    public void onPackageReady(...);
    public void onSystemServerStarting(...);
}

# Keep PrefsManager for remote preferences
-keep class eu.hxreborn.amznkiller.prefs.PrefsManager { *; }

# Keep Xposed detection method
-keep class eu.hxreborn.amznkiller.ui.MainActivity {
    public static boolean isXposedEnabled();
}

# Kotlin intrinsics optimization
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}
-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}

# Strip debug logs in release builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Obfuscation
-repackageclasses
-allowaccessmodification
