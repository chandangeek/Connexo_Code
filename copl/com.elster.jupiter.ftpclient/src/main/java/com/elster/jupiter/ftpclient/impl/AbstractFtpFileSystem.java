package com.elster.jupiter.ftpclient.impl;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

abstract class AbstractFtpFileSystem extends FileSystem {
    private final URI uri;

    public AbstractFtpFileSystem(URI uri) {
        this.uri = uri;
    }

    URI getUri() {
        return uri;
    }

    abstract void open() throws IOException;

    String getHost() {
        return uri.getHost();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.singleton(new FtpPath(this, "/"));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return Collections.singleton(new AsFileStore());
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Collections.singleton("basic");
    }

    @Override
    public FtpPath getPath(String first, String... more) {
        return new FtpPath(this, ImmutableList.<String>builder().add(first).add(more).build());
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        if (syntaxAndPattern.startsWith("glob:")) {
            String regexPattern = GlobToRegex.toRegex(syntaxAndPattern.substring(5), "/");
            return new RegexPathMatcher(Pattern.compile(regexPattern));
        }
        if (syntaxAndPattern.startsWith("regex:")) {
            return new RegexPathMatcher(Pattern.compile(syntaxAndPattern.substring(6)));
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

    abstract void createDirectory(FtpPath dir) throws IOException;

    abstract void delete(FtpPath ftpPath) throws IOException;

    abstract boolean exists(FtpPath path) throws IOException;

    abstract void checkAccess(FtpPath path, AccessMode... modes) throws IOException;

    abstract <V extends FileAttributeView> V getFileAttributeView(FtpPath ftpPath, Class<V> type);

    abstract DirectoryStream<Path> newDirectoryStream(FtpPath ftpDir, DirectoryStream.Filter<? super Path> filter);

    abstract InputStream openInputStream(FtpPath path) throws IOException;

    abstract OutputStream openOutputStream(FtpPath path) throws IOException;

    abstract OutputStream openOutputStreamToAppend(FtpPath path) throws IOException;

    abstract SeekableByteChannel newByteChannel(FtpPath ftpPath, Set<? extends OpenOption> givenOptions, FileAttribute<?>[] attrs) throws IOException;

    abstract long size(FtpPath path) throws IOException;

    private class AsFileStore extends FileStore {
        @Override
        public String name() {
            return getUri().toString();
        }

        @Override
        public String type() {
            return FtpFileSystemProvider.SCHEME;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public long getTotalSpace() throws IOException {
            return 0;
        }

        @Override
        public long getUsableSpace() throws IOException {
            return 0;
        }

        @Override
        public long getUnallocatedSpace() throws IOException {
            return 0;
        }

        @Override
        public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
            return BasicFileAttributeView.class.isAssignableFrom(type);
        }

        @Override
        public boolean supportsFileAttributeView(String name) {
            return "basic".equals(name);
        }

        @Override
        public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getAttribute(String attribute) throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
