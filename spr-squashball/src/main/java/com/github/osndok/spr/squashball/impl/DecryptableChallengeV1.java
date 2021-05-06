package com.github.osndok.spr.squashball.impl;

import com.github.osndok.spr.Spr1Key;
import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
        var file = new File(directory.toFile(), "index.spr.v1");

        try (var out = new PrintWriter(file))
        {
            out.println("[SPR1-DECRYPTABLE]");
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

        if (operation.equals("write"))
        {
            new DecryptableChallengeV1(secretToProtect, password).writeTo(directory);
        }
    }
}
