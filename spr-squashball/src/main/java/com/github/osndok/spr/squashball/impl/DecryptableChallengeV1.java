package com.github.osndok.spr.squashball.impl;

import com.github.osndok.spr.Spr1Key;
import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

public
class DecryptableChallengeV1
{
    private static final
    Random RANDOM = new Random();

    private static final
    Argon2Advanced PASSWORD_CRYPTO = Argon2Factory.createAdvanced();

    // The number of bytes long that a sha1 hash is.
    private static final
    int KEY_LENGTH = 20;

    private final
    int iterations;

    private final
    int memory;

    private final
    int parallelism;

    private final
    byte[] saltBytes;

    private final
    byte[] publicHash;

    private final
    byte[] encryptedPrivateHash;

    public
    DecryptableChallengeV1(final Spr1Key key, final String password)
    {
        var passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        iterations = 10;
        memory = 10*1024; // 10 MB
        parallelism = 1;
        saltBytes = new byte[KEY_LENGTH];
        RANDOM.nextBytes(saltBytes);

        var secretKeyMaterial = PASSWORD_CRYPTO.pbkdf(iterations, memory, parallelism, passwordBytes, saltBytes, KEY_LENGTH);

        publicHash = key.getPublicBytes();
        encryptedPrivateHash = bytewiseXOR(key.getPrivateBytes(), secretKeyMaterial);
    }

    private
    Spr1Key recoverKey(final String password)
    {
        var passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        var secretKeyMaterial = PASSWORD_CRYPTO.pbkdf(iterations, memory, parallelism, passwordBytes, saltBytes, KEY_LENGTH);

        var actualPrivateHash = bytewiseXOR(encryptedPrivateHash, secretKeyMaterial);

        return new Spr1Key(publicHash, actualPrivateHash);
    }

    private
    DecryptableChallengeV1(
            final int iterations,
            final int memory,
            final int parallelism,
            final byte[] saltBytes,
            final byte[] publicHash,
            final byte[] encryptedPrivateHash
    )
    {
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
        this.saltBytes = saltBytes;
        this.publicHash = publicHash;
        this.encryptedPrivateHash = encryptedPrivateHash;
    }

    public static
    byte[] bytewiseXOR(final byte[] a, final byte[] b)
    {
        assert(a.length == b.length);
        int length = a.length;

        var retval = new byte[length];

        for (int i = 0; i < length; i++)
        {
            retval[i] = (byte)(a[i] ^ b[i]);
        }

        return retval;
    }

    public
    void writeTo(final Path directory) throws IOException
    {
        try (var out = new PrintWriter(getFile(directory)))
        {
            out.println("format=spr1-decryptable");
            out.println("version=1");
            out.print("iterations=");
            out.println(iterations);
            out.print("memory=");
            out.println(memory);
            out.print("parallelism=");
            out.println(parallelism);
            out.print("salt=");
            out.println(encodeBytes(saltBytes));
            out.print("public=");
            out.println(encodeBytes(publicHash));
            out.print("private=");
            out.println(encodeBytes(encryptedPrivateHash));
        }
    }

    public static
    DecryptableChallengeV1 fromDirectoryEntry(Path directory) throws IOException
    {
        var p = loadProperties(getFile(directory));

        var format = p.getProperty("format");
        var version = p.getProperty("version");
        var iterations = p.getProperty("iterations");
        var memory = p.getProperty("memory");
        var parallelism = p.getProperty("parallelism");
        var salt = p.getProperty("salt");
        var _public = p.getProperty("public");
        var _private = p.getProperty("private");

        if (!format.equals("spr1-decryptable") || !version.equals("1"))
        {
            throw new UnsupportedOperationException("cannot decode "+format+" / v"+version);
        }

        return new DecryptableChallengeV1(
                Integer.parseInt(iterations),
                Integer.parseInt(memory),
                Integer.parseInt(parallelism),
                Base64.decodeBase64(salt),
                Base64.decodeBase64(_public),
                Base64.decodeBase64(_private)
        );
    }

    private static
    Properties loadProperties(final File file) throws IOException
    {
        var retval = new Properties();

        try (var in = new FileInputStream(file))
        {
            retval.load(in);
        }

        return retval;
    }

    public static
    File getFile(final Path directory)
    {
        return new File(directory.toFile(), "index.spr.v1");
    }

    private
    String encodeBytes(final byte[] bytes)
    {
        return Base64.encodeBase64URLSafeString(bytes);
    }

    public static
    void main(String[] args) throws IOException
    {
        var secretToProtect = new Spr1Key("spr1-" + "OAJe0eqWMDSFIOTrSXyPLjQTxVw" + "qvTGHdzF6KLavt4PO0gs2a6pQ00");

        var directory = Path.of(args[0]);
        var password = args[1];
        var operation = args[2];

        if (operation.equals("write") || operation.equals("both"))
        {
            new DecryptableChallengeV1(secretToProtect, password).writeTo(directory);
        }

        if (operation.equals("read") || operation.equals("both"))
        {
            var d = DecryptableChallengeV1.fromDirectoryEntry(directory);
            var recoveredKey = d.recoverKey(password);

            System.out.println("Recovered: "+recoveredKey);

            if (operation.equals("both"))
            {

                if (!Arrays.equals(recoveredKey.getPublicBytes(), secretToProtect.getPublicBytes()))
                {
                    throw new AssertionError("public key got mangled");
                }

                if (!Arrays.equals(recoveredKey.getPrivateBytes(), secretToProtect.getPrivateBytes()))
                {
                    throw new AssertionError("private key got mangled");
                }
            }
        }
    }
}
