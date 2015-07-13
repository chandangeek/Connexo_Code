package com.elster.jupiter.ftpclient.impl;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.pro.ProFTPClient;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.text.ParseException;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

class FtpFileSystem extends FileSystem {

    private static final Pattern AUTH = Pattern.compile("([^:]*):(.*)");
    private static final int READ_BUFFER_SIZE = 64;
    private static final int WRITE_BUFFER_SIZE = 64;

    private final EdtFtpjFileSystemProvider provider;
    private final URI uri;
    private final ProFTPClient ftpClient = new ProFTPClient();
    private CountingLatch openPipes = new CountingLatch();
    private boolean open;

    FtpFileSystem(EdtFtpjFileSystemProvider provider, URI uri) {
        this.provider = provider;
        this.uri = uri;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    void open() throws IOException {
        try {
            String userInfo = uri.getUserInfo();
            Matcher matcher = AUTH.matcher(userInfo);
            if (matcher.matches()) {
                ftpClient.setRemoteHost(uri.getHost());
                if (uri.getPort() != -1) {
                    ftpClient.setRemotePort(uri.getPort());
                }
                ftpClient.connect();
                ftpClient.login(matcher.group(1), matcher.group(2));
            } else {
                throw new IOException("unable to log in");
            }
        } catch (FTPException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            try {
                openPipes.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ftpClient.quit();
            provider.closed(this);
        } catch (FTPException e) {
            throw new IOException(e);
        } finally {
            open = false;
        }
    }

    @Override
    public boolean isOpen() {
        return open;
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
        return null;
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

    String getHost() {
        return uri.getHost();
    }

    void createDirectory(FtpPath dir) throws IOException {
        try {
            ftpClient.mkdir(dir.toString());
        } catch (FTPException e) {
            throw new IOException(e);
        }
    }

    void delete(FtpPath ftpPath) throws IOException {
        try {
            boolean isDirectory = ftpClient.existsDirectory(ftpPath.toString());
            if (isDirectory) {
                ftpClient.rmdir(ftpPath.toString());
            }
            ftpClient.delete(ftpPath.toString());
        } catch (FTPException e) {
            throw new IOException(e);
        }
    }

    void write(FtpPath path, InputStream inputStream) throws IOException {
        try {
            ftpClient.put(inputStream, path.toString(), false);
        } catch (FTPException e){
            throw new IOException(e);
        }
    }

    private void append(FtpPath path, InputStream inputStream) throws IOException {
        try {
            ftpClient.put(inputStream, path.toString(), true);
        } catch (FTPException e){
            throw new IOException(e);
        }
    }

    void read(FtpPath path, OutputStream outputStream) throws IOException {
        try {
            ftpClient.get(outputStream, path.toString());
        } catch (FTPException e) {
            throw new IOException(e);
        }
    }

    boolean exists(FtpPath path) throws IOException {
        try {
            return ftpClient.existsFile(path.toString()) || ftpClient.existsDirectory(path.toString());
        } catch (FTPException e) {
            throw new IOException(e);
        }
    }

    void checkAccess(FtpPath path, AccessMode... modes) throws IOException {
        if (!path.exists()) {
            throw new NoSuchFileException(path.toString());
        }
        try {
            FTPFile ftpFile = ftpClient.fileDetails(path.toString());
            String permissions = ftpFile.getPermissions();
            if (permissions == null) {
                permissions = "-rwxrwxrwx";
            }
            for (AccessMode mode : modes) {
                switch (mode) {
                    case READ:
                        if (permissions.charAt(1) != 'r') {
                            throw new AccessDeniedException(path.toString());
                        }
                        break;
                    case WRITE:
                        if (permissions.charAt(2) != 'w') {
                            throw new AccessDeniedException(path.toString());
                        }
                        break;
                    default:
                        throw new AccessDeniedException(path.toString());
                }
            }
        } catch (FTPException | ParseException e) {
            throw new IOException(e);
        }
    }

    <V extends FileAttributeView> V getFileAttributeView(FtpPath ftpPath, Class<V> type) {
        if (BasicFileAttributeView.class.equals(type)) {
            return (V) new MyBasicFileAttributeView(() -> {
                try {
                    return ftpClient.fileDetails(ftpPath.toString());
                } catch (IOException e) {
                    throw new IOExceptionWrapper(e);
                } catch (FTPException | ParseException e) {
                    throw new IOExceptionWrapper(new IOException(e));
                }
            });
        }
        return null;
    }

    DirectoryStream<Path> newDirectoryStream(FtpPath ftpDir, DirectoryStream.Filter<? super Path> filter) {
        return new DirectoryStream<Path>() {

            @Override
            public void close() throws IOException {
            }

            @Override
            public Iterator<Path> iterator() {
                try {
                    String[] dir = ftpClient.dir(ftpDir.toString());
                    return Arrays.asList(dir).stream()
                            .map(name -> getPath(name))
                            .filter(path -> {
                                try {
                                    return filter.accept(path);
                                } catch (IOException e) {
                                    throw new IOExceptionWrapper(e);
                                }
                            })
                            .collect(Collectors.<Path>toList())
                            .iterator();
                } catch (IOException e) {
                    throw new IOExceptionWrapper(e);
                } catch (FTPException e) {
                    throw new IOExceptionWrapper(new IOException(e));
                }
            }
        };
    }

    InputStream openInputStream(FtpPath path) throws IOException {
        PipedInputStream pipedInputStream = new PipedInputStream(READ_BUFFER_SIZE);
        PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
        openPipes.acquire();
        provider.getExecutorService().submit(() -> {
            try (OutputStream outputStream = pipedOutputStream) {
                path.getFileSystem().read(path, pipedOutputStream);
            } catch (IOException e) {
                try {
                    pipedInputStream.close();
                } catch (IOException e1) {
                    e.addSuppressed(e1);
                }
                throw new IOExceptionWrapper(e);
            } finally {
                openPipes.release();
            }
        }, provider.getExecutorService());

        return pipedInputStream;
    }

    OutputStream openOutputStream(FtpPath path) throws IOException {
        PipedInputStream pipedInputStream = new PipedInputStream(WRITE_BUFFER_SIZE);
        openPipes.acquire();
        provider.getExecutorService().submit(() -> {
            try {
                path.getFileSystem().write(path, pipedInputStream);
            } finally {
                openPipes.release();
                pipedInputStream.close();
            }
            return null;
        });
        return new PipedOutputStream(pipedInputStream);
    }

    OutputStream openOutputStreamToAppend(FtpPath path) throws IOException {
        PipedInputStream pipedInputStream = new PipedInputStream(WRITE_BUFFER_SIZE);
        openPipes.acquire();
        provider.getExecutorService().submit(() -> {
            try {
                path.getFileSystem().append(path, pipedInputStream);
            } finally {
                openPipes.release();
                pipedInputStream.close();
            }
            return null;
        });
        return new PipedOutputStream(pipedInputStream);
    }

    SeekableByteChannel newByteChannel(FtpPath ftpPath, Set<? extends OpenOption> givenOptions, FileAttribute<?>[] attrs) throws IOException {
        Set<StandardOpenOption> options = givenOptions.stream()
                .filter(opt -> opt instanceof StandardOpenOption)
                .map(StandardOpenOption.class::cast)
                .collect(() -> EnumSet.noneOf(StandardOpenOption.class), EnumSet::add, AbstractCollection::addAll);
        if (ftpPath.exists()) {
            if (options.contains(CREATE_NEW)) {
                throw new FileAlreadyExistsException(ftpPath.toString());
            }
            if (isOpenedForReading(options)) {
                return new FtpReadableByteChannel(ftpClient, ftpPath);
            } else {
                if (options.contains(APPEND)) {
                    return FtpWritableByteChannel.toAppend(ftpPath);
                }
                return FtpWritableByteChannel.toOverwrite(ftpPath);
            }
        } else {
            if (isOpenedForReading(options)) {
                throw new NoSuchFileException(ftpPath.toString());
            }
            if (options.contains(CREATE) || options.contains(CREATE_NEW)) {
                return FtpWritableByteChannel.toOverwrite(ftpPath);
            } else {
                throw new NoSuchFileException(ftpPath.toString());
            }
        }
    }

    private boolean isOpenedForReading(Set<StandardOpenOption> options) {
        return !options.contains(WRITE) && !options.contains(APPEND);
    }

    long size(FtpPath path) throws IOException {
        try {
            return ftpClient.size(path.toString());
        } catch (FTPException e) {
            throw new IOException(e);
        }
    }

    URI getUri() {
        return uri;
    }

     private class AsFileStore extends FileStore {
        @Override
        public String name() {
            return uri.toString();
        }

        @Override
        public String type() {
            return EdtFtpjFileSystemProvider.SCHEME;
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

    private static class MyBasicFileAttributeView implements BasicFileAttributeView {

        private Supplier<FTPFile> ftpFile;
        boolean resolved = false;

        private MyBasicFileAttributeView(Supplier<FTPFile> ftpFile) {
            this.ftpFile = ftpFile;
        }

        private FTPFile getFtpFile() {
            if (!resolved) {
                FTPFile file = this.ftpFile.get();
                ftpFile = () -> file;
                resolved = true;
                return file;
            }
            return ftpFile.get();
        }

        @Override
        public String name() {
            return "basic";
        }

        @Override
        public BasicFileAttributes readAttributes() throws IOException {
            try {
                final FTPFile gotten = getFtpFile();
                return new BasicFileAttributes() {
                    @Override
                    public FileTime lastModifiedTime() {
                        return FileTime.from(gotten.lastModified().toInstant());
                    }

                    @Override
                    public FileTime lastAccessTime() {
                        return FileTime.from(gotten.lastModified().toInstant());
                    }

                    @Override
                    public FileTime creationTime() {
                        return FileTime.from(gotten.created().toInstant());
                    }

                    @Override
                    public boolean isRegularFile() {
                        return gotten.isFile();
                    }

                    @Override
                    public boolean isDirectory() {
                        return gotten.isDir();
                    }

                    @Override
                    public boolean isSymbolicLink() {
                        return gotten.isLink();
                    }

                    @Override
                    public boolean isOther() {
                        return !isRegularFile() && !isDirectory() && ! isSymbolicLink();
                    }

                    @Override
                    public long size() {
                        return gotten.size();
                    }

                    @Override
                    public Object fileKey() {
                        return null;
                    }
                };
            } catch (IOExceptionWrapper e) {
                throw e.getCause();
            }
        }

        @Override
        public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
