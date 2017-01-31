/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ftpclient.impl;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.pro.ProFTPClient;

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
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.text.ParseException;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

abstract class ProFtpClientFileSystem<S extends ProFTPClient, T extends ProFtpClientFileSystem<S, T>> extends AbstractFtpFileSystem {
    private static final Pattern AUTH = Pattern.compile("([^:]*):(.*)");
    private static final int READ_BUFFER_SIZE = 64;
    private static final int WRITE_BUFFER_SIZE = 64;
    protected final AbstractFtpFileSystemProvider<T> provider;
    private final S ftpClient;
    private Semaphore openPipes = new Semaphore(1, true);
    private boolean open;


    ProFtpClientFileSystem(URI uri, AbstractFtpFileSystemProvider<T> provider, S proFTPClient) {
        super(uri);
        this.provider = provider;
        this.ftpClient = proFTPClient;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    void open() throws IOException {
        try {
            String userInfo = getUri().getUserInfo();
            if (userInfo == null) {
                throw new IOException("unable to log in");
            }
            Matcher matcher = AUTH.matcher(userInfo);
            if (matcher.matches()) {
                ftpClient.setRemoteHost(getHost());
                if (getUri().getPort() != -1) {
                    ftpClient.setRemotePort(getUri().getPort());
                }
                ftpClient.connect();
                postConnectCommands(ftpClient);
                ftpClient.login(matcher.group(1), matcher.group(2));
            } else {
                throw new IOException("unable to log in");
            }
        } catch (FTPException e) {
            throw new IOException(e);
        }
    }

    protected void postConnectCommands(S ftpClient) throws IOException, FTPException {
        // nothing by default
    }

    @Override
    public void close() throws IOException {
        boolean interrupted = false;
        try {
            boolean acquired = false;
            while (!acquired) {
                try {
                    openPipes.acquire();
                    acquired = true;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            ftpClient.quit();
            provider.closed((T) this);
        } catch (FTPException e) {
            throw new IOException(e);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            open = false;
        }
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    void createDirectory(FtpPath dir) throws IOException {
        try {
            if (!ftpClient.existsDirectory(dir.toString())) {
                ftpClient.mkdir(dir.toString());
            }
        } catch (FTPException e) {
            throw new IOException(e);
        }
    }

    @Override
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
        } catch (FTPException e) {
            throw new IOException(e);
        }
    }

    private void append(FtpPath path, InputStream inputStream) throws IOException {
        try {
            ftpClient.put(inputStream, path.toString(), true);
        } catch (FTPException e) {
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

    @Override
    boolean exists(FtpPath path) throws IOException {
        try {
            return ftpClient.existsFile(path.toString()) || ftpClient.existsDirectory(path.toString());
        } catch (FTPException e) {
            throw new IOException(e);
        }
    }

    @Override
    void checkAccess(FtpPath path, AccessMode... modes) throws IOException {
        if (!path.exists()) {
            throw new NoSuchFileException(path.toString());
        }
        try {
            String permissions = null;
            if (ftpClient.existsFile(path.toString())) {
                FTPFile ftpFile = ftpClient.fileDetails(path.toString());
                permissions = ftpFile.getPermissions();
            }
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

    @Override
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

    @Override
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

    @Override
    InputStream openInputStream(FtpPath path) throws IOException {
        if (!this.equals(path.getFileSystem())) {
            throw new IllegalArgumentException();
        }
        PipedInputStream pipedInputStream = new PipedInputStream(READ_BUFFER_SIZE);
        PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
        boolean interrupted = false;
        boolean acquired = false;
        while (!acquired) {
            try {
                openPipes.acquire();
                acquired = true;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        provider.getExecutorService().submit(() -> {
            try (OutputStream outputStream = pipedOutputStream) {
                read(path, pipedOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    pipedInputStream.close();
                } catch (IOException e1) {
                    e.addSuppressed(e1);
                }
                throw new IOExceptionWrapper(e);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                openPipes.release();
            }
        }, provider.getExecutorService());

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return pipedInputStream;
    }

    @Override
    OutputStream openOutputStream(FtpPath path) throws IOException {
        if (!this.equals(path.getFileSystem())) {
            throw new IllegalArgumentException();
        }
        PipedInputStream pipedInputStream = new PipedInputStream(WRITE_BUFFER_SIZE);
        boolean interrupted = false;
        boolean acquired = false;
        while (!acquired) {
            try {
                openPipes.acquire();
                acquired = true;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        CountDownLatch waitLatch = new CountDownLatch(1);
        provider.getExecutorService().submit(() -> {
            try {
                write(path, pipedInputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                openPipes.release();
                waitLatch.countDown();
                pipedInputStream.close();
            }
            return null;
        });
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return new OutputStreamDecorator(new PipedOutputStream(pipedInputStream), waitLatch);
    }

    private class OutputStreamDecorator extends OutputStream {
        private final OutputStream decorated;
        private final CountDownLatch waitLatch;

        private OutputStreamDecorator(OutputStream decorated, CountDownLatch waitLatch) {
            this.decorated = decorated;
            this.waitLatch = waitLatch;
        }

        @Override
        public void write(int b) throws IOException {
            decorated.write(b);
        }

        @Override
        public void close() throws IOException {
            decorated.close();
            try {
                waitLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    OutputStream openOutputStreamToAppend(FtpPath path) throws IOException {
        if (!this.equals(path.getFileSystem())) {
            throw new IllegalArgumentException();
        }
        PipedInputStream pipedInputStream = new PipedInputStream(WRITE_BUFFER_SIZE);
        boolean interrupted = false;
        boolean acquired = false;
        while (!acquired) {
            try {
                openPipes.acquire();
                acquired = true;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        provider.getExecutorService().submit(() -> {
            try {
                append(path, pipedInputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                openPipes.release();
                pipedInputStream.close();
            }
            return null;
        });
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return new PipedOutputStream(pipedInputStream);
    }

    @Override
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

    @Override
    long size(FtpPath path) throws IOException {
        boolean interrupted = false;
        boolean acquired = false;
        try {
            while (!acquired) {
                try {
                    openPipes.acquire();
                    acquired = true;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            return ftpClient.size(path.toString());
        } catch (FTPException e) {
            throw new IOException(e);
        } finally {
            openPipes.release();
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
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
                        return !isRegularFile() && !isDirectory() && !isSymbolicLink();
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
