# R8 en release. Sin esto la build de release iba sin optimizar, que es la mitad de la
# diferencia de fluidez frente a una app compilada de verdad.

# --- Modelos que Gson rellena por reflexión -------------------------------------------------
# Gson busca los campos por nombre, así que renombrarlos rompe la deserialización en silencio:
# no falla, simplemente llegan nulos. Retrofit y Gson traen sus propias reglas desde hace
# versiones; estas cubren lo nuestro, que es lo que ellas no pueden saber.
-keepclassmembers class com.dwightsckrute.visto.data.** {
    <fields>;
    <init>(...);
}
-keepclassmembers class com.dwightsckrute.visto.core.network.** {
    <fields>;
    <init>(...);
}
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault

# --- Enumeraciones que viajan a la base de datos --------------------------------------------
# Room guarda su nombre como texto; renombrarlas dejaría la biblioteca ilegible.
-keepclassmembers enum com.dwightsckrute.visto.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}

# --- Kotlin ---------------------------------------------------------------------------------
# Los metadatos hacen falta para la reflexión de kotlinx y para los tipos genéricos de Gson.
-keep class kotlin.Metadata { *; }
