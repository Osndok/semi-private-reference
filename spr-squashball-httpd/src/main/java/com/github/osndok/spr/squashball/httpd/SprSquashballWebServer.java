package com.github.osndok.spr.squashball.httpd;

import com.github.osndok.spr.squashball.impl.SquashReaderV1;
import fi.iki.elonen.NanoHTTPD;

import java.io.*;

/**
 * Given either a squashball or a directory of squashballs, serve the contents via a small HTTPD server.
 * The decryption password may either be provided or requested at runtime.
 * The port number may be provided, or the server will try to allocate one semi-stable to the content being served.
 */
public
class SprSquashballWebServer extends NanoHTTPD
{
    private final
    File file;

    private final
    SquashReaderV1 squashReader;

    public
    SprSquashballWebServer(final File file, final int port) throws IOException
    {
        super(port);
        this.file = file;
        // TODO: support a "whole directory full of squashballs" by layering them (NB: password entry)
        this.squashReader = new SquashReaderV1(file.getAbsolutePath());
    }

    public static
    void main(String[] args) throws IOException
    {
        var file = args[0];
        var port = args[1];
        var server = new SprSquashballWebServer(new File(file), Integer.parseInt(port));
        var daemon = false;
        server.start(SOCKET_READ_TIMEOUT, daemon);

        if (args.length>=3)
        {
            var password = args[2];
            server.squashReader.decryptTableOfContents(password);
        }
        else
        {
            var br = new BufferedReader(new InputStreamReader(System.in));
            System.err.print("Enter password: ");
            var password = br.readLine();
            server.squashReader.decryptTableOfContents(password);
        }
    }

    @Override
    public
    Response serve(final IHTTPSession request)
    {
        var uri = request.getUri();
        var path = uri.substring(1);

        if (path.isEmpty())
        {
            path = "index.html";
        }

        // TODO: support password input via POST
        // TODO: respond with redirect to password entry page if squash reader is not unlocked

        try
        {
            var tuple = squashReader.fetchPath(path);
            if (tuple == null)
            {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found");
            }
            var data = tuple.clearTextBytes;
            return newFixedLengthResponse(Response.Status.OK, guessMimeType(path.toLowerCase()), new ByteArrayInputStream(data), data.length);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.getMessage());
        }
    }

    private
    String guessMimeType(final String path)
    {
        if (path.endsWith(".html"))
        {
            return "text/html";
        }
        if (path.endsWith(".txt"))
        {
            return "text/plain";
        }
        if (path.endsWith(".png"))
        {
            return "image/png";
        }
        if (path.endsWith(".jpg"))
        {
            return "image/jpeg";
        }

        return "application/octet-stream";
    }
}
