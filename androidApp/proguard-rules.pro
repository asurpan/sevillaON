# Reglas de ProGuard para ON AIR SPAIN

# Evitar que se rompa la interfaz de Compose
-keepclassmembers class * extends androidx.activity.ComponentActivity {
   public <init>(...);
}

# Mantener clases de los modelos de datos si se usan para serialización (Firebase/JS)
-keep class com.sagon.on.** { *; }

# Mantener decoradores de Compose
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault

# --- 🌐 REGLAS CRÍTICAS PARA WEBVIEW Y BRIDGE ---
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Evitar que se borren las clases internas de MainActivity (donde está el bridge)
-keep class com.sagon.on.MainActivity$* {
    *;
}

# Mantener el servicio de radio
-keep class com.sagon.on.RadioService { *; }

# --- 🚀 REGLAS PARA WORKMANAGER Y APP STARTUP (SOLUCIÓN CRASH V8/V9) ---
-keep class androidx.work.impl.** { *; }
-keep class androidx.startup.** { *; }
-keep class androidx.room.MultiInstanceInvalidationService { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# Mantener las implementaciones de base de datos de Room (usadas por WorkManager)
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.work.impl.WorkDatabase_Impl
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { public <init>(...); }

# Mantener los inicializadores de App Startup
-keep class * implements androidx.startup.Initializer { *; }
-keep class androidx.startup.InitializationProvider { *; }
