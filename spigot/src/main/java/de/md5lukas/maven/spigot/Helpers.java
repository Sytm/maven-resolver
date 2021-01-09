package de.md5lukas.maven.spigot;

import com.google.common.io.BaseEncoding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Helpers {

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

    static void downloadFile(File destination, URL url) throws IOException {
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
             FileChannel fc = FileChannel.open(destination.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            fc.transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    static byte[] readChecksum(URL url) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line = br.readLine();
            if (line == null) {
                return null;
            }
            return BaseEncoding.base16().decode(line.toUpperCase(Locale.ROOT));
        }
    }

    static byte[] digestFile(MessageDigest messageDigest, File file) throws IOException {
        try (DigestOutputStream dos = new DigestOutputStream(new DiscardingOutputStream(), messageDigest)) {
            Files.copy(file.toPath(), dos);
        }
        return messageDigest.digest();
    }
}
