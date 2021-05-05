package com.github.osndok.spr.squashball;

import com.github.osndok.spr.Spr1Directory;
import com.github.osndok.spr.Spr1Repo;
import com.github.osndok.spr.Spr1Tuple;
import com.github.osndok.spr.squashball.impl.DecryptableChallengeV1;
import com.github.osndok.spr.squashball.impl.DeduplicatingSpr1RepoFilter;
import com.github.osndok.spr.squashball.impl.SquashReaderV1;
import com.github.osndok.spr.squashball.impl.TableOfContents;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Input:
 * (0) A password to encode the table-of-contents (stdin)
 * (1) A source directory full of files,
 * (2) An output filename for the final squashball,
 * (3) An optional list of pre-existing squashballs (that will be available at runtime) for deduplication
 */
public
class Create
{
    public static
    void main(String[] argsArray) throws IOException, InterruptedException
    {
        if (argsArray.length < 2)
        {
            printUsage();
            System.exit(1);
        }

        var args = Arrays.asList(argsArray);

        var password = readPasswordFrom(System.in);
        var sourceDirectory = new File(args.remove(0));
        var outputFilename = args.remove(0);
        var helperSquashballs = openAll(args, password);

        var toc = new TableOfContents();
        var tempDirectory = Files.createTempDirectory("spr1-squashball-create");
        Spr1Repo spr1Repo = new Spr1Directory(tempDirectory.toFile());

        for (SquashReader helperSquashball : helperSquashballs)
        {
            spr1Repo = new DeduplicatingSpr1RepoFilter(helperSquashball, spr1Repo);
        }

        deepCopyAllFilesAndDirectoriesIntoRepo(sourceDirectory, spr1Repo, toc);

        var tocTuple = new Spr1Tuple(toc.toBytes());
        spr1Repo.put(tocTuple);

        new DecryptableChallengeV1(tocTuple, password).writeTo(tempDirectory);
        makeSquashball(tempDirectory, outputFilename);
        Files.delete(tempDirectory);
    }

    private static
    void makeSquashball(final Path tempDirectory, final String outputFilename) throws InterruptedException, IOException
    {
        var cmd = new String[]{"mksquashfs", tempDirectory.toString(), outputFilename};

        var process = Runtime.getRuntime().exec(cmd);

        int status = process.waitFor();

        if (status != 0)
        {
            throw new RuntimeException("mksquashfs returned status "+status);
        }

        var file = new File(outputFilename);

        if (!file.exists())
        {
            throw new IOException("mksquashfs did not create output file");
        }

        if (file.length() == 0)
        {
            throw new IOException("mksquashfs created an empty output file");
        }
    }

    private static
    void deepCopyAllFilesAndDirectoriesIntoRepo(
            final File sourceDirectory,
            final Spr1Repo spr1Repo,
            final TableOfContents toc
    ) throws IOException
    {
        for (String baseName : sourceDirectory.list())
        {
            deepCopyAllFilesAndDirectoriesIntoRepo(sourceDirectory, baseName, spr1Repo, toc);
        }
    }

    private static
    void deepCopyAllFilesAndDirectoriesIntoRepo(
            final File sourceDirectory,
            final String relativePath,
            final Spr1Repo spr1Repo,
            final TableOfContents toc
    ) throws IOException
    {
        System.err.println("Adding: "+relativePath);

        var file =  new File(sourceDirectory, relativePath);
        if (file.isFile())
        {
            var tuple = new Spr1Tuple(file);
            spr1Repo.put(tuple);
            toc.add(relativePath, tuple.spr1Key);
        }
        else if (file.isDirectory())
        {
            for (String baseName : file.list())
            {
                var deeperPath = relativePath + "/" + baseName;
                deepCopyAllFilesAndDirectoriesIntoRepo(sourceDirectory, deeperPath, spr1Repo, toc);
            }
        }
        else
        {
            throw new UnsupportedOperationException("not a file or directory: "+file);
        }
    }

    private static
    List<SquashReader> openAll(List<String> squashballFileNames, String password)
    {
        var retval = new ArrayList<SquashReader>();

        for (String filename : squashballFileNames)
        {
            retval.add(new SquashReaderV1(filename, password));
        }

        return retval;
    }

    private static
    String readPasswordFrom(InputStream inputStream) throws IOException
    {
        try (var br = new BufferedReader(new InputStreamReader(inputStream)))
        {
            return br.readLine();
        }
    }

    private static
    void printUsage()
    {
        System.err.println("usage: echo secret | spr1-squashball-create /tmp/input_directory /tmp/output.squash [/tmp/former.squash ...]");
    }
}
