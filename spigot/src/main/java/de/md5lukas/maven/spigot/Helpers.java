package de.md5lukas.maven.spigot;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Helpers {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Nullable
    private static Method addUrlMethod;

    @NotNull
    private static Method getAddUrlMethod() throws NoSuchMethodException {
        if (addUrlMethod == null) {
            addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addUrlMethod.setAccessible(true);
        }

        return addUrlMethod;
    }

    @SneakyThrows // Method addURL exists in the URLClassLoader and the URL created from the file should work too
    static void loadJar(Class<? extends Plugin> clazz, File file) {
        getAddUrlMethod().invoke(clazz.getClassLoader(), file.toURI().toURL());
    }
}
