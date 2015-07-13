package com.elster.jupiter.ftpclient.impl;

import com.elster.jupiter.util.UpdatableHolder;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

abstract class AbstractFtpFileSystemProvider<T extends AbstractFtpFileSystem> extends FileSystemProvider {
    private final ExecutorService executorService;
    private final String scheme;
    private Map<URI, T> openFileSystems = new ConcurrentHashMap<>();

    AbstractFtpFileSystemProvider(String scheme) {
        this.scheme = scheme;
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(8, 16, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        this.executorService = threadPoolExecutor;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public T newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        UpdatableHolder<Boolean> wasNew = new UpdatableHolder<>(false);
        T fileSystem = null;
        try {
            fileSystem = openFileSystems.computeIfAbsent(uri, u -> {
                wasNew.update(true);
                try {
                    T ftpFileSystem = createNewFileSystem(uri);
                    ftpFileSystem.open();
                    return ftpFileSystem;
                } catch (IOException e) {
                    throw new IOExceptionWrapper(e);
                }
            });
        } catch (IOExceptionWrapper e) {
            throw e.getCause();
        }
        if (!wasNew.get()) {
            throw new FileSystemAlreadyExistsException();
        }
        return fileSystem;
    }

    abstract T createNewFileSystem(URI uri);

    @Override
    public T getFileSystem(URI uri) {
        return Optional.ofNullable(openFileSystems.get(uri)).orElseThrow(FileSystemNotFoundException::new);
    }

    @Override
    public Path getPath(URI uri) {
        FileSystem fileSystem = getFileSystem(uri);
        return fileSystem.getPath(uri.getPath());
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        FtpPath ftpPath = (FtpPath) path;
        return ftpPath.getFileSystem().newByteChannel(ftpPath, options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        FtpPath ftpDir = (FtpPath) dir;
        return ftpDir.getFileSystem().newDirectoryStream(ftpDir, filter);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        ((FtpPath) dir).createDirectory();
    }

    @Override
    public void delete(Path path) throws IOException {
        ((FtpPath) path).delete();
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        copy(source, target, options);
        delete(source);
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return ((FtpPath) path).isSameFile(path2);
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return path.getFileSystem().getFileStores().iterator().next();
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        FtpPath ftpPath = (FtpPath) path;
        ftpPath.getFileSystem().checkAccess(ftpPath, modes);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        FtpPath ftpPath = (FtpPath) path;
        return ftpPath.getFileSystem().getFileAttributeView(ftpPath, type);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        if (BasicFileAttributes.class.equals(type)) {
            return (A) getFileAttributeView(path, BasicFileAttributeView.class, options).readAttributes();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException();
    }ExecutorService getExecutorService() {
        return executorService;
    }

    public void closed(T ftpFileSystem) {
        openFileSystems.remove(ftpFileSystem.getUri());
    }
}
