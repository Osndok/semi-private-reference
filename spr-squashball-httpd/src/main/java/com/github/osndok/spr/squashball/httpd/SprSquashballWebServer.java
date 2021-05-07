package com.github.osndok.spr.squashball.httpd;

import java.io.File;

/**
 * Given either a squashball or a directory of squashballs, serve the contents via a small HTTPD server.
 * The decryption password may either be provided or requested at runtime.
 * The port number may be provided, or the server will try to allocate one semi-stable to the content being served.
 */
public
class SprSquashballWebServer
{
    private final
    File file;

    public
    SprSquashballWebServer(final File file)
    {
        this.file = file;
    }
}
